package lk.jiat.smartcity360.pattern.composite;

import lk.jiat.smartcity360.domain.enums.PowerStatus;
import lk.jiat.smartcity360.domain.enums.ZoneType;

import java.math.BigDecimal;
import java.util.List;

public record UsageReport(Long zoneId, String code, String name, ZoneType type, PowerStatus power,
                          BigDecimal kwh, int meters, int buildings, List<UsageReport> children) {
}