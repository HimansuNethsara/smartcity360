package lk.jiat.smartcity360.pattern.composite;

import java.util.ArrayList;
import java.util.List;

public class ChangedZonesVisitor extends TraversingVisitor {

    private final List<ZoneComponent> changed = new ArrayList<>();

    @Override
    protected void onNode(ZoneComponent node, int depth) {
        if (node.isChanged()) {
            changed.add(node);
        }
    }

    public List<ZoneComponent> getChanged() { return changed; }
}