package lk.jiat.smartcity360.pattern.interpreter;

public class PolicyParseException extends RuntimeException {

    private final int position;

    public PolicyParseException(String message, int position) {
        super(message);
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
