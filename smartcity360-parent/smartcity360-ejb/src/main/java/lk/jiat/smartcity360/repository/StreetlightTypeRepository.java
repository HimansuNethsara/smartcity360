package lk.jiat.smartcity360.repository;

import jakarta.ejb.Stateless;
import lk.jiat.smartcity360.domain.StreetlightType;

import java.util.Optional;

@Stateless
public class StreetlightTypeRepository extends AbstractRepository<StreetlightType, Integer> {

    @Override
    protected Class<StreetlightType> entityClass() {
        return StreetlightType.class;
    }

    public Optional<StreetlightType> findByCode(String typeCode) {
        return em.createQuery("SELECT t FROM StreetlightType t WHERE t.typeCode = :code", StreetlightType.class)
                .setParameter("code", typeCode)
                .getResultStream()
                .findFirst();
    }
}