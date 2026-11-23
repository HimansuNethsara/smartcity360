package lk.jiat.smartcity360.repository;

import jakarta.ejb.Stateless;
import lk.jiat.smartcity360.domain.EnergyMeter;

import java.util.List;
import java.util.Optional;

@Stateless
public class EnergyMeterRepository extends AbstractRepository<EnergyMeter, Long> {

    @Override
    protected Class<EnergyMeter> entityClass() {
        return EnergyMeter.class;
    }

    public Optional<EnergyMeter> findByCode(String meterCode) {
        return em.createQuery("SELECT m FROM EnergyMeter m WHERE m.meterCode = :code", EnergyMeter.class)
                .setParameter("code", meterCode)
                .getResultStream()
                .findFirst();
    }

    public List<EnergyMeter> findByZone(Long zoneId) {
        return em.createQuery("SELECT m FROM EnergyMeter m WHERE m.zone.id = :zid", EnergyMeter.class)
                .setParameter("zid", zoneId)
                .getResultList();
    }
}