package lk.jiat.smartcity360.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import lk.jiat.smartcity360.domain.EnergyMeter;
import lk.jiat.smartcity360.domain.MeterReading;
import lk.jiat.smartcity360.domain.Sensor;
import lk.jiat.smartcity360.domain.SensorReading;
import lk.jiat.smartcity360.domain.enums.MeterStatus;
import lk.jiat.smartcity360.domain.enums.SensorType;
import lk.jiat.smartcity360.dto.PollResult;
import lk.jiat.smartcity360.repository.EnergyMeterRepository;
import lk.jiat.smartcity360.repository.MeterReadingRepository;
import lk.jiat.smartcity360.repository.SensorReadingRepository;
import lk.jiat.smartcity360.repository.SensorRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.ThreadLocalRandom;

@Stateless
public class SensorPollingService {

    @EJB
    private SensorRepository sensorRepository;
    @EJB
    private SensorReadingRepository sensorReadingRepository;
    @EJB
    private EnergyMeterRepository meterRepository;
    @EJB
    private MeterReadingRepository meterReadingRepository;

    public PollResult pollOnce() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        int sensorRows = 0;
        for (Sensor s : sensorRepository.findActive()) {
            SensorReading last = sensorReadingRepository.findLatest(s.getId());
            double next = nextValue(s.getSensorType(), last == null ? null : last.getValue().doubleValue());
            sensorReadingRepository.save(SensorReading.builder()
                    .sensor(s).value(scaled(next)).readAt(now).build());
            sensorRows++;
        }
        int meterRows = 0;
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (EnergyMeter m : meterRepository.findAll()) {
            if (m.getStatus() != MeterStatus.ACTIVE) {
                continue;
            }
            meterReadingRepository.save(MeterReading.builder()
                    .meter(m)
                    .kwh(scaled(5 + rnd.nextDouble() * 20))
                    .voltage(BigDecimal.valueOf(225 + rnd.nextDouble() * 10).setScale(2, RoundingMode.HALF_UP))
                    .readAt(now).build());
            meterRows++;
        }
        return new PollResult(sensorRows, meterRows);
    }

    private double nextValue(SensorType type, Double previous) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        double min;
        double max;
        double step;
        switch (type) {
            case NOISE -> { min = 50; max = 100; step = 6; }
            case TEMPERATURE -> { min = 25; max = 42; step = 1; }
            default -> { min = 30; max = 130; step = 8; }
        }
        double v = previous == null
                ? min + rnd.nextDouble() * (max - min)
                : previous + (rnd.nextDouble() * 2 - 1) * step;
        if (type == SensorType.NOISE && rnd.nextInt(10) == 0) {
            v = 92 + rnd.nextDouble() * 8;                 // occasional spike: breaks the 70 and 90 dB limits
        }
        return Math.max(min, Math.min(max, v));
    }

    private BigDecimal scaled(double v) {
        return BigDecimal.valueOf(v).setScale(3, RoundingMode.HALF_UP);
    }
}
