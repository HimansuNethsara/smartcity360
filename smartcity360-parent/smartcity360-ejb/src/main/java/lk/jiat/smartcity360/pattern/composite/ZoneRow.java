package lk.jiat.smartcity360.pattern.composite;

import lk.jiat.smartcity360.domain.enums.LandUse;
import lk.jiat.smartcity360.domain.enums.PowerStatus;
import lk.jiat.smartcity360.domain.enums.ZoneType;

public record ZoneRow(Long id, Long parentId, ZoneType type, String code, String name,
                      LandUse landUse, PowerStatus power) {
}