package lk.jiat.smartcity360.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lk.jiat.smartcity360.domain.enums.ComplaintStatus;
import lk.jiat.smartcity360.domain.enums.MeterStatus;
import lk.jiat.smartcity360.domain.enums.PolicyOutcome;
import lk.jiat.smartcity360.domain.enums.StreetlightStatus;
import lk.jiat.smartcity360.repository.SensorRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Stateless
public class DashboardService {

    @PersistenceContext(unitName = "smartcity360PU")
    private EntityManager em;
    @EJB
    private SensorRepository sensorRepository;

    public Map<String, Object> summary(Long userId, boolean staff) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("scope", staff ? "city" : "mine");
        out.put("complaintsByStatus", complaintsByStatus(userId, staff));
        out.put("recentComplaints", recentComplaints(userId, staff));
        if (staff) {
            LocalDateTime since = LocalDateTime.now(ZoneOffset.UTC).minusHours(24);
            out.put("metersTotal", count("SELECT COUNT(m) FROM EnergyMeter m"));
            out.put("metersActive", em.createQuery(
                    "SELECT COUNT(m) FROM EnergyMeter m WHERE m.status = :s", Long.class)
                    .setParameter("s", MeterStatus.ACTIVE).getSingleResult());
            out.put("lightsByStatus", lightsByStatus());
            out.put("sensorsActive", sensorRepository.findActive().size());
            BigDecimal kwh = em.createQuery(
                    "SELECT SUM(r.kwh) FROM MeterReading r WHERE r.readAt >= :since", BigDecimal.class)
                    .setParameter("since", since).getSingleResult();
            out.put("energy24h", kwh == null ? BigDecimal.ZERO : kwh.setScale(1, RoundingMode.HALF_UP));
            out.put("violations24h", em.createQuery(
                    "SELECT COUNT(e) FROM PolicyEvaluation e WHERE e.outcome = :o AND e.evaluatedAt >= :since", Long.class)
                    .setParameter("o", PolicyOutcome.VIOLATION).setParameter("since", since).getSingleResult());
        }
        return out;
    }

    private long count(String jpql) {
        return em.createQuery(jpql, Long.class).getSingleResult();
    }

    private Map<String, Long> complaintsByStatus(Long userId, boolean staff) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (ComplaintStatus s : ComplaintStatus.values()) {
            map.put(s.name(), 0L);                                   // zero-fill so the chart has every slice
        }
        TypedQuery<Object[]> q = em.createQuery("SELECT c.status, COUNT(c) FROM Complaint c"
                + (staff ? "" : " WHERE c.citizen.id = :uid") + " GROUP BY c.status", Object[].class);
        if (!staff) {
            q.setParameter("uid", userId);
        }
        for (Object[] r : q.getResultList()) {
            map.put(((ComplaintStatus) r[0]).name(), (Long) r[1]);
        }
        return map;
    }

    private Map<String, Long> lightsByStatus() {
        Map<String, Long> map = new LinkedHashMap<>();
        for (StreetlightStatus s : StreetlightStatus.values()) {
            map.put(s.name(), 0L);
        }
        for (Object[] r : em.createQuery("SELECT s.status, COUNT(s) FROM Streetlight s GROUP BY s.status", Object[].class)
                .getResultList()) {
            map.put(((StreetlightStatus) r[0]).name(), (Long) r[1]);
        }
        return map;
    }

    private List<Map<String, Object>> recentComplaints(Long userId, boolean staff) {
        TypedQuery<Object[]> q = em.createQuery("SELECT c.id, c.title, c.severity, c.status FROM Complaint c"
                + (staff ? "" : " WHERE c.citizen.id = :uid") + " ORDER BY c.id DESC", Object[].class);
        if (!staff) {
            q.setParameter("uid", userId);
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : q.setMaxResults(5).getResultList()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r[0]);
            m.put("title", r[1]);
            m.put("severity", String.valueOf(r[2]));
            m.put("status", String.valueOf(r[3]));
            list.add(m);
        }
        return list;
    }
}
