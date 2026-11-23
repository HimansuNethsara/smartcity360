package lk.jiat.smartcity360.arquillian;

import lk.jiat.smartcity360.security.JwtClaims;
import lk.jiat.smartcity360.security.JwtService;
import lk.jiat.smartcity360.security.LoginThrottle;
import lk.jiat.smartcity360.security.PasswordService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.naming.InitialContext;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("arquillian")
@ExtendWith(ArquillianExtension.class)
public class SecurityBeansArquillianIT {

    @Deployment
    public static WebArchive deployment() {
        return Deployments.war("sc360-security-it",
                "lk.jiat.smartcity360.security", "lk.jiat.smartcity360.service.exception");
    }

    private static <T> T bean(Class<T> type) throws Exception {
        return type.cast(new InitialContext().lookup("java:module/" + type.getSimpleName()));
    }

    @Test
    @DisplayName("The test really runs inside GlassFish")
    void runsInsideTheServer() {
        assertNotNull(System.getProperty("com.sun.aas.instanceRoot"), "expected to run inside GlassFish");
    }

    @Test
    @DisplayName("JwtService is created by the container and issues tokens that verify")
    void jwtServiceInContainer() throws Exception {
        JwtService jwt = bean(JwtService.class);
        String token = jwt.issue(9001L, "arquillian", List.of("STAFF"));
        JwtClaims claims = jwt.verify(token).orElseThrow();
        assertEquals(9001L, claims.userId());
        assertEquals("arquillian", claims.username());
        assertTrue(jwt.ttlSeconds() > 0);
    }

    @Test
    @DisplayName("Revoking a user in the container invalidates their older tokens")
    void revocationInContainer() throws Exception {
        JwtService jwt = bean(JwtService.class);
        String old = jwt.issue(9002L, "revoked", List.of("STAFF"));
        jwt.revokeUser(9002L);
        assertTrue(jwt.verify(old).isEmpty());
        assertTrue(jwt.verify(jwt.issue(9002L, "revoked", List.of("STAFF"))).isPresent());
    }

    @Test
    @DisplayName("PasswordService hashes the way the JDBC realm expects")
    void passwordServiceInContainer() throws Exception {
        PasswordService passwords = bean(PasswordService.class);
        assertEquals("5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8", passwords.hash("password"));
        assertTrue(passwords.matches("Secret123", passwords.hash("Secret123")));
    }

    @Test
    @DisplayName("LoginThrottle locks a user after repeated failures and is shared by every caller (singleton)")
    void throttleInContainer() throws Exception {
        LoginThrottle first = bean(LoginThrottle.class);
        LoginThrottle second = bean(LoginThrottle.class);
        String user = "arq-user-" + System.nanoTime();
        for (int i = 0; i < 5; i++) {
            first.failed(user);
        }
        assertTrue(second.secondsLocked(user) > 0, "the second reference must see the same state");
        second.succeeded(user);
        assertEquals(0, first.secondsLocked(user));
    }
}
