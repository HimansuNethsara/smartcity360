package lk.jiat.smartcity360.dto;

import jakarta.validation.constraints.*;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GenerateRequest {
    @NotNull @Min(1) @Max(20000)
    private Integer count;
    private Long zoneId;
}
