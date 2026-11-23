package lk.jiat.smartcity360.security;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;

import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Singleton
@Lock(LockType.READ)
public class JwtService {

    private static final Logger LOG = Logger.getLogger(JwtService.class.getName());

    private JwtCodec codec;
    private final Map<Long, Integer> tokenVersions = new ConcurrentHashMap<>();

    @PostConstruct
    void init() {
        String secret = System.getenv("SC360_JWT_SECRET");
        if (secret == null || secret.isBlank()) {
            secret = System.getProperty("sc360.jwt.secret");
        }
        byte[] key;
        if (secret != null && secret.length() >= 32) {
            key = secret.getBytes(StandardCharsets.UTF_8);
        } else {
            key = new byte[48];
            new SecureRandom().nextBytes(key);
            LOG.warning("SC360_JWT_SECRET is not set (or shorter than 32 characters): using a random secret. "
                    + "Logins will not survive a redeploy.");
        }
        long minutes = Long.getLong("sc360.jwt.ttl.minutes", 120L);
        codec = new JwtCodec(key, minutes * 60, Clock.systemUTC());
    }

    public String issue(long userId, String username, Collection<String> roles) {
        return codec.issue(userId, username, roles, tokenVersions.getOrDefault(userId, 0));
    }

    public long ttlSeconds() {
        return codec.ttlSeconds();
    }

    public Optional<JwtClaims> verify(String token) {
        return codec.verify(token)
                .filter(c -> c.version() == tokenVersions.getOrDefault(c.userId(), 0));
    }

    public void revokeUser(long userId) {
        tokenVersions.merge(userId, 1, Integer::sum);
    }
}
