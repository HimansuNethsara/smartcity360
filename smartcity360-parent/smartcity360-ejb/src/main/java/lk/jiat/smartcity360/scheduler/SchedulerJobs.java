package lk.jiat.smartcity360.scheduler;

import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import lk.jiat.smartcity360.dto.PollResult;
import lk.jiat.smartcity360.dto.RunSummary;
import lk.jiat.smartcity360.service.PolicySweepService;
import lk.jiat.smartcity360.service.SensorPollingService;

import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class SchedulerJobs {

    private static final Logger LOG = Logger.getLogger(SchedulerJobs.class.getName());

    @EJB
    private SensorPollingService pollingService;
    @EJB
    private PolicySweepService sweepService;

    @Schedule(minute = "*", hour = "*", persistent = false, info = "sensor-poll")
    public void pollSensors() {
        if (!enabled()) {
            return;
        }
        try {
            PollResult r = pollingService.pollOnce();
            LOG.info(() -> "Sensor poll: " + r.getSensorReadings() + " sensor and "
                    + r.getMeterReadings() + " meter readings");
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "Sensor poll failed", e);
        }
    }

    @Schedule(minute = "*/2", hour = "*", persistent = false, info = "policy-sweep")
    public void sweepPolicies() {
        if (!enabled()) {
            return;
        }
        try {
            RunSummary s = sweepService.sweep();
            LOG.info(() -> "Policy sweep: " + s.getEvaluated() + " checks, " + s.getViolations() + " violation(s)");
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "Policy sweep failed", e);
        }
    }

    private boolean enabled() {
        return Boolean.parseBoolean(System.getProperty("sc360.jobs.enabled", "true"));
    }
}
