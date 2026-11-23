package lk.jiat.smartcity360.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class UserUpdateRequest {
    @Size(max = 100)
    private String fullName;
    @Email @Size(max = 120)
    private String email;
    @Size(max = 20)
    private String phone;
    private Boolean enabled;
    private List<String> roles;
}