package lk.jiat.smartcity360.pattern.composite;

import java.math.BigDecimal;

public record LeafUsage(BigDecimal kwh, int meters) {

    public static final LeafUsage NONE = new LeafUsage(BigDecimal.ZERO, 0);
}