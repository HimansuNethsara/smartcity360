package lk.jiat.smartcity360.pattern.composite;

import lk.jiat.smartcity360.domain.enums.ZoneType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ZoneTreeBuilder {

    private ZoneTreeBuilder() {
    }

    public static List<ZoneComponent> build(List<ZoneRow> rows, Map<Long, LeafUsage> usageByZone) {
        Map<Long, ZoneComponent> nodes = new HashMap<>();
        for (ZoneRow row : rows) {
            ZoneComponent node = row.type() == ZoneType.BUILDING
                    ? new ZoneLeaf(row, usageByZone.get(row.id()))
                    : new ZoneComposite(row);
            nodes.put(row.id(), node);
        }

        List<ZoneComponent> roots = new ArrayList<>();
        for (ZoneRow row : rows) {
            ZoneComponent node = nodes.get(row.id());
            ZoneComponent parent = row.parentId() == null ? null : nodes.get(row.parentId());
            if (parent == null) {
                roots.add(node);
            } else if (parent instanceof ZoneComposite composite) {
                composite.add(node);
            } else {
                throw new IllegalStateException("Building '" + parent.getName() + "' cannot contain zones");
            }
        }

        int reachable = 0;
        for (ZoneComponent root : roots) {
            ZoneStatsVisitor count = new ZoneStatsVisitor();
            root.accept(count);
            reachable += count.getTotal();
        }
        if (reachable != rows.size()) {
            throw new IllegalStateException("The zone data contains a loop (a zone is its own ancestor)");
        }
        return roots;
    }
}