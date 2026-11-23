package lk.jiat.smartcity360.pattern.interpreter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class PolicyParserTest {

    private static final PolicyVocabulary VOCAB = PolicyVocabulary.standard();

    private static PolicyProgram parse(String src) {
        return PolicyParser.parse(src, VOCAB);
    }

    private static PolicyParseException rejected(String src) {
        return assertThrows(PolicyParseException.class, () -> parse(src), src);
    }

    // ------------------------------------------------------------------ good policies

    @Test
    @DisplayName("describe() prints the parsed policy back in normal form")
    void describeNormalises() {
        assertEquals("IF noise > 70 THEN FINE 500", parse("if noise>70 then fine 500").describe());
        assertEquals("IF temperature >= 35.5 THEN ALERT \"Heat\"", parse("IF temperature >= 35.5 THEN ALERT \"Heat\"").describe());
    }

    @Test
    @DisplayName("AND binds tighter than OR")
    void andBeforeOr() {
        Expression e = parse("IF noise > 70 OR hour > 22 AND zone = \"RESIDENTIAL\" THEN FINE 100").condition();
        assertEquals("OR", e.label());
        assertEquals("AND", e.children().get(1).label());
    }

    @Test
    @DisplayName("Brackets override precedence and are kept when describing")
    void brackets() {
        PolicyProgram p = parse("IF (noise > 70 OR hour > 22) AND zone = \"residential\" THEN ALERT \"x\"");
        assertEquals("AND", p.condition().label());
        assertEquals("IF (noise > 70 OR hour > 22) AND zone = \"RESIDENTIAL\" THEN ALERT \"x\"", p.describe());
    }

    @Test
    @DisplayName("NOT groups AND/OR operands in brackets when described")
    void notGroup() {
        PolicyProgram p = parse("IF NOT (noise > 70 AND hour > 22) THEN FINE 1");
        assertEquals("NOT", p.condition().label());
        assertEquals(1, p.condition().children().size());
        assertEquals("IF NOT (noise > 70 AND hour > 22) THEN FINE 1", p.describe());
    }

    @Test
    @DisplayName("The action is a FineAction or an AlertAction with its details")
    void actions() {
        PolicyAction fine = parse("IF noise > 1 THEN FINE 5000").action();
        assertInstanceOf(FineAction.class, fine);
        assertEquals(5000, ((FineAction) fine).amount());
        assertEquals("FINE", fine.kind());

        PolicyAction alert = parse("IF noise > 1 THEN ALERT \"Too loud\"").action();
        assertInstanceOf(AlertAction.class, alert);
        assertEquals("Too loud", ((AlertAction) alert).message());
        assertEquals("ALERT", alert.kind());
    }

    @Test
    @DisplayName("Ten levels of brackets are accepted")
    void nestingAtLimit() {
        assertDoesNotThrow(() -> parse("IF " + "(".repeat(10) + "noise > 1" + ")".repeat(10) + " THEN FINE 1"));
    }

    // ------------------------------------------------------------------ rejected policies

    @Test
    @DisplayName("Empty, blank, null and over-long policies are rejected")
    void sizeLimits() {
        assertThrows(PolicyParseException.class, () -> PolicyParser.parse(null, VOCAB));
        rejected("   ");
        PolicyParseException tooLong = rejected("IF noise > 1 THEN ALERT \"" + "a".repeat(PolicyParser.MAX_LENGTH) + "\"");
        assertTrue(tooLong.getMessage().contains("longer than"));
    }

    @Test
    @DisplayName("Structure errors say what was expected and what was found")
    void structureErrors() {
        assertTrue(rejected("noise > 1 THEN FINE 1").getMessage().contains("must start with IF"));
        assertTrue(rejected("IF noise > 1 FINE 1").getMessage().contains("Expected THEN"));
        assertTrue(rejected("IF noise > 1 THEN").getMessage().contains("Expected FINE or ALERT"));
        assertTrue(rejected("IF noise > 1 THEN FINE 5 extra").getMessage().contains("Nothing may follow"));
        assertTrue(rejected("IF (noise > 1 THEN FINE 5").getMessage().contains("Expected ')'"));
        assertTrue(rejected("IF THEN FINE 5").getMessage().contains("Expected a variable name"));
        assertTrue(rejected("IF noise 5 THEN FINE 5").getMessage().contains("Expected a comparison"));
    }

    @Test
    @DisplayName("An unknown variable is reported with its position and the known names")
    void unknownVariable() {
        PolicyParseException ex = rejected("IF foo > 1 THEN FINE 5");
        assertTrue(ex.getMessage().contains("Unknown variable 'foo'"));
        assertTrue(ex.getMessage().contains("noise"));
        assertEquals(4, ex.getPosition());
    }

    @Test
    @DisplayName("Types are checked while parsing")
    void typeChecks() {
        assertTrue(rejected("IF noise > \"loud\" THEN FINE 5").getMessage().contains("is a number"));
        assertTrue(rejected("IF zone = 5 THEN FINE 5").getMessage().contains("is text"));
        assertTrue(rejected("IF zone > \"RESIDENTIAL\" THEN FINE 5").getMessage().contains("only be compared with = or !="));
        assertTrue(rejected("IF zone = \"MOON\" THEN FINE 5").getMessage().contains("Use one of"));
    }

    @ParameterizedTest
    @CsvSource({"FINE 0", "FINE 1000001", "FINE 2.5", "FINE -5", "FINE 99999999999999999999", "FINE abc"})
    @DisplayName("Fines must be whole numbers between 1 and 1,000,000")
    void fineLimits(String action) {
        rejected("IF noise > 1 THEN " + action);
    }

    @Test
    @DisplayName("Alert messages must be present and not too long")
    void alertLimits() {
        rejected("IF noise > 1 THEN ALERT 5");
        rejected("IF noise > 1 THEN ALERT \" \"");
        rejected("IF noise > 1 THEN ALERT \"" + "a".repeat(121) + "\"");
    }

    @Test
    @DisplayName("Excessive nesting is rejected")
    void nestingTooDeep() {
        PolicyParseException ex = rejected("IF " + "(".repeat(11) + "noise > 1" + ")".repeat(11) + " THEN FINE 1");
        assertTrue(ex.getMessage().contains("nested too deeply"));
        rejected("IF " + "NOT ".repeat(11) + "noise > 1 THEN FINE 1");
    }

    @Test
    @DisplayName("The vocabulary lists its variables and allowed text values")
    void vocabulary() {
        assertEquals(PolicyVocabulary.VariableType.NUMBER, VOCAB.typeOf("noise"));
        assertEquals(PolicyVocabulary.VariableType.TEXT, VOCAB.typeOf("zone"));
        assertNull(VOCAB.typeOf("nope"));
        assertTrue(VOCAB.names().containsAll(java.util.List.of("noise", "temperature", "air_quality", "hour", "zone")));
        assertTrue(VOCAB.allowedTexts("zone").contains("RESIDENTIAL"));
    }
}
