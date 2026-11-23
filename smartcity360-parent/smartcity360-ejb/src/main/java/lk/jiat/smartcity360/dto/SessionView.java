package lk.jiat.smartcity360.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SessionView {
    private final Long id;
    private final String username;
    private final String fullName;
    private final String email;
    private final String phone;
    private final List<String> roles;
    private final String token;

    public SessionView withToken(String newToken) {
        return new SessionView(id, username, fullName, email, phone, roles, newToken);
    }
}
