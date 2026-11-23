package lk.jiat.smartcity360.pattern.decorator;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;

public class AuthenticationDecorator extends MeterStreamDecorator {

    private static final String HMAC = "HmacSHA256";
    private static final int TAG_LENGTH = 32;

    private final byte[] key;

    public AuthenticationDecorator(MeterStream wrapped, String meterCode, String config) {
        super(wrapped);
        if (config != null && !config.isBlank() && !"TOKEN".equalsIgnoreCase(config)) {
            throw new IllegalArgumentException("Unsupported authentication config '" + config + "' (expected TOKEN)");
        }
        this.key = StreamKeys.derive("auth", meterCode, 32);
    }

    @Override
    public byte[] send(byte[] payload) {
        byte[] data = super.send(payload);
        byte[] tag = hmac(data);
        byte[] out = Arrays.copyOf(data, data.length + TAG_LENGTH);
        System.arraycopy(tag, 0, out, data.length, TAG_LENGTH);
        return out;
    }

    @Override
    public byte[] receive(byte[] wire) {
        if (wire == null || wire.length < TAG_LENGTH) {
            throw new StreamProcessingException("Authentication failed: message too short to carry a tag");
        }
        byte[] data = Arrays.copyOf(wire, wire.length - TAG_LENGTH);
        byte[] received = Arrays.copyOfRange(wire, wire.length - TAG_LENGTH, wire.length);
        if (!MessageDigest.isEqual(hmac(data), received)) {
            throw new StreamProcessingException("Authentication failed: tag does not match (data altered or wrong meter)");
        }
        return super.receive(data);
    }

    @Override
    public String describe() {
        return super.describe() + ">Auth";
    }

    private byte[] hmac(byte[] data) {
        try {
            Mac mac = Mac.getInstance(HMAC);
            mac.init(new SecretKeySpec(key, HMAC));
            return mac.doFinal(data);
        } catch (GeneralSecurityException e) {
            throw new StreamProcessingException("Could not compute authentication tag", e);
        }
    }
}
