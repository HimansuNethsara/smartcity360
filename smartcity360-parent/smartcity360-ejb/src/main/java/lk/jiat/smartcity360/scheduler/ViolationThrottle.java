package lk.jiat.smartcity360.scheduler;

import jakarta.ejb.ConcurrencyManagement;
import jakarta.ejb.ConcurrencyManagementType;
import jakarta.ejb.Singleton;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class ViolationThrottle {

    private static final Duration WINDOW = Duration.ofMinutes(10);

    private final ConcurrentHashMap<String, Instant> last = new ConcurrentHashMap<>();

    public boolean shouldNotify(String key) {
        Instant now = Instant.now();
        boolean[] allowed = {false};
        last.compute(key, (k, prev) -> {
            if (prev == null || prev.plus(WINDOW).isBefore(now)) {
                allowed[0] = true;
                return now;
            }
            return prev;
        });
        return allowed[0];
    }
}
