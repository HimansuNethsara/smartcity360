package lk.jiat.smartcity360.security;

import java.util.List;

public record JwtClaims(long userId, String username, List<String> roles, int version, long issuedAt, long expiresAt) {

    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}
