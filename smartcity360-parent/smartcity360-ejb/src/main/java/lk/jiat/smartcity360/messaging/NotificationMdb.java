package lk.jiat.smartcity360.messaging;

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.EJB;
import jakarta.ejb.MessageDriven;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;
import lk.jiat.smartcity360.domain.enums.NotificationType;
import lk.jiat.smartcity360.service.NotificationService;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/SC360NotificationQueue"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue")
})
public class NotificationMdb implements MessageListener {

    private static final Logger LOG = Logger.getLogger(NotificationMdb.class.getName());

    @EJB
    private NotificationService notificationService;
    @EJB
    private NotificationDispatcher dispatcher;

    @Override
    public void onMessage(Message message) {
        try {
            if (!(message instanceof TextMessage text)) {
                LOG.warning("Ignored a notification message that is not text: " + message.getJMSMessageID());
                return;
            }
            long userId = text.getLongProperty("userId");
            NotificationType type = NotificationType.valueOf(text.getStringProperty("type"));
            String title = text.getStringProperty("title");

            Optional<NotificationService.Delivery> stored =
                    notificationService.store(userId, type, title, text.getText());
            stored.ifPresent(dispatcher::dispatchAsync);
        } catch (JMSException | IllegalArgumentException e) {
            LOG.log(Level.WARNING, "Dropped a malformed notification message", e);
        }
    }
}
