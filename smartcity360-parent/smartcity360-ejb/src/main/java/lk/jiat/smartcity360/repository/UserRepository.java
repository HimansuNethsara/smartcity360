package lk.jiat.smartcity360.repository;

import jakarta.ejb.Stateless;
import lk.jiat.smartcity360.domain.User;

import java.util.Optional;

@Stateless
public class UserRepository extends AbstractRepository<User, Long> {

    @Override
    protected Class<User> entityClass() {
        return User.class;
    }

    public Optional<User> findByUsername(String username) {
        return em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }

    public Optional<User> findByUsernameWithRoles(String username) {
        return em.createQuery(
                        "SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username",
                        User.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }

    public User findByIdWithRoles(Long id) {
        return em.createQuery("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :id", User.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public Optional<User> findByEmail(String email) {
        return em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst();
    }

    public boolean usernameTaken(String username) {
        return findByUsername(username).isPresent();
    }

    public boolean emailTaken(String email) {
        return findByEmail(email).isPresent();
    }

    public java.util.List<User> findEnabledByRole(String roleName) {
        return em.createQuery(
                        "SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name = :role AND u.enabled = true ORDER BY u.id",
                        User.class)
                .setParameter("role", roleName)
                .getResultList();
    }
}