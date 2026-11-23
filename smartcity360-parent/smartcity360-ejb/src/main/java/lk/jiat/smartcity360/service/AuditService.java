package lk.jiat.smartcity360.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import lk.jiat.smartcity360.domain.AuditLog;
import lk.jiat.smartcity360.repository.AuditLogRepository;
import lk.jiat.smartcity360.repository.UserRepository;

@Stateless
public class AuditService {

    @EJB
    private AuditLogRepository auditLogRepository;
    @EJB
    private UserRepository userRepository;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void record(Long userId, String action, String entityType, Long entityId, String detailJson) {
        auditLogRepository.save(AuditLog.builder()
                .user(userId == null ? null : userRepository.findById(userId))
                .action(cut(action, 60))
                .entityType(cut(entityType, 40))
                .entityId(entityId)
                .detailJson(detailJson)
                .build());
    }

    private String cut(String s, int max) {
        return s == null ? "" : (s.length() <= max ? s : s.substring(0, max));
    }
}
