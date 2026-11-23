package lk.jiat.smartcity360.pattern.interpreter;

public final class VariableValue implements ValueExpression {

    private final String name;

    public VariableValue(String name) {
        this.name = name;
    }

    @Override
    public Object evaluate(EvaluationContext context) {
        return context.get(name);
    }

    @Override
    public String describe() {
        return name;
    }
}
