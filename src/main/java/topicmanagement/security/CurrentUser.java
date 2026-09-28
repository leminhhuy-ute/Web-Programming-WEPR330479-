package topicmanagement.security;

import java.util.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import topicmanagement.entity.User;

public record CurrentUser(
    Long id,
    String username,
    String fullName,
    String role,
    boolean enabled,
    Long departmentId,
    String departmentCode,
    String departmentName,
    java.time.LocalDateTime authenticatedAt
) implements UserDetails {

    public CurrentUser(Long id, String username, String fullName, String role, boolean enabled) {
        this(id, username, fullName, role, enabled, null, null, null, java.time.LocalDateTime.now());
    }

    public CurrentUser(Long id, String username, String fullName, String role, boolean enabled,
        Long departmentId, String departmentCode, String departmentName) {
        this(id, username, fullName, role, enabled, departmentId, departmentCode, departmentName, java.time.LocalDateTime.now());
    }

    public static CurrentUser from(User u) {
        return from(u, java.time.LocalDateTime.now());
    }

    public static CurrentUser from(User u, java.time.LocalDateTime authenticatedAt) {
        topicmanagement.entity.Department d = u.getDepartment();
        return new CurrentUser(
            u.getId(),
            u.getUsername(),
            u.getFullName(),
            u.getRole().name(),
            u.getStatus().name().equals("ACTIVE"),
            d != null ? d.getId() : null,
            d != null ? d.getCode() : null,
            d != null ? d.getName() : null,
            authenticatedAt != null ? authenticatedAt : java.time.LocalDateTime.now()
        );
    }

    public DepartmentView getDepartment() {
        if (departmentId == null) return null;
        return new DepartmentView(departmentId, departmentCode, departmentName);
    }

    public record DepartmentView(Long id, String code, String name) {}

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
