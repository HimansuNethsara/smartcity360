package lk.jiat.smartcity360.pattern.interpreter;

import java.util.List;

public final class NotExpression implements Expression {

    private final Expression operand;

    public NotExpression(Expression operand) {
        this.operand = operand;
    }

    @Override
    public boolean interpret(EvaluationContext context) {
        return !operand.interpret(context);
    }

    @Override
    public String describe() {
        boolean group = operand instanceof AndExpression || operand instanceof OrExpression;
        return "NOT " + (group ? "(" + operand.describe() + ")" : operand.describe());
    }

    @Override
    public String label() {
        return "NOT";
    }

    @Override
    public List<Expression> children() {
        return List.of(operand);
    }
}
