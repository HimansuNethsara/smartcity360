package lk.jiat.smartcity360.pattern.interpreter;

import java.util.List;

public interface Expression {

    boolean interpret(EvaluationContext context);

    String describe();

    String label();

    List<Expression> children();
}
