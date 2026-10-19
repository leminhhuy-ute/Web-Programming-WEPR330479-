package topicmanagement.service;

import java.time.LocalDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import topicmanagement.dto.response.ProfileResponse;
import topicmanagement.entity.User;
import topicmanagement.repository.UserRepository;
import topicmanagement.security.CurrentAccount;
import topicmanagement.security.LegacyPasswordVerifier;
import topicmanagement.security.PasswordPolicy;

@Service
@Transactional
public class ProfileService {
    private final CurrentAccount accounts;
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final AuditLogService audit;

    public ProfileService(CurrentAccount accounts, UserRepository users,
            PasswordEncoder passwords, AuditLogService audit) {
        this.accounts = accounts;
        this.users = users;
        this.passwords = passwords;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public ProfileResponse profile() {
        return map(accounts.user());
    }

    public void changePassword(String currentPassword, String newPassword, String confirmPassword) {
        User user = accounts.user();
        if (currentPassword == null || !matches(currentPassword, user.getPasswordHash()))
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng.");
        if (newPassword == null || !newPassword.equals(confirmPassword))
            throw new IllegalArgumentException("Xác nhận mật khẩu mới không khớp.");
        if (newPassword.equals(currentPassword))
            throw new IllegalArgumentException("Mật khẩu mới phải khác mật khẩu hiện tại.");
        PasswordPolicy.requireStrong(newPassword);
        user.setPasswordHash(passwords.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        users.save(user);
        audit.log(user, "CHANGE_OWN_PASSWORD", "User", user.getId(), null, null);
    }

    private boolean matches(String raw, String hash) {
        return hash.startsWith("$2") ? passwords.matches(raw, hash) : LegacyPasswordVerifier.matches(raw, hash);
    }

    private ProfileResponse map(User user) {
        return new ProfileResponse(user.getUsername(), user.getUserCode(), user.getFullName(),
            user.getEmail(), user.getRole().name(),
            user.getDepartment() == null ? null : user.getDepartment().getName(),
            user.getStudentClass());
    }
}
