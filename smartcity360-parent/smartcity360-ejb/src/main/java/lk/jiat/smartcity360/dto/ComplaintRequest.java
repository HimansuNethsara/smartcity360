package lk.jiat.smartcity360.dto;

import lk.jiat.smartcity360.domain.enums.ComplaintCategory;
import lk.jiat.smartcity360.domain.enums.ComplaintSeverity;
import jakarta.validation.constraints.*;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ComplaintRequest {
    private Long citizenId;
    private Long zoneId;
    private Long streetlightId;
    @NotNull
    private ComplaintCategory category;
    @NotBlank @Size(max = 150)
    private String title;
    @NotBlank @Size(max = 2000)
    private String description;
    @NotNull
    private ComplaintSeverity severity;
}
