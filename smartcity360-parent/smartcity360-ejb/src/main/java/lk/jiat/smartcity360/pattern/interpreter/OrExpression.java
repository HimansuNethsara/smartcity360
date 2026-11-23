package lk.jiat.smartcity360.pattern.interpreter;

import java.util.List;

public final class OrExpression implements Expression {

    private final Expression left;
    private final Expression right;

    public OrExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean interpret(EvaluationContext context) {
        return left.interpret(context) || right.interpret(context);
    }

    @Override
    public String describe() {
        return left.describe() + " OR " + right.describe();
    }

    @Override
    public String label() {
        return "OR";
    }

    @Override
    public List<Expression> children() {
        return List.of(left, right);
    }
}
