package lk.jiat.smartcity360.repository;

import jakarta.ejb.Stateless;
import lk.jiat.smartcity360.domain.Sensor;
import lk.jiat.smartcity360.domain.enums.SensorStatus;

import java.util.List;
import java.util.Optional;

@Stateless
public class SensorRepository extends AbstractRepository<Sensor, Long> {

    @Override
    protected Class<Sensor> entityClass() {
        return Sensor.class;
    }

    public Optional<Sensor> findByCode(String sensorCode) {
        return em.createQuery("SELECT s FROM Sensor s WHERE s.sensorCode = :code", Sensor.class)
                .setParameter("code", sensorCode)
                .getResultStream()
                .findFirst();
    }

    /** Phase 6's @Schedule poll target. */
    public List<Sensor> findActive() {
        return em.createQuery("SELECT s FROM Sensor s WHERE s.status = :st", Sensor.class)
                .setParameter("st", SensorStatus.ACTIVE)
                .getResultList();
    }
}