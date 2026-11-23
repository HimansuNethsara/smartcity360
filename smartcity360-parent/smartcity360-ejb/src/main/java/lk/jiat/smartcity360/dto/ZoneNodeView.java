package lk.jiat.smartcity360.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class ZoneNodeView {
    private final Long id;
    private final String code;
    private final String name;
    private final String type;
    private final String power;
    private final BigDecimal kwh;
    private final int meters;
    private final int buildings;
    private final List<ZoneNodeView> children;
}