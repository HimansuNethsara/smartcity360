package lk.jiat.smartcity360.pattern.interpreter;

import java.util.List;

public final class AndExpression implements Expression {

    private final Expression left;
    private final Expression right;

    public AndExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean interpret(EvaluationContext context) {
        return left.interpret(context) && right.interpret(context);
    }

    @Override
    public String describe() {
        return wrap(left) + " AND " + wrap(right);
    }

    private static String wrap(Expression e) {
        return e instanceof OrExpression ? "(" + e.describe() + ")" : e.describe();
    }

    @Override
    public String label() {
        return "AND";
    }

    @Override
    public List<Expression> children() {
        return List.of(left, right);
    }
}
