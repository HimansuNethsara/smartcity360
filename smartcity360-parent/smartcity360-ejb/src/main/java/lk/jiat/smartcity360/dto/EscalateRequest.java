package lk.jiat.smartcity360.dto;

import jakarta.validation.constraints.*;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EscalateRequest {
    @Size(max = 255)
    private String note;
}
