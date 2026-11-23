package lk.jiat.smartcity360.pattern.decorator;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;

public class EncryptionDecorator extends MeterStreamDecorator {

    private static final int IV_LENGTH = 12;
    private static final int GCM_TAG_BITS = 128;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final byte[] key;

    public EncryptionDecorator(MeterStream wrapped, String meterCode, String config) {
        super(wrapped);
        int keyBytes;
        if (config == null || config.isBlank() || "AES-128".equalsIgnoreCase(config)) {
            keyBytes = 16;
        } else if ("AES-256".equalsIgnoreCase(config)) {
            keyBytes = 32;
        } else {
            throw new IllegalArgumentException("Unsupported encryption config '" + config + "' (expected AES-128 or AES-256)");
        }
        this.key = StreamKeys.derive("enc", meterCode, keyBytes);
    }

    @Override
    public byte[] send(byte[] payload) {
        byte[] data = super.send(payload);
        try {
            byte[] iv = new byte[IV_LENGTH];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(data);
            byte[] out = Arrays.copyOf(iv, IV_LENGTH + encrypted.length);
            System.arraycopy(encrypted, 0, out, IV_LENGTH, encrypted.length);
            return out;
        } catch (GeneralSecurityException e) {
            throw new StreamProcessingException("Encryption failed", e);
        }
    }

    @Override
    public byte[] receive(byte[] wire) {
        if (wire == null || wire.length < IV_LENGTH + 16) {
            throw new StreamProcessingException("Decryption failed: message too short");
        }
        try {
            byte[] iv = Arrays.copyOf(wire, IV_LENGTH);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] data = cipher.doFinal(wire, IV_LENGTH, wire.length - IV_LENGTH);
            return super.receive(data);
        } catch (GeneralSecurityException e) {
            throw new StreamProcessingException("Decryption failed: data altered or wrong key", e);
        }
    }

    @Override
    public String describe() {
        return super.describe() + ">Encrypt";
    }
}
