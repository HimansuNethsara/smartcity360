package lk.jiat.smartcity360.repository;

import jakarta.ejb.Stateless;
import lk.jiat.smartcity360.domain.StreamAudit;

import java.util.List;

@Stateless
public class StreamAuditRepository extends AbstractRepository<StreamAudit, Long> {

    @Override
    protected Class<StreamAudit> entityClass() {
        return StreamAudit.class;
    }

    public List<StreamAudit> findRecent(Long meterId, int max) {
        return em.createQuery(
                        "SELECT a FROM StreamAudit a WHERE a.meter.id = :mid ORDER BY a.processedAt DESC, a.id DESC",
                        StreamAudit.class)
                .setParameter("mid", meterId)
                .setMaxResults(max)
                .getResultList();
    }

    public List<StreamAudit> findByMeter(Long meterId) {
        return em.createQuery(
                        "SELECT a FROM StreamAudit a WHERE a.meter.id = :mid ORDER BY a.processedAt DESC",
                        StreamAudit.class)
                .setParameter("mid", meterId)
                .getResultList();
    }
}