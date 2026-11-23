package lk.jiat.smartcity360.scheduler;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.ejb.ConcurrencyManagement;
import jakarta.ejb.ConcurrencyManagementType;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import lk.jiat.smartcity360.dto.OverdueResult;
import lk.jiat.smartcity360.service.ComplaintService;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class EscalationTimeoutScheduler {

    private static final Logger LOG = Logger.getLogger(EscalationTimeoutScheduler.class.getName());
    private static final long PERIOD_SECONDS = 20;

    @Resource
    private ManagedScheduledExecutorService scheduler;
    @EJB
    private ComplaintService complaintService;

    private ScheduledFuture<?> job;

    @PostConstruct
    void start() {
        job = scheduler.scheduleWithFixedDelay(this::runOnce, 30, PERIOD_SECONDS, TimeUnit.SECONDS);
        LOG.info("Escalation timeout check scheduled every " + PERIOD_SECONDS + " seconds");
    }

    @PreDestroy
    void stop() {
        if (job != null) {
            job.cancel(false);
        }
    }

    private void runOnce() {
        if (!Boolean.parseBoolean(System.getProperty("sc360.jobs.enabled", "true"))) {
            return;
        }
        try {
            OverdueResult r = complaintService.processTimeouts();
            if (r.getEscalated() > 0) {
                LOG.info(() -> "Timeout check escalated complaints " + r.getComplaintIds()
                        + " (thread " + Thread.currentThread().getName() + ")");
            }
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "Timeout check failed", e);
        }
    }
}


