package lk.jiat.smartcity360.pattern.interpreter;

import java.util.List;

public final class ComparisonExpression implements Expression {

    public enum Operator {
        GT(">"), LT("<"), GE(">="), LE("<="), EQ("="), NE("!=");

        private final String symbol;

        Operator(String symbol) {
            this.symbol = symbol;
        }

        public String symbol() {
            return symbol;
        }

        public static Operator of(String symbol) {
            for (Operator o : values()) {
                if (o.symbol.equals(symbol)) {
                    return o;
                }
            }
            return null;
        }
    }

    private final ValueExpression left;
    private final Operator operator;
    private final ValueExpression right;

    public ComparisonExpression(ValueExpression left, Operator operator, ValueExpression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public boolean interpret(EvaluationContext context) {
        Object a = left.evaluate(context);
        Object b = right.evaluate(context);
        if (a instanceof Double x && b instanceof Double y) {
            int c = Double.compare(x, y);
            return switch (operator) {
                case GT -> c > 0;
                case LT -> c < 0;
                case GE -> c >= 0;
                case LE -> c <= 0;
                case EQ -> c == 0;
                case NE -> c != 0;
            };
        }
        if (a instanceof String x && b instanceof String y) {
            boolean same = x.equalsIgnoreCase(y);
            return switch (operator) {
                case EQ -> same;
                case NE -> !same;
                default -> throw new PolicyEvaluationException("Texts can only be compared with = or !=");
            };
        }
        throw new PolicyEvaluationException("Cannot compare " + left.describe() + " with " + right.describe());
    }

    @Override
    public String describe() {
        return left.describe() + " " + operator.symbol() + " " + right.describe();
    }

    @Override
    public String label() {
        return describe();
    }

    @Override
    public List<Expression> children() {
        return List.of();
    }
}
