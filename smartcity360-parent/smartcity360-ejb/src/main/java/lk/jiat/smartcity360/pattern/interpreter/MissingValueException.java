package lk.jiat.smartcity360.pattern.interpreter;

public class MissingValueException extends PolicyEvaluationException {

    public MissingValueException(String variable) {
        super("There is no value for '" + variable + "' to compare");
    }
}