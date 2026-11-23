package lk.jiat.smartcity360.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import lk.jiat.smartcity360.domain.Notification;
import lk.jiat.smartcity360.domain.User;
import lk.jiat.smartcity360.domain.enums.NotificationType;
import lk.jiat.smartcity360.dto.NotificationView;
import lk.jiat.smartcity360.repository.NotificationRepository;
import lk.jiat.smartcity360.repository.UserRepository;
import lk.jiat.smartcity360.service.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

@Stateless
public class NotificationService {

    public record Delivery(long userId, String email, String title, String message) {
    }

    @EJB
    private NotificationRepository notificationRepository;
    @EJB
    private UserRepository userRepository;

    // ---------------------------------------------------------------- write (MDB)

    public Optional<Delivery> store(long userId, NotificationType type, String title, String message) {
        User user = userRepository.findById(userId);
        if (user == null) {
            return Optional.empty();
        }
        Notification n = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .build();
        notificationRepository.save(n);
        return Optional.of(new Delivery(userId, user.getEmail(), title, message));
    }

    // ---------------------------------------------------------------- reads (own notifications only)

    public List<NotificationView> list(Long userId, boolean unreadOnly) {
        return notificationRepository.findByUser(userId).stream()
                .filter(n -> !unreadOnly || !Boolean.TRUE.equals(n.getIsRead()))
                .limit(200)
                .map(this::view)
                .toList();
    }

    public long unreadCount(Long userId) {
        return notificationRepository.countUnread(userId);
    }

    // ---------------------------------------------------------------- mark as read

    public void markRead(Long id, Long userId) {
        Notification n = notificationRepository.findById(id);
        if (n == null || !n.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Notification " + id + " not found");
        }
        n.setIsRead(true);
    }

    public int markAllRead(Long userId) {
        int changed = 0;
        for (Notification n : notificationRepository.findByUser(userId)) {
            if (!Boolean.TRUE.equals(n.getIsRead())) {
                n.setIsRead(true);
                changed++;
            }
        }
        return changed;
    }

    private NotificationView view(Notification n) {
        return new NotificationView(n.getId(), n.getType().name(), n.getTitle(), n.getMessage(),
                Boolean.TRUE.equals(n.getIsRead()),
                n.getCreatedAt() == null ? null : n.getCreatedAt().toString());
    }
}
