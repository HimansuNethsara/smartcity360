package lk.jiat.smartcity360.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JwtCodec {

    private static final Base64.Encoder ENC = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DEC = Base64.getUrlDecoder();
    private static final String HEADER = ENC.encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
    private static final Pattern PAYLOAD = Pattern.compile(
            "\\{\"sub\":\"(\\d{1,18})\",\"usr\":\"((?:[^\"\\\\]|\\\\.)*)\","
                    + "\"roles\":\\[((?:\"[A-Z_]+\"(?:,\"[A-Z_]+\")*)?)],\"ver\":(\\d{1,9}),\"iat\":(\\d{1,12}),\"exp\":(\\d{1,12})}");
    private static final int MAX_TOKEN_LENGTH = 4096;

    private final byte[] secret;
    private final long ttlSeconds;
    private final Clock clock;

    public JwtCodec(byte[] secret, long ttlSeconds, Clock clock) {
        if (secret == null || secret.length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 bytes");
        }
        this.secret = secret.clone();
        this.ttlSeconds = ttlSeconds;
        this.clock = clock;
    }

    public long ttlSeconds() {
        return ttlSeconds;
    }

    public String issue(long userId, String username, Collection<String> roles, int version) {
        long now = clock.instant().getEpochSecond();
        StringBuilder r = new StringBuilder();
        for (String role : roles) {
            if (!role.matches("[A-Z_]+")) {
                throw new IllegalArgumentException("Bad role name: " + role);
            }
            if (r.length() > 0) {
                r.append(',');
            }
            r.append('"').append(role).append('"');
        }
        String payload = "{\"sub\":\"" + userId + "\",\"usr\":\"" + escape(username) + "\",\"roles\":[" + r
                + "],\"ver\":" + version + ",\"iat\":" + now + ",\"exp\":" + (now + ttlSeconds) + "}";
        String unsigned = HEADER + "." + ENC.encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return unsigned + "." + ENC.encodeToString(sign(unsigned));
    }

    public Optional<JwtClaims> verify(String token) {
        try {
            if (token == null || token.length() > MAX_TOKEN_LENGTH) {
                return Optional.empty();
            }
            String[] parts = token.split("\\.", -1);
            if (parts.length != 3 || !HEADER.equals(parts[0])) {
                return Optional.empty();
            }
            byte[] given = DEC.decode(parts[2]);
            if (!MessageDigest.isEqual(sign(parts[0] + "." + parts[1]), given)) {
                return Optional.empty();
            }
            String payload = new String(DEC.decode(parts[1]), StandardCharsets.UTF_8);
            Matcher m = PAYLOAD.matcher(payload);
            if (!m.matches()) {
                return Optional.empty();
            }
            long exp = Long.parseLong(m.group(6));
            if (exp <= clock.instant().getEpochSecond()) {
                return Optional.empty();
            }
            List<String> roles = m.group(3).isEmpty() ? List.of()
                    : List.of(m.group(3).replace("\"", "").split(","));
            return Optional.of(new JwtClaims(Long.parseLong(m.group(1)), unescape(m.group(2)), roles,
                    Integer.parseInt(m.group(4)), Long.parseLong(m.group(5)), exp));
        } catch (RuntimeException e) {          // bad base64, bad number, ...
            return Optional.empty();
        }
    }

    private byte[] sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HmacSHA256 is not available", e);
        }
    }

    private static String escape(String s) {
        StringBuilder b = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == '"' || c == '\\') {
                b.append('\\').append(c);
            } else if (c < 0x20) {
                b.append(String.format("\\u%04x", (int) c));
            } else {
                b.append(c);
            }
        }
        return b.toString();
    }

    private static String unescape(String s) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c != '\\') {
                b.append(c);
            } else if (s.charAt(i + 1) == 'u') {
                b.append((char) Integer.parseInt(s.substring(i + 2, i + 6), 16));
                i += 5;
            } else {
                b.append(s.charAt(++i));
            }
        }
        return b.toString();
    }
}
