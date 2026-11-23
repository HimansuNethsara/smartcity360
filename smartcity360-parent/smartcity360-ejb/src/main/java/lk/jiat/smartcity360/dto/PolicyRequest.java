package lk.jiat.smartcity360.dto;

import jakarta.validation.constraints.*;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PolicyRequest {
    @NotBlank @Size(max = 100)
    private String name;
    @Size(max = 255)
    private String description;
    @NotBlank @Size(max = 500)
    private String dslText;
    private Boolean enabled;
    private Long actorId;
    private Integer version;
}
