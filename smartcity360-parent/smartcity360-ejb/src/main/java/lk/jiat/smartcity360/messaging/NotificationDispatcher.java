package lk.jiat.smartcity360.messaging;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import lk.jiat.smartcity360.service.NotificationService;

import java.util.logging.Logger;

@Stateless
public class NotificationDispatcher {

    private static final Logger LOG = Logger.getLogger(NotificationDispatcher.class.getName());

    @Resource
    private ManagedExecutorService executor;

    public void dispatchAsync(NotificationService.Delivery d) {
        executor.execute(() -> deliver(d));
    }

    private void deliver(NotificationService.Delivery d) {
        try {
            Thread.sleep(300);                         // stands in for the SMTP / SMS round trip
            LOG.info(() -> "[email -> " + d.email() + "] " + d.title() + ": " + d.message()
                    + " (thread " + Thread.currentThread().getName() + ")");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
