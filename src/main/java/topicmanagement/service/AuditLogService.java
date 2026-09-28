package topicmanagement.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.*;
import topicmanagement.entity.*;
import topicmanagement.repository.AuditLogRepository;

@Service
public class AuditLogService {
    private final AuditLogRepository logs;

    public AuditLogService(AuditLogRepository logs) { this.logs = logs; }

    @Transactional
    public void log(User user, String action, String entity, Object entityId, String oldValue, String newValue) {
        AuditLog entry = new AuditLog();
        entry.setUser(user);
        entry.setAction(limit(action, 80));
        entry.setEntity(limit(entity, 80));
        entry.setEntityId(entityId == null ? null : limit(String.valueOf(entityId), 80));
        entry.setOldValue(oldValue);
        entry.setNewValue(newValue);
        entry.setIpAddress(ipAddress());
        logs.save(entry);
    }

    private String ipAddress() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servlet)) return null;
        HttpServletRequest request = servlet.getRequest();
        return limit(request.getRemoteAddr(), 64);
    }

    private String limit(String value, int max) {
        if (value == null) return null;
        return value.length() <= max ? value : value.substring(0, max);
    }
}
