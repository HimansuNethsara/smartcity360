package lk.jiat.smartcity360.rest;

import java.security.Principal;
import java.util.List;

public record AuthenticatedUser(long id, String name, List<String> roles) implements Principal {

    @Override
    public String getName() {
        return name;
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public boolean hasAnyRole(String... wanted) {
        for (String r : wanted) {
            if (roles.contains(r)) {
                return true;
            }
        }
        return false;
    }
}
