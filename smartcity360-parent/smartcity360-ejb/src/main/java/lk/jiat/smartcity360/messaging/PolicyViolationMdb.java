package lk.jiat.smartcity360.messaging;

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.EJB;
import jakarta.ejb.MessageDriven;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;
import lk.jiat.smartcity360.domain.User;
import lk.jiat.smartcity360.domain.enums.NotificationType;
import lk.jiat.smartcity360.repository.UserRepository;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/SC360PolicyQueue"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue")
})
public class PolicyViolationMdb implements MessageListener {

    private static final Logger LOG = Logger.getLogger(PolicyViolationMdb.class.getName());
    private static final String[] RECIPIENT_ROLES = {"ADMIN", "CITY_MANAGER", "SUPERVISOR"};

    @EJB
    private UserRepository userRepository;
    @EJB
    private NotificationPublisher notifier;

    @Override
    public void onMessage(Message message) {
        try {
            if (!(message instanceof TextMessage text)) {
                return;
            }
            String policy = text.getStringProperty("policyName");
            String zone = text.getStringProperty("zoneName");
            String action = text.getText();

            Set<Long> recipients = new LinkedHashSet<>();
            for (String role : RECIPIENT_ROLES) {
                for (User u : userRepository.findEnabledByRole(role)) {
                    recipients.add(u.getId());
                }
            }
            String title = "Policy violation: " + policy;
            String body = (zone.isEmpty() ? "" : zone + ": ") + (action == null ? "" : action);
            for (Long userId : recipients) {
                notifier.publish(userId, NotificationType.POLICY_VIOLATION, title, body);
            }
        } catch (JMSException e) {
            LOG.log(Level.WARNING, "Dropped a malformed policy message", e);
        }
    }
}
