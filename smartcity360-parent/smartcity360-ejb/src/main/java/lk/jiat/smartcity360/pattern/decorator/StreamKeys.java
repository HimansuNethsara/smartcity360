package lk.jiat.smartcity360.pattern.decorator;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

final class StreamKeys {

    private static final String SECRET =
            System.getProperty("sc360.stream.secret", "sc360-demo-secret-change-me");

    private StreamKeys() {
    }

    /** @param lengthBytes 16 for AES-128, 32 for AES-256 or an HMAC key (max 32) */
    static byte[] derive(String purpose, String meterCode, int lengthBytes) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] digest = sha.digest((purpose + ":" + meterCode + ":" + SECRET).getBytes(StandardCharsets.UTF_8));
            return Arrays.copyOf(digest, lengthBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new StreamProcessingException("SHA-256 is not available", e);
        }
    }
}
