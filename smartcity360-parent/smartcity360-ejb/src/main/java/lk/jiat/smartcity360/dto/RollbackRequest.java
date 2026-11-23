package lk.jiat.smartcity360.dto;

import jakarta.validation.constraints.*;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RollbackRequest {
    @NotNull @Min(1)
    private Integer versionNo;
    private Long actorId;
}
