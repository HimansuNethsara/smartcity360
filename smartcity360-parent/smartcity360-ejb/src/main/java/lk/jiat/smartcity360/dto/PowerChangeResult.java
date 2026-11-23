package lk.jiat.smartcity360.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class PowerChangeResult {
    private final Long zoneId;
    private final String zoneName;
    private final String action;
    private final int zonesChanged;
    private final List<ChangedZone> changed;
    private final BigDecimal kwhBefore;
    private final BigDecimal kwhAfter;
}