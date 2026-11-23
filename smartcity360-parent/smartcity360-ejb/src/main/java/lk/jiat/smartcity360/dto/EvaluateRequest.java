package lk.jiat.smartcity360.dto;

import jakarta.validation.constraints.*;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EvaluateRequest {
    private Long zoneId;
    private Double noise;
    private Double temperature;
    private Double airQuality;
    @Min(0) @Max(23)
    private Integer hour;
    @Size(max = 50)
    private String zone;
}
