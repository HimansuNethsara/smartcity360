package lk.jiat.smartcity360.pattern.composite;

import lk.jiat.smartcity360.domain.enums.LandUse;
import lk.jiat.smartcity360.domain.enums.PowerStatus;
import lk.jiat.smartcity360.domain.enums.ZoneType;

import java.util.List;

public abstract class ZoneComponent {

    private final Long id;
    private final ZoneType type;
    private final String code;
    private final String name;
    private final LandUse landUse;
    private final PowerStatus originalPower;
    private PowerStatus power;
    private ZoneComponent parent;

    protected ZoneComponent(ZoneRow row) {
        this.id = row.id();
        this.type = row.type();
        this.code = row.code();
        this.name = row.name();
        this.landUse = row.landUse();
        this.power = row.power();
        this.originalPower = row.power();
    }

    // ---- the Composite operations -------------------------------------------------

    public final int shutdownPower() {
        return doShutdown();
    }

    public final int restorePower() {
        if (parent != null && parent.isOff()) {
            throw new IllegalStateException("Cannot restore '" + name + "' while its parent '"
                    + parent.name + "' is powered off; restore the parent instead");
        }
        return doRestore();
    }

    public abstract UsageReport reportUsage();

    public abstract List<ZoneComponent> getChildren();

    public abstract void accept(ZoneVisitor visitor);

    protected abstract int doShutdown();

    protected abstract int doRestore();

    // ---- shared state ---------------------------------------------------------------

    protected final boolean switchTo(PowerStatus target) {
        if (power == target) {
            return false;
        }
        power = target;
        return true;
    }

    public final ZoneComponent findById(Long zoneId) {
        if (id != null && id.equals(zoneId)) {
            return this;
        }
        for (ZoneComponent child : getChildren()) {
            ZoneComponent hit = child.findById(zoneId);
            if (hit != null) {
                return hit;
            }
        }
        return null;
    }

    final void attachTo(ZoneComponent newParent) {
        this.parent = newParent;
    }

    public final boolean isOff() {
        return power == PowerStatus.OFF;
    }

    public final boolean isChanged() {
        return power != originalPower;
    }

    public final Long getId() { return id; }
    public final ZoneType getType() { return type; }
    public final String getCode() { return code; }
    public final String getName() { return name; }
    public final LandUse getLandUse() { return landUse; }
    public final PowerStatus getPower() { return power; }
    public final ZoneComponent getParent() { return parent; }
}