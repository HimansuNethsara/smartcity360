package lk.jiat.smartcity360.pattern.composite;

import lk.jiat.smartcity360.domain.enums.ZoneType;

import java.util.EnumMap;
import java.util.Map;

public class ZoneStatsVisitor extends TraversingVisitor {

    private final Map<ZoneType, Integer> perType = new EnumMap<>(ZoneType.class);
    private int total;
    private int off;
    private int levels;

    @Override
    protected void onNode(ZoneComponent node, int depth) {
        total++;
        perType.merge(node.getType(), 1, Integer::sum);
        if (node.isOff()) {
            off++;
        }
        levels = Math.max(levels, depth + 1);
    }

    public Map<ZoneType, Integer> getPerType() { return perType; }
    public int getTotal() { return total; }
    public int getOff() { return off; }
    public int getLevels() { return levels; }
}