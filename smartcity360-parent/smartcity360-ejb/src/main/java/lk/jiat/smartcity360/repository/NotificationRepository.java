package lk.jiat.smartcity360.repository;

import jakarta.ejb.Stateless;
import lk.jiat.smartcity360.domain.Notification;

import java.util.List;

@Stateless
public class NotificationRepository extends AbstractRepository<Notification, Long> {

    @Override
    protected Class<Notification> entityClass() {
        return Notification.class;
    }

    public List<Notification> findByUser(Long userId) {
        return em.createQuery(
                        "SELECT n FROM Notification n WHERE n.user.id = :uid ORDER BY n.createdAt DESC",
                        Notification.class)
                .setParameter("uid", userId)
                .getResultList();
    }

    public long countUnread(Long userId) {
        return em.createQuery(
                        "SELECT COUNT(n) FROM Notification n WHERE n.user.id = :uid AND n.isRead = false",
                        Long.class)
                .setParameter("uid", userId)
                .getSingleResult();
    }
}