package lk.jiat.smartcity360.service;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;

import java.util.logging.Logger;

@Singleton
@Startup
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ReportRecovery {

    private static final Logger LOG = Logger.getLogger(ReportRecovery.class.getName());

    @EJB
    private ReportJobService jobs;

    @PostConstruct
    void recover() {
        try {
            int n = jobs.failInterrupted();
            if (n > 0) {
                LOG.info(n + " interrupted report job(s) marked FAILED");
            }
        } catch (RuntimeException e) {
            LOG.warning("Report recovery skipped: " + e.getMessage());
        }
    }
}
