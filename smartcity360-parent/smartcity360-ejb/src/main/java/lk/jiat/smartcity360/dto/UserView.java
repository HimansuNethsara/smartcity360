package lk.jiat.smartcity360.dto;

import java.util.List;

public record UserView(Long id, String username, String fullName, String email, String phone,
                       Boolean enabled, List<String> roles, String createdAt) {
}