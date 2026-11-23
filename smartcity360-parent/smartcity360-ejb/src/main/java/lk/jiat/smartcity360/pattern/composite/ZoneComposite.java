package lk.jiat.smartcity360.pattern.composite;

import lk.jiat.smartcity360.domain.enums.PowerStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ZoneComposite extends ZoneComponent {

    private final List<ZoneComponent> children = new ArrayList<>();

    public ZoneComposite(ZoneRow row) {
        super(row);
    }

    public void add(ZoneComponent child) {
        children.add(child);
        child.attachTo(this);
    }

    @Override
    protected int doShutdown() {
        int changed = switchTo(PowerStatus.OFF) ? 1 : 0;
        for (ZoneComponent child : children) {
            changed += child.doShutdown();
        }
        return changed;
    }

    @Override
    protected int doRestore() {
        int changed = switchTo(PowerStatus.ON) ? 1 : 0;
        for (ZoneComponent child : children) {
            changed += child.doRestore();
        }
        return changed;
    }

    @Override
    public UsageReport reportUsage() {
        List<UsageReport> parts = new ArrayList<>();
        BigDecimal kwh = BigDecimal.ZERO;
        int meters = 0;
        int buildings = 0;
        for (ZoneComponent child : children) {
            UsageReport r = child.reportUsage();
            parts.add(r);
            kwh = kwh.add(r.kwh());
            meters += r.meters();
            buildings += r.buildings();
        }
        if (isOff()) {
            kwh = BigDecimal.ZERO; // an off zone draws nothing, whatever its children claim
        }
        return new UsageReport(getId(), getCode(), getName(), getType(), getPower(),
                kwh, meters, buildings, parts);
    }

    @Override
    public List<ZoneComponent> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public void accept(ZoneVisitor visitor) {
        visitor.visit(this);
    }
}