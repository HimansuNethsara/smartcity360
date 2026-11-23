package lk.jiat.smartcity360.repository;

import jakarta.ejb.Stateless;
import lk.jiat.smartcity360.domain.ReportJob;
import lk.jiat.smartcity360.domain.enums.ReportStatus;

import java.util.List;

@Stateless
public class ReportJobRepository extends AbstractRepository<ReportJob, Long> {

    @Override
    protected Class<ReportJob> entityClass() {
        return ReportJob.class;
    }

    public List<ReportJob> findByRequester(Long userId) {
        return em.createQuery(
                        "SELECT r FROM ReportJob r WHERE r.requestedBy.id = :uid ORDER BY r.requestedAt DESC",
                        ReportJob.class)
                .setParameter("uid", userId)
                .getResultList();
    }

    public List<ReportJob> findByStatus(ReportStatus status) {
        return em.createQuery("SELECT r FROM ReportJob r WHERE r.status = :st", ReportJob.class)
                .setParameter("st", status)
                .getResultList();
    }
}