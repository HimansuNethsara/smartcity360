package lk.jiat.smartcity360.dto;

import jakarta.validation.constraints.*;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginRequest {
    @NotBlank @Size(max = 50)
    private String username;
    @NotBlank @Size(max = 72)
    private String password;
}
