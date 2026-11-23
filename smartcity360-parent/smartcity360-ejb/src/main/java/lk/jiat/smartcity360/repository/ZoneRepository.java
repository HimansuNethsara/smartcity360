package lk.jiat.smartcity360.repository;

import jakarta.ejb.Stateless;
import lk.jiat.smartcity360.domain.Zone;
import lk.jiat.smartcity360.domain.enums.ZoneType;

import java.util.List;
import java.util.Optional;

@Stateless
public class ZoneRepository extends AbstractRepository<Zone, Long> {

    @Override
    protected Class<Zone> entityClass() {
        return Zone.class;
    }

    public Optional<Zone> findByCode(String code) {
        return em.createQuery("SELECT z FROM Zone z WHERE z.code = :code", Zone.class)
                .setParameter("code", code)
                .getResultStream()
                .findFirst();
    }

    public List<Zone> findRoots() {
        return em.createQuery("SELECT z FROM Zone z WHERE z.parent IS NULL", Zone.class)
                .getResultList();
    }

    public List<Zone> findChildren(Long parentId) {
        return em.createQuery("SELECT z FROM Zone z WHERE z.parent.id = :pid ORDER BY z.name", Zone.class)
                .setParameter("pid", parentId)
                .getResultList();
    }

    public List<Zone> findByType(ZoneType type) {
        return em.createQuery("SELECT z FROM Zone z WHERE z.zoneType = :t ORDER BY z.name", Zone.class)
                .setParameter("t", type)
                .getResultList();
    }
}