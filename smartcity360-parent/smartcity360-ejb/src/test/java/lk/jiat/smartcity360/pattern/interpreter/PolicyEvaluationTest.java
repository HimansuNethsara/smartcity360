package lk.jiat.smartcity360.pattern.interpreter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class PolicyEvaluationTest {

    private static final PolicyVocabulary VOCAB = PolicyVocabulary.standard();

    private static PolicyResult run(String policy, EvaluationContext ctx) {
        return PolicyParser.parse(policy, VOCAB).evaluate(ctx);
    }

    private static EvaluationContext ctx() {
        return new EvaluationContext();
    }

    // ------------------------------------------------------------------ outcomes

    @Test
    @DisplayName("A violated FINE policy returns the action text")
    void violatedFine() {
        PolicyResult r = run("IF noise > 70 THEN FINE 500", ctx().put("noise", 80.0));
        assertTrue(r.violated());
        assertEquals("FINE 500", r.actionText());
    }

    @Test
    @DisplayName("A violated ALERT policy returns the quoted message")
    void violatedAlert() {
        PolicyResult r = run("IF noise > 70 THEN ALERT \"Too loud\"", ctx().put("noise", 80.0));
        assertEquals("ALERT \"Too loud\"", r.actionText());
    }

    @Test
    @DisplayName("A policy that is not violated has no action")
    void notViolated() {
        PolicyResult r = run("IF noise > 70 THEN FINE 500", ctx().put("noise", 60.0));
        assertFalse(r.violated());
        assertNull(r.actionText());
    }

    @ParameterizedTest
    @CsvSource({
            "'>',  70.0, false",
            "'>',  71.0, true",
            "'>=', 70.0, true",
            "'<',  70.0, false",
            "'<',  69.0, true",
            "'<=', 70.0, true",
            "'=',  70.0, true",
            "'=',  70.5, false",
            "'!=', 70.0, false",
            "'!=', 12.0, true"})
    @DisplayName("Each comparison operator behaves correctly at the boundary (threshold = 70)")
    void operators(String op, double reading, boolean expected) {
        PolicyResult r = run("IF noise " + op + " 70 THEN FINE 1", ctx().put("noise", reading));
        assertEquals(expected, r.violated());
    }

    // ------------------------------------------------------------------ AND / OR / NOT

    @Test
    @DisplayName("OR with AND: precedence decides the result")
    void precedence() {
        String p = "IF noise > 70 OR hour > 22 AND zone = \"RESIDENTIAL\" THEN FINE 1";
        assertTrue(run(p, ctx().put("noise", 80.0).put("hour", 10.0).put("zone", "COMMERCIAL")).violated());
        assertFalse(run(p, ctx().put("noise", 50.0).put("hour", 23.0).put("zone", "COMMERCIAL")).violated());
        assertTrue(run(p, ctx().put("noise", 50.0).put("hour", 23.0).put("zone", "RESIDENTIAL")).violated());
    }

    @Test
    @DisplayName("Brackets change the result of the same conditions")
    void bracketsChangeResult() {
        String p = "IF (noise > 70 OR hour > 22) AND zone = \"RESIDENTIAL\" THEN FINE 1";
        assertFalse(run(p, ctx().put("noise", 80.0).put("hour", 10.0).put("zone", "COMMERCIAL")).violated());
        assertTrue(run(p, ctx().put("noise", 80.0).put("hour", 10.0).put("zone", "RESIDENTIAL")).violated());
    }

    @Test
    @DisplayName("NOT inverts its operand")
    void not() {
        String p = "IF NOT noise > 70 THEN FINE 1";
        assertTrue(run(p, ctx().put("noise", 50.0)).violated());
        assertFalse(run(p, ctx().put("noise", 90.0)).violated());
    }

    @Test
    @DisplayName("Text comparison ignores case")
    void textIgnoresCase() {
        assertTrue(run("IF zone = \"residential\" THEN FINE 1", ctx().put("zone", "residential")).violated());
        assertTrue(run("IF zone != \"INDUSTRIAL\" THEN FINE 1", ctx().put("zone", "RESIDENTIAL")).violated());
    }

    @Test
    @DisplayName("AND and OR stop early, so an unneeded reading may be missing")
    void shortCircuit() {
        assertFalse(run("IF noise > 70 AND hour > 22 THEN FINE 1", ctx().put("noise", 50.0)).violated());
        assertTrue(run("IF noise > 70 OR hour > 22 THEN FINE 1", ctx().put("noise", 80.0)).violated());
    }

    // ------------------------------------------------------------------ problems at evaluation time

    @Test
    @DisplayName("A needed reading that is missing raises MissingValueException naming the variable")
    void missingValue() {
        MissingValueException ex = assertThrows(MissingValueException.class,
                () -> run("IF noise > 70 THEN FINE 1", ctx()));
        assertTrue(ex.getMessage().contains("noise"));
        assertInstanceOf(PolicyEvaluationException.class, ex);
    }

    @Test
    @DisplayName("A null reading is treated as missing")
    void nullIsMissing() {
        assertThrows(MissingValueException.class, () -> run("IF noise > 70 THEN FINE 1", ctx().put("noise", null)));
    }

    @Test
    @DisplayName("Context names are case-insensitive")
    void contextNamesCaseInsensitive() {
        assertTrue(run("IF noise > 70 THEN FINE 1", ctx().put("NOISE", 80.0)).violated());
    }

    @Test
    @DisplayName("A reading of the wrong type fails evaluation with a clear message")
    void wrongTypeReading() {
        PolicyEvaluationException ex = assertThrows(PolicyEvaluationException.class,
                () -> run("IF noise > 70 THEN FINE 1", ctx().put("noise", "loud")));
        assertTrue(ex.getMessage().contains("Cannot compare"));
    }

    @Test
    @DisplayName("The context exposes a read-only snapshot")
    void asMapIsReadOnly() {
        EvaluationContext c = ctx().put("noise", 1.0);
        assertEquals(1.0, c.asMap().get("noise"));
        assertThrows(UnsupportedOperationException.class, () -> c.asMap().put("x", 1));
    }

    @Test
    @DisplayName("Expression tree exposes labels and children for display")
    void treeForDisplay() {
        PolicyProgram p = PolicyParser.parse("IF noise > 70 AND NOT hour < 6 THEN FINE 1", VOCAB);
        Expression and = p.condition();
        assertEquals("AND", and.label());
        assertEquals("noise > 70", and.children().get(0).label());
        assertTrue(and.children().get(0).children().isEmpty());
        assertEquals("NOT", and.children().get(1).label());
    }
}
