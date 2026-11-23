package lk.jiat.smartcity360.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService service;

    @BeforeEach
    void start() {
        System.setProperty("sc360.jwt.secret", "unit-test-secret-unit-test-secret-1234");
        System.setProperty("sc360.jwt.ttl.minutes", "5");
        service = new JwtService();
        service.init();
    }

    @AfterEach
    void cleanProperties() {
        System.clearProperty("sc360.jwt.secret");
        System.clearProperty("sc360.jwt.ttl.minutes");
    }

    @Test
    @DisplayName("An issued token verifies, and the lifetime comes from the configured minutes")
    void issueAndVerify() {
        String token = service.issue(7L, "kamal", List.of("STAFF"));
        JwtClaims c = service.verify(token).orElseThrow();
        assertEquals(7L, c.userId());
        assertEquals("kamal", c.username());
        assertEquals(300, service.ttlSeconds());
    }

    @Test
    @DisplayName("Revoking a user invalidates their existing tokens but not new ones")
    void revokeInvalidatesOldTokens() {
        String old = service.issue(7L, "kamal", List.of("STAFF"));
        service.revokeUser(7L);
        assertTrue(service.verify(old).isEmpty());

        String fresh = service.issue(7L, "kamal", List.of("STAFF"));
        assertTrue(service.verify(fresh).isPresent());
        assertEquals(1, service.verify(fresh).orElseThrow().version());
    }

    @Test
    @DisplayName("Revoking one user leaves other users' tokens alone")
    void revokeIsPerUser() {
        String a = service.issue(1L, "a", List.of("STAFF"));
        String b = service.issue(2L, "b", List.of("STAFF"));
        service.revokeUser(1L);
        assertTrue(service.verify(a).isEmpty());
        assertTrue(service.verify(b).isPresent());
    }

    @Test
    @DisplayName("Garbage tokens are rejected by the service as well")
    void garbage() {
        assertTrue(service.verify("not.a.token").isEmpty());
        assertTrue(service.verify(null).isEmpty());
    }

    @Test
    @DisplayName("Without a configured secret the service still starts, using a random one")
    void randomSecretFallback() {
        System.clearProperty("sc360.jwt.secret");
        JwtService s = new JwtService();
        s.init();
        assertTrue(s.verify(s.issue(1L, "u", List.of("STAFF"))).isPresent());
        assertTrue(service.verify(s.issue(1L, "u", List.of("STAFF"))).isEmpty(),
                "a token from a differently-keyed service must not verify here");
    }
}
