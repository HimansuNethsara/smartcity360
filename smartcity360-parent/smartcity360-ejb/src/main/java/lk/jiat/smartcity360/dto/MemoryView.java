package lk.jiat.smartcity360.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemoryView {
    private final int lights;
    private final int distinctTypes;
    private final int typeObjectsInFlyweight;
    private final long requests;
    private final long hits;
    private final long naiveEstimatedBytes;
    private final long flyweightEstimatedBytes;
    private final double estimatedSavedPercent;
    private final long naiveMeasuredBytes;
    private final long flyweightMeasuredBytes;
}
