package lk.jiat.smartcity360.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import lk.jiat.smartcity360.dto.EvaluationView;
import lk.jiat.smartcity360.dto.RunSummary;
import lk.jiat.smartcity360.messaging.PolicyViolationPublisher;
import lk.jiat.smartcity360.scheduler.ViolationThrottle;

@Stateless
public class PolicySweepService {

    @EJB
    private PolicyEvaluationService evaluationService;
    @EJB
    private PolicyViolationPublisher publisher;
    @EJB
    private ViolationThrottle throttle;

    public RunSummary sweep() {
        RunSummary summary = evaluationService.runAllEnabled();
        for (EvaluationView f : summary.getFindings()) {
            if (!"VIOLATION".equals(f.getOutcome())) {
                continue;
            }
            if (throttle.shouldNotify(f.getPolicyId() + "|" + f.getZoneName())) {
                publisher.publish(f.getPolicyId(), f.getPolicyName(), f.getZoneName(), f.getActionResult());
            }
        }
        return summary;
    }
}
