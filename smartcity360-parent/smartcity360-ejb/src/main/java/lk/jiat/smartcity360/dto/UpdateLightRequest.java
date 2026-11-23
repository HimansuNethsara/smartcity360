package lk.jiat.smartcity360.dto;

import lk.jiat.smartcity360.domain.enums.StreetlightStatus;
import jakarta.validation.constraints.*;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateLightRequest {
    private StreetlightStatus status;
    @Min(0) @Max(100)
    private Integer brightness;
}
