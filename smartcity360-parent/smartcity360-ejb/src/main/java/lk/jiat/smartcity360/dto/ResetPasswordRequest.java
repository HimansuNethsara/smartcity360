package lk.jiat.smartcity360.dto;

import jakarta.validation.constraints.*;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResetPasswordRequest {
    @NotBlank @Size(max = 72)
    private String newPassword;
}
