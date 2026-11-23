package lk.jiat.smartcity360.repository;

import jakarta.ejb.Stateless;
import lk.jiat.smartcity360.domain.MeterReading;

import java.util.List;

@Stateless
public class MeterReadingRepository extends AbstractRepository<MeterReading, Long> {

    @Override
    protected Class<MeterReading> entityClass() {
        return MeterReading.class;
    }

    public List<MeterReading> findByMeter(Long meterId) {
        return em.createQuery(
                        "SELECT r FROM MeterReading r WHERE r.meter.id = :mid ORDER BY r.readAt DESC",
                        MeterReading.class)
                .setParameter("mid", meterId)
                .getResultList();
    }

    public List<MeterReading> findRecent(Long meterId, int max) {
        return em.createQuery(
                        "SELECT r FROM MeterReading r WHERE r.meter.id = :mid ORDER BY r.readAt DESC",
                        MeterReading.class)
                .setParameter("mid", meterId)
                .setMaxResults(max)
                .getResultList();
    }

    public MeterReading findLatest(Long meterId) {
        return em.createQuery(
                        "SELECT r FROM MeterReading r WHERE r.meter.id = :mid ORDER BY r.readAt DESC",
                        MeterReading.class)
                .setParameter("mid", meterId)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }
}