package lk.jiat.smartcity360.messaging;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.jms.JMSConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSRuntimeException;
import jakarta.jms.Queue;
import lk.jiat.smartcity360.domain.enums.NotificationType;

import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class NotificationPublisher {

    private static final Logger LOG = Logger.getLogger(NotificationPublisher.class.getName());

    @Inject
    @JMSConnectionFactory("jms/SC360ConnectionFactory")
    private JMSContext jms;

    @Resource(lookup = "jms/SC360NotificationQueue")
    private Queue queue;

    public void publish(Long userId, NotificationType type, String title, String message) {
        if (userId == null) {
            return;
        }
        try {
            jms.createProducer()
                    .setProperty("userId", userId)
                    .setProperty("type", type.name())
                    .setProperty("title", cut(title, 120))
                    .send(queue, cut(message, 500));
        } catch (JMSRuntimeException e) {
            LOG.log(Level.WARNING, "Could not queue notification for user " + userId, e);
        }
    }

    private String cut(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }
}
