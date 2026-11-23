package lk.jiat.smartcity360.pattern.decorator;

public interface MeterStream {

    byte[] send(byte[] payload);

    byte[] receive(byte[] wire);

    String describe();
}
