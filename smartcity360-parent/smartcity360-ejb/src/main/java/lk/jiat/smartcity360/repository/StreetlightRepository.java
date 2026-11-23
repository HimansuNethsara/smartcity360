package lk.jiat.smartcity360.repository;

import jakarta.ejb.Stateless;
import lk.jiat.smartcity360.domain.Streetlight;
import lk.jiat.smartcity360.domain.enums.StreetlightStatus;

import java.util.List;

@Stateless
public class StreetlightRepository extends AbstractRepository<Streetlight, Long> {

    @Override
    protected Class<Streetlight> entityClass() {
        return Streetlight.class;
    }

    public List<Streetlight> findByZone(Long zoneId) {
        return em.createQuery("SELECT s FROM Streetlight s WHERE s.zone.id = :zid", Streetlight.class)
                .setParameter("zid", zoneId)
                .getResultList();
    }

    public List<Streetlight> findByStatus(StreetlightStatus status) {
        return em.createQuery("SELECT s FROM Streetlight s WHERE s.status = :st", Streetlight.class)
                .setParameter("st", status)
                .getResultList();
    }

    public long countByType(Integer typeId) {
        return em.createQuery("SELECT COUNT(s) FROM Streetlight s WHERE s.type.id = :tid", Long.class)
                .setParameter("tid", typeId)
                .getSingleResult();
    }

    public long countAll() {
        return em.createQuery("SELECT COUNT(s) FROM Streetlight s", Long.class).getSingleResult();
    }

    public List<Object[]> findAllRows() {
        return em.createQuery(
                        "SELECT s.id, s.zone.id, s.latitude, s.longitude, s.type, s.status, s.brightness " +
                                "FROM Streetlight s ORDER BY s.id",
                        Object[].class)
                .getResultList();
    }

    public long countFiltered(Long zoneId, StreetlightStatus status) {
        var q = em.createQuery("SELECT COUNT(s) FROM Streetlight s" + where(zoneId, status), Long.class);
        bind(q, zoneId, status);
        return q.getSingleResult();
    }

    public List<Streetlight> findPage(Long zoneId, StreetlightStatus status, int offset, int size) {
        var q = em.createQuery("SELECT s FROM Streetlight s JOIN FETCH s.type JOIN FETCH s.zone"
                + where(zoneId, status) + " ORDER BY s.id", Streetlight.class);
        bind(q, zoneId, status);
        return q.setFirstResult(offset).setMaxResults(size).getResultList();
    }

    public int bulkInsert(List<Object[]> rows) {
        final int chunk = 500;
        int total = 0;
        for (int from = 0; from < rows.size(); from += chunk) {
            List<Object[]> part = rows.subList(from, Math.min(from + chunk, rows.size()));
            StringBuilder sql = new StringBuilder(
                    "INSERT INTO streetlights (type_id, zone_id, latitude, longitude, status, brightness, installed_at) VALUES ");
            int p = 1;
            for (int i = 0; i < part.size(); i++) {
                sql.append(i == 0 ? "(" : ",(");
                for (int c = 0; c < 7; c++) {
                    sql.append(c == 0 ? "" : ",").append('?').append(p++);
                }
                sql.append(')');
            }
            var q = em.createNativeQuery(sql.toString());
            p = 1;
            for (Object[] row : part) {
                for (Object value : row) {
                    q.setParameter(p++, value);
                }
            }
            total += q.executeUpdate();
        }
        return total;
    }

    public int deleteByInstalledAt(java.time.LocalDate marker) {
        return em.createNativeQuery("DELETE FROM streetlights WHERE installed_at = ?1")
                .setParameter(1, marker)
                .executeUpdate();
    }

    private String where(Long zoneId, StreetlightStatus status) {
        StringBuilder sb = new StringBuilder();
        if (zoneId != null) {
            sb.append(" WHERE s.zone.id = :zid");
        }
        if (status != null) {
            sb.append(zoneId != null ? " AND" : " WHERE").append(" s.status = :st");
        }
        return sb.toString();
    }

    private void bind(jakarta.persistence.TypedQuery<?> q, Long zoneId, StreetlightStatus status) {
        if (zoneId != null) {
            q.setParameter("zid", zoneId);
        }
        if (status != null) {
            q.setParameter("st", status);
        }
    }
}