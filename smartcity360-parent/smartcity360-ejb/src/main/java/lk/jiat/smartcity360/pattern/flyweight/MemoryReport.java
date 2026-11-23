package lk.jiat.smartcity360.pattern.flyweight;

public record MemoryReport(int lights, int distinctTypes, int typeObjectsInFlyweight,
                           long requests, long hits,
                           long naiveEstimatedBytes, long flyweightEstimatedBytes, double estimatedSavedPercent,
                           long naiveMeasuredBytes, long flyweightMeasuredBytes) {
}
