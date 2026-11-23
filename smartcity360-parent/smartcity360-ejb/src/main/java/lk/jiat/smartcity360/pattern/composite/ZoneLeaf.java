package lk.jiat.smartcity360.pattern.composite;

import lk.jiat.smartcity360.domain.enums.PowerStatus;

import java.math.BigDecimal;
import java.util.List;

public class ZoneLeaf extends ZoneComponent {

    private final LeafUsage usage;

    public ZoneLeaf(ZoneRow row, LeafUsage usage) {
        super(row);
        this.usage = usage == null ? LeafUsage.NONE : usage;
    }

    @Override
    protected int doShutdown() {
        return switchTo(PowerStatus.OFF) ? 1 : 0;
    }

    @Override
    protected int doRestore() {
        return switchTo(PowerStatus.ON) ? 1 : 0;
    }

    @Override
    public UsageReport reportUsage() {
        BigDecimal kwh = isOff() ? BigDecimal.ZERO : usage.kwh();
        return new UsageReport(getId(), getCode(), getName(), getType(), getPower(),
                kwh, usage.meters(), 1, List.of());
    }

    @Override
    public List<ZoneComponent> getChildren() {
        return List.of();
    }

    @Override
    public void accept(ZoneVisitor visitor) {
        visitor.visit(this);
    }
}