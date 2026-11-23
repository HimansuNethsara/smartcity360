package lk.jiat.smartcity360.repository;

import jakarta.ejb.Stateless;
import lk.jiat.smartcity360.domain.PolicyEvaluation;

import java.util.List;

@Stateless
public class PolicyEvaluationRepository extends AbstractRepository<PolicyEvaluation, Long> {

    @Override
    protected Class<PolicyEvaluation> entityClass() {
        return PolicyEvaluation.class;
    }

    public List<PolicyEvaluation> findByPolicy(Long policyId) {
        return em.createQuery(
                        "SELECT e FROM PolicyEvaluation e WHERE e.policy.id = :pid ORDER BY e.evaluatedAt DESC",
                        PolicyEvaluation.class)
                .setParameter("pid", policyId)
                .getResultList();
    }

    public List<PolicyEvaluation> findRecent(Long policyId, int max) {
        return em.createQuery(
                        "SELECT e FROM PolicyEvaluation e WHERE e.policy.id = :pid ORDER BY e.evaluatedAt DESC, e.id DESC",
                        PolicyEvaluation.class)
                .setParameter("pid", policyId)
                .setMaxResults(max)
                .getResultList();
    }

    public List<PolicyEvaluation> findRecentAll(int max) {
        return em.createQuery("SELECT e FROM PolicyEvaluation e ORDER BY e.evaluatedAt DESC, e.id DESC",
                        PolicyEvaluation.class)
                .setMaxResults(max)
                .getResultList();
    }
}