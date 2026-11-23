package lk.jiat.smartcity360.repository;

import jakarta.ejb.Stateless;
import lk.jiat.smartcity360.domain.MeterStreamLayer;

import java.util.List;

@Stateless
public class MeterStreamLayerRepository extends AbstractRepository<MeterStreamLayer, Long> {

    @Override
    protected Class<MeterStreamLayer> entityClass() {
        return MeterStreamLayer.class;
    }

    public List<MeterStreamLayer> findAllByMeter(Long meterId) {
        return em.createQuery(
                        "SELECT l FROM MeterStreamLayer l WHERE l.meter.id = :mid ORDER BY l.layerOrder ASC",
                        MeterStreamLayer.class)
                .setParameter("mid", meterId)
                .getResultList();
    }

    public List<MeterStreamLayer> findByMeterOrdered(Long meterId) {
        return em.createQuery(
                        "SELECT l FROM MeterStreamLayer l WHERE l.meter.id = :mid AND l.enabled = true " +
                                "ORDER BY l.layerOrder ASC",
                        MeterStreamLayer.class)
                .setParameter("mid", meterId)
                .getResultList();
    }
}