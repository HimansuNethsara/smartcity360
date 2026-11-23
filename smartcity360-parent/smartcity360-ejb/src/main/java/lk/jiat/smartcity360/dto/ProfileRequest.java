package lk.jiat.smartcity360.dto;

import jakarta.validation.constraints.*;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProfileRequest {
    @NotBlank @Size(max = 100)
    private String fullName;
    @NotBlank @Email @Size(max = 120)
    private String email;
    @Size(max = 20)
    private String phone;
}
