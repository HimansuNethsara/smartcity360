package lk.jiat.smartcity360.dto;

import lk.jiat.smartcity360.domain.enums.ComplaintStatus;
import jakarta.validation.constraints.*;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StatusRequest {
    @NotNull
    private ComplaintStatus status;
}
