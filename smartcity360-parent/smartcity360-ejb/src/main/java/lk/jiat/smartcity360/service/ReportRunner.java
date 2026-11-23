package lk.jiat.smartcity360.service;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import lk.jiat.smartcity360.domain.enums.NotificationType;
import lk.jiat.smartcity360.messaging.NotificationPublisher;

import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class ReportRunner {

    private static final Logger LOG = Logger.getLogger(ReportRunner.class.getName());

    @Resource
    private ManagedExecutorService executor;
    @EJB
    private ReportJobService jobs;
    @EJB
    private ReportBuilder builder;
    @EJB
    private NotificationPublisher notifier;

    public void start(Long jobId) {
        executor.execute(() -> work(jobId));
    }

    private void work(Long jobId) {
        try {
            ReportJobService.JobSpec spec = jobs.markRunning(jobId);
            if (spec == null) {
                return;
            }
            LOG.info(() -> "Report " + jobId + " (" + spec.type() + ") running on "
                    + Thread.currentThread().getName());
            pause();
            String json = builder.build(spec.type(), spec.hours());
            jobs.complete(jobId, json);
            notifier.publish(spec.userId(), NotificationType.REPORT_READY, "Report ready",
                    "Your " + spec.type() + " report (#" + jobId + ") is ready.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            jobs.fail(jobId, "Interrupted");
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "Report " + jobId + " failed", e);
            try {
                jobs.fail(jobId, e.getMessage());
            } catch (RuntimeException inner) {
                LOG.log(Level.SEVERE, "Could not record the failure of report " + jobId, inner);
            }
        }
    }

    private void pause() throws InterruptedException {
        long ms = Long.getLong("sc360.report.delay.ms", 3000L);
        if (ms > 0) {
            Thread.sleep(ms);
        }
    }
}
