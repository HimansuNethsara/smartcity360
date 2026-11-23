package lk.jiat.smartcity360.security;

import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

@Singleton
@Lock(LockType.WRITE)
public class LoginThrottle {

    static final int MAX_FAILURES = 5;
    static final long LOCK_SECONDS = 60;

    private static final class Entry {
        int failures;
        long lockedUntil;
    }

    private final Map<String, Entry> entries = new HashMap<>();
    private final Clock clock = Clock.systemUTC();

    /** Seconds still locked, or 0 when the username may try. */
    public long secondsLocked(String username) {
        Entry e = entries.get(key(username));
        if (e == null) {
            return 0;
        }
        return Math.max(0, e.lockedUntil - now());
    }

    public void failed(String username) {
        if (entries.size() > 5000) {
            entries.values().removeIf(x -> x.lockedUntil < now());
        }
        Entry e = entries.computeIfAbsent(key(username), k -> new Entry());
        if (e.lockedUntil != 0 && e.lockedUntil <= now()) {   // an old lock has run out: start counting again
            e.failures = 0;
            e.lockedUntil = 0;
        }
        e.failures++;
        if (e.failures >= MAX_FAILURES) {
            e.lockedUntil = now() + LOCK_SECONDS;
            e.failures = 0;
        }
    }

    public void succeeded(String username) {
        entries.remove(key(username));
    }

    private long now() {
        return clock.instant().getEpochSecond();
    }

    private static String key(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }
}
