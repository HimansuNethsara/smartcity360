package lk.jiat.smartcity360.repository;

import jakarta.ejb.Stateless;
import lk.jiat.smartcity360.domain.SensorReading;

import java.util.List;

@Stateless
public class SensorReadingRepository extends AbstractRepository<SensorReading, Long> {

    @Override
    protected Class<SensorReading> entityClass() {
        return SensorReading.class;
    }

    public SensorReading findLatest(Long sensorId) {
        return em.createQuery(
                        "SELECT r FROM SensorReading r WHERE r.sensor.id = :sid ORDER BY r.readAt DESC",
                        SensorReading.class)
                .setParameter("sid", sensorId)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public List<SensorReading> findBySensor(Long sensorId) {
        return em.createQuery(
                        "SELECT r FROM SensorReading r WHERE r.sensor.id = :sid ORDER BY r.readAt DESC",
                        SensorReading.class)
                .setParameter("sid", sensorId)
                .getResultList();
    }
}