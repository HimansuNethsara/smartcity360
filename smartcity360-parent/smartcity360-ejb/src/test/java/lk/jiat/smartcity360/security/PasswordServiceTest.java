package lk.jiat.smartcity360.security;

import lk.jiat.smartcity360.service.exception.BusinessRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class PasswordServiceTest {

    private final PasswordService passwords = new PasswordService();

    @Test
    @DisplayName("hash() is lowercase SHA-256 hex, the same format the JDBC realm compares")
    void knownHash() {
        assertEquals("5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8", passwords.hash("password"));
    }

    @Test
    @DisplayName("matches() accepts the right password and rejects others, ignoring hex case")
    void matching() {
        String stored = passwords.hash("Secret123");
        assertTrue(passwords.matches("Secret123", stored));
        assertTrue(passwords.matches("Secret123", stored.toUpperCase()));
        assertFalse(passwords.matches("secret123", stored));
        assertFalse(passwords.matches("", stored));
    }

    @Test
    @DisplayName("matches() is false, not an error, when either side is null")
    void nulls() {
        assertFalse(passwords.matches(null, "abc"));
        assertFalse(passwords.matches("abc", null));
    }

    @ParameterizedTest
    @CsvSource({"abcd1234", "Passw0rdOK", "a1a1a1a1"})
    @DisplayName("Acceptable passwords pass")
    void strongPasswords(String pw) {
        assertDoesNotThrow(() -> passwords.requireStrong(pw));
    }

    @ParameterizedTest
    @CsvSource({"abc12, at least 8", "abcdefgh, one letter and one digit", "12345678, one letter and one digit"})
    @DisplayName("Short passwords and passwords missing a letter or a digit are rejected with a reason")
    void weakPasswords(String pw, String reason) {
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> passwords.requireStrong(pw));
        assertTrue(ex.getMessage().contains(reason), ex.getMessage());
    }

    @Test
    @DisplayName("Null and over-long passwords are rejected")
    void nullAndTooLong() {
        assertThrows(BusinessRuleException.class, () -> passwords.requireStrong(null));
        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> passwords.requireStrong("a1".repeat(37)));
        assertTrue(ex.getMessage().contains("at most 72"));
        assertDoesNotThrow(() -> passwords.requireStrong("a1".repeat(36)));
    }
}
