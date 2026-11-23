package lk.jiat.smartcity360.messaging;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.jms.JMSConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSRuntimeException;
import jakarta.jms.Queue;

import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class PolicyViolationPublisher {

    private static final Logger LOG = Logger.getLogger(PolicyViolationPublisher.class.getName());

    @Inject
    @JMSConnectionFactory("jms/SC360ConnectionFactory")
    private JMSContext jms;

    @Resource(lookup = "jms/SC360PolicyQueue")
    private Queue queue;

    public void publish(Long policyId, String policyName, String zoneName, String action) {
        try {
            jms.createProducer()
                    .setProperty("policyId", policyId)
                    .setProperty("policyName", policyName == null ? "" : policyName)
                    .setProperty("zoneName", zoneName == null ? "" : zoneName)
                    .send(queue, action == null ? "" : action);
        } catch (JMSRuntimeException e) {
            LOG.log(Level.WARNING, "Could not queue violation of policy " + policyId, e);
        }
    }
}
