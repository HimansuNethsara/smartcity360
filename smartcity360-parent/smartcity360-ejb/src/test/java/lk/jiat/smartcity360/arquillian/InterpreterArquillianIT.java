package lk.jiat.smartcity360.arquillian;

import lk.jiat.smartcity360.pattern.interpreter.EvaluationContext;
import lk.jiat.smartcity360.pattern.interpreter.MissingValueException;
import lk.jiat.smartcity360.pattern.interpreter.PolicyParseException;
import lk.jiat.smartcity360.pattern.interpreter.PolicyParser;
import lk.jiat.smartcity360.pattern.interpreter.PolicyProgram;
import lk.jiat.smartcity360.pattern.interpreter.PolicyResult;
import lk.jiat.smartcity360.pattern.interpreter.PolicyVocabulary;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@Tag("arquillian")
@ExtendWith(ArquillianExtension.class)
public class InterpreterArquillianIT {

    @Deployment
    public static WebArchive deployment() {
        return Deployments.war("sc360-interpreter-it",
                "lk.jiat.smartcity360.pattern.interpreter", "lk.jiat.smartcity360.domain.enums");
    }

    private static final PolicyVocabulary VOCAB = PolicyVocabulary.standard();

    private static PolicyResult run(String policy, EvaluationContext ctx) {
        return PolicyParser.parse(policy, VOCAB).evaluate(ctx);
    }

    @Test
    @DisplayName("A policy parses, evaluates and returns its action")
    void evaluates() {
        PolicyResult r = run("IF noise > 70 AND zone = \"RESIDENTIAL\" THEN FINE 500",
                new EvaluationContext().put("noise", 82.0).put("zone", "RESIDENTIAL"));
        assertTrue(r.violated());
        assertEquals("FINE 500", r.actionText());
    }

    @Test
    @DisplayName("Brackets and NOT change the result as in the language definition")
    void precedence() {
        String p = "IF (noise > 70 OR hour > 22) AND NOT zone = \"INDUSTRIAL\" THEN ALERT \"Quiet hours\"";
        assertTrue(run(p, new EvaluationContext().put("noise", 80.0).put("hour", 10.0).put("zone", "MIXED")).violated());
        assertFalse(run(p, new EvaluationContext().put("noise", 80.0).put("hour", 10.0).put("zone", "INDUSTRIAL")).violated());
    }

    @Test
    @DisplayName("describe() prints the parsed program back in normal form")
    void describe() {
        PolicyProgram p = PolicyParser.parse("if noise>70 then fine 500", VOCAB);
        assertEquals("IF noise > 70 THEN FINE 500", p.describe());
    }

    @Test
    @DisplayName("Bad policies are rejected with a position; missing readings are reported")
    void errors() {
        PolicyParseException ex = assertThrows(PolicyParseException.class,
                () -> PolicyParser.parse("IF foo > 1 THEN FINE 5", VOCAB));
        assertEquals(4, ex.getPosition());
        assertThrows(MissingValueException.class, () -> run("IF noise > 70 THEN FINE 1", new EvaluationContext()));
    }
}
