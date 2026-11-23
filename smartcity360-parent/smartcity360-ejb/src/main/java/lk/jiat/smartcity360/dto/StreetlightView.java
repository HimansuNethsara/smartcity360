package lk.jiat.smartcity360.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class StreetlightView {
    private final Long id;
    private final String typeCode;
    private final String model;
    private final String lampType;
    private final int wattage;
    private final Long zoneId;
    private final String zoneName;
    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final String status;
    private final int brightness;
    private final int powerWatts;
    private final int lumens;
}
