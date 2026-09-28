package topicmanagement.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import topicmanagement.entity.User;
import topicmanagement.enums.*;
import topicmanagement.repository.UserRepository;

@Service
public class CurrentAccount {
    private final UserRepository users;
    public CurrentAccount(UserRepository users) { this.users = users; }
    public User user() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CurrentUser p))
            throw new AccessDeniedException("Vui lòng đăng nhập.");
        User u = users.findById(p.id()).orElseThrow(() -> new AccessDeniedException("Tài khoản không tồn tại."));
        if (u.getStatus() != UserStatus.ACTIVE) throw new AccessDeniedException("Tài khoản đã bị khóa.");
        return u;
    }
    public Long departmentId() { var d = user().getDepartment(); return d == null ? null : d.getId(); }
}
