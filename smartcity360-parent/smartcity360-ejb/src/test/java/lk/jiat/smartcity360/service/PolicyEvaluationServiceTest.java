package lk.jiat.smartcity360.service;

import lk.jiat.smartcity360.domain.CityPolicy;
import lk.jiat.smartcity360.domain.PolicyEvaluation;
import lk.jiat.smartcity360.domain.Sensor;
import lk.jiat.smartcity360.domain.SensorReading;
import lk.jiat.smartcity360.domain.Zone;
import lk.jiat.smartcity360.domain.enums.LandUse;
import lk.jiat.smartcity360.domain.enums.PolicyOutcome;
import lk.jiat.smartcity360.domain.enums.SensorType;
import lk.jiat.smartcity360.dto.EvaluateRequest;
import lk.jiat.smartcity360.dto.EvaluationView;
import lk.jiat.smartcity360.dto.RunSummary;
import lk.jiat.smartcity360.repository.CityPolicyRepository;
import lk.jiat.smartcity360.repository.PolicyEvaluationRepository;
import lk.jiat.smartcity360.repository.SensorReadingRepository;
import lk.jiat.smartcity360.repository.SensorRepository;
import lk.jiat.smartcity360.repository.ZoneRepository;
import lk.jiat.smartcity360.service.exception.BusinessRuleException;
import lk.jiat.smartcity360.service.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolicyEvaluationServiceTest {

    private static final String LOUD = "IF noise > 70 THEN FINE 500";

    @Mock CityPolicyRepository policyRepository;
    @Mock PolicyEvaluationRepository evaluationRepository;
    @Mock ZoneRepository zoneRepository;
    @Mock SensorRepository sensorRepository;
    @Mock SensorReadingRepository readingRepository;

    @InjectMocks PolicyEvaluationService service;

    // ------------------------------------------------------------------ builders

    private static CityPolicy policy(long id, String name, String dsl) {
        return CityPolicy.builder().id(id).name(name).dslText(dsl).enabled(true).build();
    }

    private static Zone zone(long id, LandUse use) {
        return Zone.builder().id(id).name("Zone " + id).code("Z" + id).landUse(use).build();
    }

    private static Sensor sensor(long id, SensorType type, Zone zone) {
        return Sensor.builder().id(id).sensorCode("S" + id).sensorType(type).zone(zone).build();
    }

    private static SensorReading reading(String value) {
        return SensorReading.builder().value(new BigDecimal(value)).build();
    }

    private static EvaluateRequest values(Double noise) {
        EvaluateRequest r = new EvaluateRequest();
        r.setNoise(noise);
        return r;
    }

    private PolicyEvaluation recorded() {
        ArgumentCaptor<PolicyEvaluation> c = ArgumentCaptor.forClass(PolicyEvaluation.class);
        verify(evaluationRepository).save(c.capture());
        return c.getValue();
    }

    // ------------------------------------------------------------------ evaluate with typed-in values

    @Test
    @DisplayName("A reading over the limit is a VIOLATION with the policy's action, and the run is recorded")
    void violation() {
        when(policyRepository.findById(1L)).thenReturn(policy(1, "Noise limit", LOUD));

        EvaluationView view = service.evaluate(1L, values(80.0));

        assertEquals("VIOLATION", view.getOutcome());
        assertEquals("FINE 500", view.getActionResult());
        PolicyEvaluation row = recorded();
        assertEquals(PolicyOutcome.VIOLATION, row.getOutcome());
        assertEquals("{\"noise\":80}", row.getInputJson());
    }

    @Test
    @DisplayName("A reading within the limit is a PASS with no action")
    void pass() {
        when(policyRepository.findById(1L)).thenReturn(policy(1, "Noise limit", LOUD));

        EvaluationView view = service.evaluate(1L, values(60.0));

        assertEquals("PASS", view.getOutcome());
        assertNull(view.getActionResult());
    }

    @Test
    @DisplayName("A value the policy needs but did not get is recorded as an ERROR naming the variable")
    void missingValueIsError() {
        when(policyRepository.findById(1L)).thenReturn(policy(1, "Noise limit", LOUD));
        EvaluateRequest onlyTemperature = new EvaluateRequest();
        onlyTemperature.setTemperature(20.0);

        EvaluationView view = service.evaluate(1L, onlyTemperature);

        assertEquals("ERROR", view.getOutcome());
        assertTrue(view.getActionResult().contains("noise"));
    }

    @Test
    @DisplayName("A policy whose stored text no longer parses is recorded as an ERROR, not thrown")
    void invalidPolicyTextIsError() {
        when(policyRepository.findById(1L)).thenReturn(policy(1, "Broken", "this is not a policy"));

        EvaluationView view = service.evaluate(1L, values(80.0));

        assertEquals("ERROR", view.getOutcome());
        assertTrue(view.getActionResult().startsWith("Policy text is not valid"));
    }

    // ------------------------------------------------------------------ evaluate: input checks

    @Test
    @DisplayName("Unknown policy and unknown zone are 'not found'")
    void notFound() {
        assertThrows(ResourceNotFoundException.class, () -> service.evaluate(404L, values(80.0)));

        when(policyRepository.findById(1L)).thenReturn(policy(1, "Noise limit", LOUD));
        EvaluateRequest badZone = new EvaluateRequest();
        badZone.setZoneId(99L);
        assertThrows(ResourceNotFoundException.class, () -> service.evaluate(1L, badZone));
        verify(evaluationRepository, never()).save(any());
    }

    @Test
    @DisplayName("An empty request, an hour outside 0-23, NaN and an unknown land use are refused")
    void invalidInput() {
        when(policyRepository.findById(1L)).thenReturn(policy(1, "Noise limit", LOUD));

        assertThrows(BusinessRuleException.class, () -> service.evaluate(1L, new EvaluateRequest()));
        assertThrows(BusinessRuleException.class, () -> service.evaluate(1L, null));

        EvaluateRequest badHour = values(80.0);
        badHour.setHour(24);
        assertThrows(BusinessRuleException.class, () -> service.evaluate(1L, badHour));

        assertThrows(BusinessRuleException.class, () -> service.evaluate(1L, values(Double.NaN)));

        EvaluateRequest badZoneText = values(80.0);
        badZoneText.setZone("moon");
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> service.evaluate(1L, badZoneText));
        assertTrue(ex.getMessage().contains("Unknown land use"));
        verify(evaluationRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ evaluate from live sensors

    @Test
    @DisplayName("Live sensors in the zone are averaged per type and fed to the policy together with the land use")
    void liveReadingsAreAveraged() {
        Zone z = zone(5, LandUse.RESIDENTIAL);
        Sensor a = sensor(1, SensorType.NOISE, z);
        Sensor b = sensor(2, SensorType.NOISE, z);
        Sensor elsewhere = sensor(3, SensorType.NOISE, zone(6, LandUse.MIXED));
        when(policyRepository.findById(1L)).thenReturn(
                policy(1, "Night noise", "IF noise >= 70 AND zone = \"RESIDENTIAL\" THEN ALERT \"Too loud\""));
        when(zoneRepository.findById(5L)).thenReturn(z);
        when(sensorRepository.findActive()).thenReturn(List.of(a, b, elsewhere));
        when(readingRepository.findLatest(1L)).thenReturn(reading("60"));
        when(readingRepository.findLatest(2L)).thenReturn(reading("80"));
        EvaluateRequest req = new EvaluateRequest();
        req.setZoneId(5L);

        EvaluationView view = service.evaluate(1L, req);

        assertEquals("VIOLATION", view.getOutcome());
        PolicyEvaluation row = recorded();
        assertTrue(row.getInputJson().contains("\"noise\":70"), row.getInputJson());
        assertTrue(row.getInputJson().contains("\"zone\":\"RESIDENTIAL\""), row.getInputJson());
        assertTrue(row.getInputJson().contains("\"hour\":"), row.getInputJson());
        assertSame(z, row.getZone());
    }

    @Test
    @DisplayName("Typed-in values override what the sensors say")
    void overridesWin() {
        Zone z = zone(5, LandUse.RESIDENTIAL);
        when(policyRepository.findById(1L)).thenReturn(policy(1, "Noise limit", LOUD));
        when(zoneRepository.findById(5L)).thenReturn(z);
        when(sensorRepository.findActive()).thenReturn(List.of(sensor(1, SensorType.NOISE, z)));
        when(readingRepository.findLatest(1L)).thenReturn(reading("40"));
        EvaluateRequest req = values(90.0);
        req.setZoneId(5L);

        assertEquals("VIOLATION", service.evaluate(1L, req).getOutcome());
    }

    // ------------------------------------------------------------------ run all

    @Test
    @DisplayName("Run-all records violations and errors, and skips policies that need a sensor the zone does not have")
    void runAllEnabled() {
        Zone z = zone(5, LandUse.MIXED);
        when(policyRepository.findEnabled()).thenReturn(List.of(
                policy(1, "Noise", LOUD),
                policy(2, "Heat", "IF temperature > 30 THEN ALERT \"Hot\""),
                policy(3, "Broken", "nonsense")));
        when(sensorRepository.findActive()).thenReturn(List.of(sensor(1, SensorType.NOISE, z)));
        when(zoneRepository.findById(5L)).thenReturn(z);
        when(readingRepository.findLatest(1L)).thenReturn(reading("80"));

        RunSummary summary = service.runAllEnabled();

        assertEquals(1, summary.getZones());
        assertEquals(3, summary.getPolicies());
        assertEquals(2, summary.getEvaluated(), "noise policy and the broken policy were recorded");
        assertEquals(1, summary.getViolations());
        assertEquals(1, summary.getErrors());
        assertEquals(1, summary.getSkipped(), "heat policy has no temperature sensor in this zone");
        assertEquals(2, summary.getFindings().size());
    }

    @Test
    @DisplayName("With no active sensors nothing is evaluated")
    void runAllWithoutSensors() {
        when(policyRepository.findEnabled()).thenReturn(List.of(policy(1, "Noise", LOUD)));
        when(sensorRepository.findActive()).thenReturn(List.of());

        RunSummary summary = service.runAllEnabled();

        assertEquals(0, summary.getZones());
        assertEquals(0, summary.getEvaluated());
        verify(evaluationRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ history

    @Test
    @DisplayName("The history limit is kept between 1 and 100")
    void recentLimitIsClamped() {
        when(evaluationRepository.findRecentAll(100)).thenReturn(List.of());
        when(evaluationRepository.findRecentAll(1)).thenReturn(List.of());

        assertTrue(service.recent(null, 500).isEmpty());
        assertTrue(service.recent(null, 0).isEmpty());
        verify(evaluationRepository).findRecentAll(100);
        verify(evaluationRepository).findRecentAll(1);
    }

    @Test
    @DisplayName("History for an unknown policy is 'not found'")
    void recentUnknownPolicy() {
        assertThrows(ResourceNotFoundException.class, () -> service.recent(404L, 10));
    }
}
