package lk.jiat.smartcity360.pattern.interpreter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolicyProgramMockTest {

    @Mock
    Expression condition;
    @Mock
    PolicyAction action;

    private final EvaluationContext ctx = new EvaluationContext();

    @Test
    @DisplayName("When the condition is true the program reports a violation with the action's text")
    void violated() {
        when(condition.interpret(ctx)).thenReturn(true);
        when(action.result()).thenReturn("FINE 1");

        PolicyResult r = new PolicyProgram(condition, action).evaluate(ctx);

        assertTrue(r.violated());
        assertEquals("FINE 1", r.actionText());
    }

    @Test
    @DisplayName("When the condition is false the action is never asked for its result")
    void notViolated() {
        when(condition.interpret(ctx)).thenReturn(false);

        PolicyResult r = new PolicyProgram(condition, action).evaluate(ctx);

        assertFalse(r.violated());
        verify(action, never()).result();
    }

    @Test
    @DisplayName("describe() combines the condition's and the action's text")
    void describe() {
        when(condition.describe()).thenReturn("noise > 70");
        when(action.result()).thenReturn("FINE 5");
        assertEquals("IF noise > 70 THEN FINE 5", new PolicyProgram(condition, action).describe());
    }
}
