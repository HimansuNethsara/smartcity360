package lk.jiat.smartcity360.pattern.interpreter;

public final class FineAction implements PolicyAction {

    private final long amount;

    public FineAction(long amount) {
        this.amount = amount;
    }

    public long amount() {
        return amount;
    }

    @Override
    public String kind() {
        return "FINE";
    }

    @Override
    public String result() {
        return "FINE " + amount;
    }
}
