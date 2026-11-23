package lk.jiat.smartcity360.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import lk.jiat.smartcity360.domain.*;
import lk.jiat.smartcity360.domain.enums.LandUse;
import lk.jiat.smartcity360.domain.enums.PolicyOutcome;
import lk.jiat.smartcity360.dto.EvaluateRequest;
import lk.jiat.smartcity360.dto.EvaluationView;
import lk.jiat.smartcity360.dto.RunSummary;
import lk.jiat.smartcity360.pattern.interpreter.*;
import lk.jiat.smartcity360.repository.*;
import lk.jiat.smartcity360.service.exception.BusinessRuleException;
import lk.jiat.smartcity360.service.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;

@Stateless
public class PolicyEvaluationService {

    private static final ZoneId CITY_CLOCK = ZoneId.of("Asia/Colombo");

    private final PolicyVocabulary vocabulary = PolicyVocabulary.standard();

    @EJB
    private CityPolicyRepository policyRepository;
    @EJB
    private PolicyEvaluationRepository evaluationRepository;
    @EJB
    private ZoneRepository zoneRepository;
    @EJB
    private SensorRepository sensorRepository;
    @EJB
    private SensorReadingRepository readingRepository;

    // ---------------------------------------------------------------- reads

    public List<EvaluationView> recent(Long policyId, int limit) {
        int max = Math.min(Math.max(limit, 1), 100);
        List<PolicyEvaluation> rows = policyId == null
                ? evaluationRepository.findRecentAll(max)
                : evaluationRepository.findRecent(requirePolicy(policyId).getId(), max);
        return rows.stream().map(this::view).toList();
    }

    // ---------------------------------------------------------------- commands

    public EvaluationView evaluate(Long policyId, EvaluateRequest req) {
        CityPolicy policy = requirePolicy(policyId);
        if (req == null || (req.getZoneId() == null && req.getNoise() == null && req.getTemperature() == null
                && req.getAirQuality() == null && req.getHour() == null && req.getZone() == null)) {
            throw new BusinessRuleException("Give a zone to read live sensors from, or values to test with");
        }

        Zone zone = null;
        EvaluationContext ctx = new EvaluationContext();
        if (req.getZoneId() != null) {
            zone = zoneRepository.findById(req.getZoneId());
            if (zone == null) {
                throw new ResourceNotFoundException("Zone " + req.getZoneId() + " not found");
            }
            fillFromZone(ctx, zone, liveReadings(zone.getId()));
        }
        applyOverrides(ctx, req);
        return view(run(policy, zone, ctx));
    }

    public RunSummary runAllEnabled() {
        List<CityPolicy> enabled = policyRepository.findEnabled();
        Map<Long, List<Sensor>> sensorsByZone = new LinkedHashMap<>();
        for (Sensor s : sensorRepository.findActive()) {
            sensorsByZone.computeIfAbsent(s.getZone().getId(), k -> new ArrayList<>()).add(s);
        }

        int evaluated = 0;
        int violations = 0;
        int errors = 0;
        int skipped = 0;
        List<EvaluationView> findings = new ArrayList<>();
        for (Map.Entry<Long, List<Sensor>> entry : sensorsByZone.entrySet()) {
            Zone zone = zoneRepository.findById(entry.getKey());
            EvaluationContext base = new EvaluationContext();
            fillFromZone(base, zone, averages(entry.getValue()));
            for (CityPolicy policy : enabled) {
                PolicyEvaluation row = run(policy, zone, base, true);
                if (row == null) {          // not applicable to this zone: nothing recorded
                    skipped++;
                    continue;
                }
                evaluated++;
                if (row.getOutcome() == PolicyOutcome.VIOLATION) {
                    violations++;
                    findings.add(view(row));
                } else if (row.getOutcome() == PolicyOutcome.ERROR) {
                    errors++;
                    findings.add(view(row));
                }
            }
        }
        return new RunSummary(sensorsByZone.size(), enabled.size(), evaluated, violations, errors, skipped, findings);
    }

    // ---------------------------------------------------------------- helpers

    private PolicyEvaluation run(CityPolicy policy, Zone zone, EvaluationContext ctx) {
        return run(policy, zone, ctx, false);
    }

