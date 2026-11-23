package lk.jiat.smartcity360.repository;

import jakarta.ejb.Stateless;
import lk.jiat.smartcity360.domain.EscalationPolicy;
import lk.jiat.smartcity360.domain.enums.ComplaintSeverity;

import java.util.Optional;

@Stateless
public class EscalationPolicyRepository extends AbstractRepository<EscalationPolicy, Short> {

    @Override
    protected Class<EscalationPolicy> entityClass() {
        return EscalationPolicy.class;
    }

    public Optional<EscalationPolicy> findBySeverity(ComplaintSeverity severity) {
        return em.createQuery(
                        "SELECT p FROM EscalationPolicy p WHERE p.severity = :sev",
                        EscalationPolicy.class)
                .setParameter("sev", severity)
                .getResultStream()
                .findFirst();
    }
}