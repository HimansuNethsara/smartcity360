package lk.jiat.smartcity360.repository;

import jakarta.ejb.Stateless;
import lk.jiat.smartcity360.domain.Complaint;
import lk.jiat.smartcity360.domain.enums.ComplaintStatus;

import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class ComplaintRepository extends AbstractRepository<Complaint, Long> {

    @Override
    protected Class<Complaint> entityClass() {
        return Complaint.class;
    }

    public List<Complaint> findByCitizen(Long citizenId) {
        return em.createQuery(
                        "SELECT c FROM Complaint c WHERE c.citizen.id = :cid ORDER BY c.createdAt DESC",
                        Complaint.class)
                .setParameter("cid", citizenId)
                .getResultList();
    }

    public List<Complaint> findByAssignee(Long userId) {
        return em.createQuery(
                        "SELECT c FROM Complaint c WHERE c.assignedTo.id = :uid ORDER BY c.createdAt DESC",
                        Complaint.class)
                .setParameter("uid", userId)
                .getResultList();
    }

    public List<Complaint> findDueForEscalation(LocalDateTime now) {
        return em.createQuery(
                        "SELECT c FROM Complaint c WHERE c.escalateAt IS NOT NULL AND c.escalateAt <= :now " +
                                "AND c.status NOT IN :closedStatuses",
                        Complaint.class)
                .setParameter("now", now)
                .setParameter("closedStatuses", List.of(ComplaintStatus.RESOLVED, ComplaintStatus.CLOSED))
                .getResultList();
    }

    public List<Complaint> findByStatus(ComplaintStatus status) {
        return em.createQuery("SELECT c FROM Complaint c WHERE c.status = :st", Complaint.class)
                .setParameter("st", status)
                .getResultList();
    }

    public List<Complaint> findAllNewestFirst() {
        return em.createQuery("SELECT c FROM Complaint c ORDER BY c.createdAt DESC, c.id DESC", Complaint.class)
                .getResultList();
    }

    public long countOpenByAssignee(Long userId) {
        return em.createQuery(
                        "SELECT COUNT(c) FROM Complaint c WHERE c.assignedTo.id = :uid AND c.status NOT IN :done",
                        Long.class)
                .setParameter("uid", userId)
                .setParameter("done", List.of(ComplaintStatus.RESOLVED, ComplaintStatus.CLOSED))
                .getSingleResult();
    }
}