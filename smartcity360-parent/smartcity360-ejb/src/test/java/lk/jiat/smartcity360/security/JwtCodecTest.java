package lk.jiat.smartcity360.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JwtCodecTest {

    private static final byte[] SECRET = "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8);
    private static final Instant T0 = Instant.parse("2026-11-05T10:00:00Z");
    private static final long TTL = 3600;

    private static Clock at(Instant instant) {
        return Clock.fixed(instant, ZoneOffset.UTC);
    }

    private static JwtCodec codec(Instant now) {
        return new JwtCodec(SECRET, TTL, at(now));
    }

    private final JwtCodec codec = codec(T0);

    @Test
    @DisplayName("A token verifies and carries the user, roles, version and times it was issued with")
    void roundTrip() {
        String token = codec.issue(42L, "nimal", List.of("ADMIN", "CITY_MANAGER"), 3);
        JwtClaims c = codec.verify(token).orElseThrow();
        assertEquals(42L, c.userId());
        assertEquals("nimal", c.username());
        assertEquals(List.of("ADMIN", "CITY_MANAGER"), c.roles());
        assertEquals(3, c.version());
        assertEquals(T0.getEpochSecond(), c.issuedAt());
        assertEquals(T0.getEpochSecond() + TTL, c.expiresAt());
        assertTrue(c.hasRole("ADMIN"));
        assertFalse(c.hasRole("STAFF"));
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    @DisplayName("A user with no roles still round-trips")
    void noRoles() {
        JwtClaims c = codec.verify(codec.issue(1L, "u", List.of(), 0)).orElseThrow();
        assertTrue(c.roles().isEmpty());
    }

    @Test
    @DisplayName("Usernames with quotes, backslashes, control characters and non-ASCII text survive")
    void awkwardUsernames() {
        for (String name : List.of("o\"brien", "back\\slash", "tab\there", "line\nbreak", "සුනිල්", "a.b-c_d")) {
            Optional<JwtClaims> c = codec.verify(codec.issue(5L, name, List.of("STAFF"), 0));
            assertEquals(name, c.orElseThrow().username(), "username " + name);
        }
    }

    @Test
    @DisplayName("A token is valid up to its expiry second and rejected from then on")
    void expiry() {
        String token = codec.issue(1L, "u", List.of("STAFF"), 0);
        assertTrue(codec(T0.plusSeconds(TTL - 1)).verify(token).isPresent());
        assertTrue(codec(T0.plusSeconds(TTL)).verify(token).isEmpty());
        assertTrue(codec(T0.plusSeconds(TTL + 1000)).verify(token).isEmpty());
    }

    @Test
    @DisplayName("A token signed with a different secret is rejected")
    void wrongSecret() {
        String token = codec.issue(1L, "u", List.of("ADMIN"), 0);
        JwtCodec other = new JwtCodec("ffffffffffffffffffffffffffffffff".getBytes(StandardCharsets.UTF_8), TTL, at(T0));
        assertTrue(other.verify(token).isEmpty());
    }

    @Test
    @DisplayName("Changing the payload (for example to add a role) breaks the signature")
    void tamperedPayload() {
        String[] parts = codec.issue(1L, "u", List.of("STAFF"), 0).split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8)
                .replace("\"STAFF\"", "\"ADMIN\"");
        String forged = parts[0] + "." + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8)) + "." + parts[2];
        assertTrue(codec.verify(forged).isEmpty());
    }

    @Test
    @DisplayName("A damaged signature or header is rejected")
    void tamperedSignatureAndHeader() {
        String token = codec.issue(1L, "u", List.of("STAFF"), 0);
        String[] p = token.split("\\.");
        String badSig = p[0] + "." + p[1] + "." + (p[2].charAt(0) == 'A' ? "B" : "A") + p[2].substring(1);
        assertTrue(codec.verify(badSig).isEmpty());
        assertTrue(codec.verify("eyJhbGciOiJub25lIn0." + p[1] + ".").isEmpty(), "alg=none style token");
    }

    @Test
    @DisplayName("Garbage input never throws, it is simply rejected")
    void garbage() {
        assertTrue(codec.verify(null).isEmpty());
        assertTrue(codec.verify("").isEmpty());
        assertTrue(codec.verify("abc").isEmpty());
        assertTrue(codec.verify("a.b.c").isEmpty());
        assertTrue(codec.verify("a.b.c.d").isEmpty());
        assertTrue(codec.verify("!!!.@@@.###").isEmpty());
        assertTrue(codec.verify("x".repeat(5000)).isEmpty());
    }

    @Test
    @DisplayName("Weak secrets and bad role names are refused")
    void configurationErrors() {
        assertThrows(IllegalArgumentException.class, () -> new JwtCodec(new byte[10], TTL, at(T0)));
        assertThrows(IllegalArgumentException.class, () -> new JwtCodec(null, TTL, at(T0)));
        assertThrows(IllegalArgumentException.class, () -> codec.issue(1L, "u", List.of("admin"), 0));
        assertThrows(IllegalArgumentException.class, () -> codec.issue(1L, "u", List.of("A\",\"B"), 0));
    }

    @Test
    @DisplayName("The codec reports its lifetime")
    void ttl() {
        assertEquals(TTL, codec.ttlSeconds());
    }
}
