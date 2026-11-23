package lk.jiat.smartcity360.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MeterRequest {
    @NotBlank @Size(max = 30)
    private String meterCode;
    @NotNull
    private Long zoneId;
    private LocalDate installedAt;
}
