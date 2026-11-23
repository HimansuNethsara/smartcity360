package lk.jiat.smartcity360.repository;

import jakarta.ejb.Stateless;
import lk.jiat.smartcity360.domain.ComplaintEscalation;

import java.util.List;

@Stateless
public class ComplaintEscalationRepository extends AbstractRepository<ComplaintEscalation, Long> {

    @Override
    protected Class<ComplaintEscalation> entityClass() {
        return ComplaintEscalation.class;
    }

    public List<ComplaintEscalation> findByComplaint(Long complaintId) {
        return em.createQuery(
                        "SELECT e FROM ComplaintEscalation e WHERE e.complaint.id = :cid ORDER BY e.createdAt ASC",
                        ComplaintEscalation.class)
                .setParameter("cid", complaintId)
                .getResultList();
    }
}