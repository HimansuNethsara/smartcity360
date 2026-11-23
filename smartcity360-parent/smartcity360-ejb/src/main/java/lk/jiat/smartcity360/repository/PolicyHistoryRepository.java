package lk.jiat.smartcity360.repository;

import jakarta.ejb.Stateless;
import lk.jiat.smartcity360.domain.PolicyHistory;

import java.util.List;

@Stateless
public class PolicyHistoryRepository extends AbstractRepository<PolicyHistory, Long> {

    @Override
    protected Class<PolicyHistory> entityClass() {
        return PolicyHistory.class;
    }

    public List<PolicyHistory> findByPolicyOrdered(Long policyId) {
        return em.createQuery(
                        "SELECT h FROM PolicyHistory h WHERE h.policy.id = :pid ORDER BY h.versionNo DESC",
                        PolicyHistory.class)
                .setParameter("pid", policyId)
                .getResultList();
    }
}