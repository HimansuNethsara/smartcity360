package lk.jiat.smartcity360.rest;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import lk.jiat.smartcity360.security.JwtClaims;
import lk.jiat.smartcity360.security.JwtService;

import java.security.Principal;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthFilter implements ContainerRequestFilter {

    public static final String COOKIE = "sc360_token";

    @Inject
    private JwtService jwtService;

    @Override
    public void filter(ContainerRequestContext ctx) {
        String token = null;
        String header = ctx.getHeaderString("Authorization");
        if (header != null && header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            token = header.substring(7).trim();
        } else {
            Cookie c = ctx.getCookies().get(COOKIE);
            if (c != null) {
                token = c.getValue();
            }
        }
        if (token == null || token.isEmpty()) {
            return;
        }
        JwtClaims claims = jwtService.verify(token).orElse(null);
        if (claims == null) {
            return;
        }
        AuthenticatedUser user = new AuthenticatedUser(claims.userId(), claims.username(), claims.roles());
        SecurityContext original = ctx.getSecurityContext();
        ctx.setSecurityContext(new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return user;
            }

            @Override
            public boolean isUserInRole(String role) {
                return user.hasRole(role);
            }

            @Override
            public boolean isSecure() {
                return original != null && original.isSecure();
            }

            @Override
            public String getAuthenticationScheme() {
                return "JWT";
            }
        });
    }
}
