package lk.jiat.smartcity360.repository;

import jakarta.ejb.Stateless;
import lk.jiat.smartcity360.domain.AuditLog;

import java.util.List;

@Stateless
public class AuditLogRepository extends AbstractRepository<AuditLog, Long> {

    @Override
    protected Class<AuditLog> entityClass() {
        return AuditLog.class;
    }

    public List<AuditLog> findByEntity(String entityType, Long entityId) {
        return em.createQuery(
                        "SELECT a FROM AuditLog a WHERE a.entityType = :et AND a.entityId = :eid " +
                                "ORDER BY a.createdAt DESC",
                        AuditLog.class)
                .setParameter("et", entityType)
                .setParameter("eid", entityId)
                .getResultList();
    }
}