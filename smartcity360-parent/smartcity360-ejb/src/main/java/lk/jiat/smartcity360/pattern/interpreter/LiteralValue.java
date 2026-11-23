package lk.jiat.smartcity360.pattern.interpreter;

import java.math.BigDecimal;

public final class LiteralValue implements ValueExpression {

    private final Object value;

    public LiteralValue(Object value) {
        this.value = value;
    }

    @Override
    public Object evaluate(EvaluationContext context) {
        return value;
    }

    @Override
    public String describe() {
        return value instanceof Double d ? new BigDecimal(d.toString()).stripTrailingZeros().toPlainString()
                : "\"" + value + "\"";
    }
}
