package lk.jiat.smartcity360.service;

import jakarta.ejb.Stateless;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.jiat.smartcity360.domain.enums.PolicyOutcome;
import lk.jiat.smartcity360.domain.enums.ReportType;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Stateless
public class ReportBuilder {

    @PersistenceContext(unitName = "smartcity360PU")
    private EntityManager em;

    public String build(ReportType type, int hours) {
        LocalDateTime since = LocalDateTime.now(ZoneOffset.UTC).minusHours(hours);
        Map<String, Object> doc = switch (type) {
            case ENERGY_USAGE -> energy(since, hours);
            case COMPLAINT_SUMMARY -> complaints();
            case POLICY_VIOLATIONS -> violations(since, hours);
            case STREETLIGHT_STATUS -> streetlights();
        };
        doc.put("hours", hours);
        doc.put("generatedAt", LocalDateTime.now(ZoneOffset.UTC).toString());
        try (Jsonb jsonb = JsonbBuilder.create()) {
            return jsonb.toJson(doc);
        } catch (Exception e) {
            throw new IllegalStateException("Could not write the report: " + e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------- the four reports

    private Map<String, Object> energy(LocalDateTime since, int hours) {
        List<Object[]> rows = em.createQuery(
                        "SELECT m.meterCode, SUM(r.kwh) FROM MeterReading r JOIN r.meter m "
                                + "WHERE r.readAt >= :since GROUP BY m.meterCode ORDER BY m.meterCode", Object[].class)
                .setParameter("since", since).getResultList();
        return chart("Energy used per meter, last " + hours + " h", "bar", "kWh", rows, 2);
    }

    private Map<String, Object> complaints() {
        List<Object[]> byStatus = em.createQuery(
                "SELECT c.status, COUNT(c) FROM Complaint c GROUP BY c.status ORDER BY c.status", Object[].class)
                .getResultList();
        List<Object[]> bySeverity = em.createQuery(
                "SELECT c.severity, COUNT(c) FROM Complaint c GROUP BY c.severity ORDER BY c.severity", Object[].class)
                .getResultList();
        Map<String, Object> doc = chart("Complaints by status", "doughnut", "Complaints", byStatus, 0);
        doc.put("secondary", chart("Complaints by severity", "bar", "Complaints", bySeverity, 0));
        return doc;
    }

    private Map<String, Object> violations(LocalDateTime since, int hours) {
        List<Object[]> rows = em.createQuery(
                        "SELECT p.name, COUNT(e) FROM PolicyEvaluation e JOIN e.policy p "
                                + "WHERE e.outcome = :out AND e.evaluatedAt >= :since "
                                + "GROUP BY p.name ORDER BY COUNT(e) DESC", Object[].class)
                .setParameter("out", PolicyOutcome.VIOLATION)
                .setParameter("since", since).getResultList();
        return chart("Policy violations, last " + hours + " h", "bar", "Violations", rows, 0);
    }

    private Map<String, Object> streetlights() {
        List<Object[]> byStatus = em.createQuery(
                "SELECT s.status, COUNT(s) FROM Streetlight s GROUP BY s.status ORDER BY s.status", Object[].class)
                .getResultList();
        List<Object[]> byType = em.createQuery(
                "SELECT t.typeCode, COUNT(s) FROM Streetlight s JOIN s.type t GROUP BY t.typeCode ORDER BY t.typeCode",
                Object[].class).getResultList();
        Map<String, Object> doc = chart("Streetlights by status", "doughnut", "Lights", byStatus, 0);
        doc.put("secondary", chart("Streetlights by type", "bar", "Lights", byType, 0));
        return doc;
    }

    // ---------------------------------------------------------------- shared shape

    private Map<String, Object> chart(String title, String chartType, String seriesLabel,
                                             List<Object[]> rows, int decimals) {
        List<String> labels = new ArrayList<>();
        List<Number> data = new ArrayList<>();
        for (Object[] r : rows) {
            labels.add(String.valueOf(r[0]));
            Number n = r[1] == null ? 0 : (Number) r[1];
            data.add(decimals > 0
                    ? java.math.BigDecimal.valueOf(n.doubleValue()).setScale(decimals, java.math.RoundingMode.HALF_UP)
                    : (Number) n.longValue());
        }
        Map<String, Object> dataset = new LinkedHashMap<>();
        dataset.put("label", seriesLabel);
        dataset.put("data", data);

        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("title", title);
        doc.put("chartType", chartType);
        doc.put("labels", labels);
        doc.put("datasets", List.of(dataset));
        return doc;
    }
}
