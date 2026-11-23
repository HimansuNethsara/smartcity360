package lk.jiat.smartcity360.repository;

import jakarta.ejb.Stateless;
import lk.jiat.smartcity360.domain.Role;

import java.util.Optional;

@Stateless
public class RoleRepository extends AbstractRepository<Role, Short> {

    @Override
    protected Class<Role> entityClass() {
        return Role.class;
    }

    public Optional<Role> findByName(String name) {
        return em.createQuery("SELECT r FROM Role r WHERE r.name = :name", Role.class)
                .setParameter("name", name)
                .getResultStream()
                .findFirst();
    }
}