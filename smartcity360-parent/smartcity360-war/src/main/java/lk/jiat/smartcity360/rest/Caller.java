package lk.jiat.smartcity360.rest;

import jakarta.ws.rs.core.SecurityContext;

public final class Caller {

    private Caller() {
    }

    public static AuthenticatedUser of(SecurityContext security) {
        return (AuthenticatedUser) security.getUserPrincipal();
    }

    public static boolean isStaff(AuthenticatedUser user) {
        return user.hasAnyRole("ADMIN", "CITY_MANAGER", "SUPERVISOR", "STAFF");
    }
}
