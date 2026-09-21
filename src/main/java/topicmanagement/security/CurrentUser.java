package topicmanagement.security;

import java.util.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import topicmanagement.entity.User;

public record CurrentUser(Long id, String username, String fullName, String role, boolean enabled)
        implements UserDetails {
    public static CurrentUser from(User u) {
        return new CurrentUser(u.getId(), u.getUsername(), u.getFullName(), u.getRole().name(),
                u.getStatus().name().equals("ACTIVE"));
    }

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
