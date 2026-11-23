package lk.jiat.smartcity360.pattern.interpreter;

public interface ValueExpression {

    Object evaluate(EvaluationContext context);

    String describe();
}
