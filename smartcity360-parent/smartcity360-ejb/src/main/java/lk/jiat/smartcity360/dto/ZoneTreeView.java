package lk.jiat.smartcity360.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class ZoneTreeView {
    private final ZoneNodeView root;
    private final int totalZones;
    private final int zonesOff;
    private final int levels;
    private final Map<String, Integer> perType;
    private final int usageWindowReadings;
}