package lk.jiat.smartcity360.pattern.decorator;

import java.util.Objects;

public abstract class MeterStreamDecorator implements MeterStream {

    protected final MeterStream wrapped;

    protected MeterStreamDecorator(MeterStream wrapped) {
        this.wrapped = Objects.requireNonNull(wrapped, "wrapped stream");
    }

    @Override
    public byte[] send(byte[] payload) {
        return wrapped.send(payload);
    }

    @Override
    public byte[] receive(byte[] wire) {
        return wrapped.receive(wire);
    }

    @Override
    public String describe() {
        return wrapped.describe();
    }
}
