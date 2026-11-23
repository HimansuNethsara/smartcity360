package lk.jiat.smartcity360.pattern.decorator;

public class RawMeterStream implements MeterStream {

    @Override
    public byte[] send(byte[] payload) {
        return payload;
    }

    @Override
    public byte[] receive(byte[] wire) {
        return wire;
    }

    @Override
    public String describe() {
        return "Raw";
    }
}
