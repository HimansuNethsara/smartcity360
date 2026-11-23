package lk.jiat.smartcity360.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class StreetlightSummary {
    private final long totalLights;
    private final long totalWatts;
    private final long totalLumens;
    private final Map<String, Long> byStatus;
    private final List<TypeCount> byType;
    private final PoolStats pool;
}
