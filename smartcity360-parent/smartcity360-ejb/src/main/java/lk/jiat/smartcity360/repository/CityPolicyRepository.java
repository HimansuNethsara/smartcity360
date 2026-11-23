package lk.jiat.smartcity360.repository;

import jakarta.ejb.Stateless;
import lk.jiat.smartcity360.domain.CityPolicy;

import java.util.List;
import java.util.Optional;

@Stateless
public class CityPolicyRepository extends AbstractRepository<CityPolicy, Long> {

    @Override
    protected Class<CityPolicy> entityClass() {
        return CityPolicy.class;
    }

    public Optional<CityPolicy> findByName(String name) {
        return em.createQuery("SELECT p FROM CityPolicy p WHERE p.name = :name", CityPolicy.class)
                .setParameter("name", name)
                .getResultStream()
                .findFirst();
    }

    public List<CityPolicy> findEnabled() {
        return em.createQuery("SELECT p FROM CityPolicy p WHERE p.enabled = true", CityPolicy.class)
                .getResultList();
    }

    public List<CityPolicy> findAllByName() {
        return em.createQuery("SELECT p FROM CityPolicy p ORDER BY p.name", CityPolicy.class).getResultList();
    }
}