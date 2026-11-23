package lk.jiat.smartcity360.pattern.interpreter;

public final class AlertAction implements PolicyAction {

    private final String message;

    public AlertAction(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }

    @Override
    public String kind() {
        return "ALERT";
    }

    @Override
    public String result() {
        return "ALERT \"" + message + "\"";
    }
}
