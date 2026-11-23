package lk.jiat.smartcity360.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GenerateResult {
    private final int created;
    private final int streetZonesUsed;
    private final long totalLights;
}