    private PolicyEvaluation run(CityPolicy policy, Zone zone, EvaluationContext ctx, boolean skipIfNotApplicable) {
        PolicyOutcome outcome;
        String action;
        try {
            PolicyResult result = PolicyParser.parse(policy.getDslText(), vocabulary).evaluate(ctx);
            outcome = result.violated() ? PolicyOutcome.VIOLATION : PolicyOutcome.PASS;
            action = result.actionText();
        } catch (PolicyParseException e) {
            outcome = PolicyOutcome.ERROR;
            action = "Policy text is not valid: " + e.getMessage();
        } catch (MissingValueException e) {
            if (skipIfNotApplicable) {
                return null;
            }
            outcome = PolicyOutcome.ERROR;
            action = e.getMessage();
        } catch (PolicyEvaluationException e) {
            outcome = PolicyOutcome.ERROR;
            action = e.getMessage();
        }
        PolicyEvaluation row = PolicyEvaluation.builder()
                .policy(policy)
                .zone(zone)
                .inputJson(json(ctx.asMap()))
                .outcome(outcome)
                .actionResult(action == null ? null : action.length() > 255 ? action.substring(0, 255) : action)
                .evaluatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();
        evaluationRepository.save(row);
        return row;
    }

    private Map<String, Double> liveReadings(Long zoneId) {
        List<Sensor> inZone = sensorRepository.findActive().stream()
                .filter(s -> s.getZone().getId().equals(zoneId))
                .toList();
        return averages(inZone);
    }

    private Map<String, Double> averages(List<Sensor> sensors) {
        Map<String, List<BigDecimal>> byType = new HashMap<>();
        for (Sensor s : sensors) {
            SensorReading latest = readingRepository.findLatest(s.getId());
            if (latest != null) {
                byType.computeIfAbsent(s.getSensorType().name().toLowerCase(), k -> new ArrayList<>())
                        .add(latest.getValue());
            }
        }
        Map<String, Double> out = new HashMap<>();
        byType.forEach((type, values) -> {
            BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            out.put(type, sum.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP).doubleValue());
        });
        return out;
    }

    private void fillFromZone(EvaluationContext ctx, Zone zone, Map<String, Double> readings) {
        ctx.put("zone", zone.getLandUse().name());
        ctx.put("hour", (double) LocalTime.now(CITY_CLOCK).getHour());
        readings.forEach(ctx::put);
    }

    private void applyOverrides(EvaluationContext ctx, EvaluateRequest req) {
        if (req.getNoise() != null) {
            ctx.put("noise", finite(req.getNoise(), "noise"));
        }
        if (req.getTemperature() != null) {
            ctx.put("temperature", finite(req.getTemperature(), "temperature"));
        }
        if (req.getAirQuality() != null) {
            ctx.put("air_quality", finite(req.getAirQuality(), "air quality"));
        }
        if (req.getHour() != null) {
            if (req.getHour() < 0 || req.getHour() > 23) {
                throw new BusinessRuleException("The hour must be between 0 and 23");
            }
            ctx.put("hour", req.getHour().doubleValue());
        }
        if (req.getZone() != null && !req.getZone().isBlank()) {
            try {
                ctx.put("zone", LandUse.valueOf(req.getZone().trim().toUpperCase()).name());
            } catch (IllegalArgumentException e) {
                throw new BusinessRuleException("Unknown land use '" + req.getZone() + "'. Use one of: "
                        + Arrays.toString(LandUse.values()));
            }
        }
    }

    private Double finite(Double v, String what) {
        if (v.isNaN() || v.isInfinite()) {
            throw new BusinessRuleException("The " + what + " value is not a number");
        }
        return v;
    }

    private String json(Map<String, Object> values) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> e : new TreeMap<>(values).entrySet()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append('"').append(e.getKey()).append("\":");
            if (e.getValue() instanceof Double d) {
                sb.append(BigDecimal.valueOf(d).stripTrailingZeros().toPlainString());
            } else {
                sb.append('"').append(String.valueOf(e.getValue()).replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
            }
        }
        return sb.append('}').toString();
    }

    private CityPolicy requirePolicy(Long id) {
        CityPolicy p = policyRepository.findById(id);
        if (p == null) {
            throw new ResourceNotFoundException("Policy " + id + " not found");
        }
        return p;
    }

    private EvaluationView view(PolicyEvaluation e) {
        return new EvaluationView(e.getId(), e.getPolicy().getId(), e.getPolicy().getName(),
                e.getZone() == null ? null : e.getZone().getName(), e.getInputJson(), e.getOutcome().name(),
                e.getActionResult(), e.getEvaluatedAt().toString());
    }
}