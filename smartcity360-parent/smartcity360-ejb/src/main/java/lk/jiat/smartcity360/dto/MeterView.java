package lk.jiat.smartcity360.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class MeterView {
    private final Long id;
    private final String meterCode;
    private final Long zoneId;
    private final String zoneName;
    private final String zoneCode;
    private final String status;
    private final LocalDate installedAt;
    private final String chain;
    private final List<LayerView> layers;
}