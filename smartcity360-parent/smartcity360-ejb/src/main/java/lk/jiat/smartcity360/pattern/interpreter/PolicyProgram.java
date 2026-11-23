package lk.jiat.smartcity360.pattern.interpreter;

public final class PolicyProgram {

    private final Expression condition;
    private final PolicyAction action;

    public PolicyProgram(Expression condition, PolicyAction action) {
        this.condition = condition;
        this.action = action;
    }

    public PolicyResult evaluate(EvaluationContext context) {
        return condition.interpret(context)
                ? new PolicyResult(true, action.result())
                : new PolicyResult(false, null);
    }

    public String describe() {
        return "IF " + condition.describe() + " THEN " + action.result();
    }

    public Expression condition() {
        return condition;
    }

    public PolicyAction action() {
        return action;
    }
}
