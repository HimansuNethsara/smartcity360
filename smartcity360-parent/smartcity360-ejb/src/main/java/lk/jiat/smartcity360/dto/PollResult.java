package lk.jiat.smartcity360.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PollResult {
    private final int sensorReadings;
    private final int meterReadings;
}
