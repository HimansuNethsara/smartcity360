package lk.jiat.smartcity360.arquillian;

import lk.jiat.smartcity360.domain.enums.ComplaintLevel;
import lk.jiat.smartcity360.domain.enums.ComplaintSeverity;
import lk.jiat.smartcity360.domain.enums.LayerType;
import lk.jiat.smartcity360.pattern.chain.EscalationChain;
import lk.jiat.smartcity360.pattern.chain.RoutingDecision;
import lk.jiat.smartcity360.pattern.chain.RoutingRequest;
import lk.jiat.smartcity360.pattern.decorator.MeterStream;
import lk.jiat.smartcity360.pattern.decorator.MeterStreamAssembler;
import lk.jiat.smartcity360.pattern.decorator.MeterStreamAssembler.LayerSpec;
import lk.jiat.smartcity360.pattern.decorator.StreamProcessingException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("arquillian")
@ExtendWith(ArquillianExtension.class)
public class DecoratorChainArquillianIT {

    @Deployment
    public static WebArchive deployment() {
        return Deployments.war("sc360-decorator-chain-it",
                "lk.jiat.smartcity360.pattern.decorator", "lk.jiat.smartcity360.pattern.chain",
                "lk.jiat.smartcity360.domain.enums");
    }

    private static final byte[] PAYLOAD = "{\"meter\":\"MTR-9\",\"kwh\":4.2}".getBytes(StandardCharsets.UTF_8);

    private static MeterStream fullStack(String meter) {
        return MeterStreamAssembler.assemble(meter, List.of(
                new LayerSpec(LayerType.COMPRESSION, 1, "GZIP"),
                new LayerSpec(LayerType.ENCRYPTION, 2, "AES-256"),
                new LayerSpec(LayerType.AUTHENTICATION, 3, "TOKEN")));
    }

    @Test
    @DisplayName("A Compression > Encryption > Authentication stack round-trips inside the server")
    void decoratorStackRoundTrip() {
        MeterStream s = fullStack("MTR-9");
        assertEquals("Raw>Compress>Encrypt>Auth", s.describe());
        assertArrayEquals(PAYLOAD, s.receive(s.send(PAYLOAD)));
    }

    @Test
    @DisplayName("Tampered data and another meter's messages are rejected")
    void decoratorRejectsTampering() {
        MeterStream s = fullStack("MTR-9");
        byte[] wire = s.send(PAYLOAD);
        byte[] copy = wire.clone();
        copy[0] ^= 0x01;
        assertThrows(StreamProcessingException.class, () -> s.receive(copy));
        assertThrows(StreamProcessingException.class, () -> fullStack("MTR-OTHER").receive(wire));
    }

    @Test
    @DisplayName("The standard chain is Staff > Supervisor > City Manager")
    void chainOrder() {
        assertEquals("STAFF>SUPERVISOR>CITY_MANAGER", EscalationChain.standard().describe());
    }

    @Test
    @DisplayName("Routing and escalation follow the chain, and nothing is above the City Manager")
    void chainRouting() {
        EscalationChain chain = EscalationChain.standard();

        RoutingDecision first = chain.route(
                RoutingRequest.forNewComplaint(ComplaintSeverity.HIGH, ComplaintLevel.SUPERVISOR, 30, 60)).orElseThrow();
        assertEquals(ComplaintLevel.SUPERVISOR, first.level());
        assertEquals(60, first.timeoutMinutes());

        RoutingDecision next = chain.route(RoutingRequest.forEscalation(
                ComplaintSeverity.HIGH, ComplaintLevel.SUPERVISOR,
                lk.jiat.smartcity360.domain.enums.EscalationReason.MANUAL, 30, 60)).orElseThrow();
        assertEquals(ComplaintLevel.CITY_MANAGER, next.level());
        assertNull(next.timeoutMinutes());

        assertTrue(chain.route(RoutingRequest.forEscalation(
                ComplaintSeverity.HIGH, ComplaintLevel.CITY_MANAGER,
                lk.jiat.smartcity360.domain.enums.EscalationReason.MANUAL, 30, 60)).isEmpty());
    }
}
