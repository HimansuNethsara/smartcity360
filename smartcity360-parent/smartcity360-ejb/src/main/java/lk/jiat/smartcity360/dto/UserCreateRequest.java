package lk.jiat.smartcity360.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class UserCreateRequest {
    @NotBlank @Pattern(regexp = "[A-Za-z0-9._-]{3,30}", message = "must be 3-30 letters, digits, dot, dash or underscore")
    private String username;
    @NotBlank @Size(max = 72)
    private String password;
    @NotBlank @Size(max = 100)
    private String fullName;
    @NotBlank @Email @Size(max = 120)
    private String email;
    @Size(max = 20)
    private String phone;
    @NotEmpty
    private List<String> roles;
}