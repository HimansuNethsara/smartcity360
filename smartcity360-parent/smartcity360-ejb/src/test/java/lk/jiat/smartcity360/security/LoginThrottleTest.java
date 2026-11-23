package lk.jiat.smartcity360.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginThrottleTest {

    private final LoginThrottle throttle = new LoginThrottle();

    private void fail(String user, int times) {
        for (int i = 0; i < times; i++) {
            throttle.failed(user);
        }
    }

    @Test
    @DisplayName("An unknown user is not locked")
    void unknownUser() {
        assertEquals(0, throttle.secondsLocked("nobody"));
    }

    @Test
    @DisplayName("Fewer than the maximum failures do not lock the account")
    void belowLimit() {
        fail("nimal", LoginThrottle.MAX_FAILURES - 1);
        assertEquals(0, throttle.secondsLocked("nimal"));
    }

    @Test
    @DisplayName("Reaching the maximum failures locks the account for up to the lock time")
    void locksAtLimit() {
        fail("nimal", LoginThrottle.MAX_FAILURES);
        long left = throttle.secondsLocked("nimal");
        assertTrue(left > 0 && left <= LoginThrottle.LOCK_SECONDS, "locked for " + left + "s");
    }

    @Test
    @DisplayName("A successful login clears the failure count")
    void successClears() {
        fail("nimal", LoginThrottle.MAX_FAILURES - 1);
        throttle.succeeded("nimal");
        fail("nimal", LoginThrottle.MAX_FAILURES - 1);
        assertEquals(0, throttle.secondsLocked("nimal"));
    }

    @Test
    @DisplayName("Success after a lock removes the lock")
    void successRemovesLock() {
        fail("nimal", LoginThrottle.MAX_FAILURES);
        throttle.succeeded("nimal");
        assertEquals(0, throttle.secondsLocked("nimal"));
    }

    @Test
    @DisplayName("Usernames are matched ignoring case and surrounding spaces")
    void keyIsNormalised() {
        fail("Nimal", 3);
        fail("  NIMAL ", LoginThrottle.MAX_FAILURES - 3);
        assertTrue(throttle.secondsLocked("nimal") > 0);
    }

    @Test
    @DisplayName("One user's failures do not lock another user")
    void usersAreIndependent() {
        fail("a", LoginThrottle.MAX_FAILURES);
        assertTrue(throttle.secondsLocked("a") > 0);
        assertEquals(0, throttle.secondsLocked("b"));
    }

    @Test
    @DisplayName("A null username is handled without error")
    void nullUser() {
        assertDoesNotThrow(() -> throttle.failed(null));
        assertEquals(0, throttle.secondsLocked(null));
    }
}
