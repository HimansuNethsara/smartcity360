package lk.jiat.smartcity360.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TypeCount {
    private final String typeCode;
    private final String model;
    private final String lampType;
    private final int wattage;
    private final long lights;
}
