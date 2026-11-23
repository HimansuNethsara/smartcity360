package lk.jiat.smartcity360.pattern.flyweight;

import lk.jiat.smartcity360.domain.enums.LampType;

public record LightTypeSpec(int id, String typeCode, String model, LampType lampType,
                            int wattage, int maxLumens, int colorTempK, String firmware) {
}
