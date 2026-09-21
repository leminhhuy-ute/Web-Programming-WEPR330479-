package topicmanagement.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import topicmanagement.entity.User;
import topicmanagement.enums.UserStatus;
import topicmanagement.repository.UserRepository;

@Component
public class DatabaseAuthenticationProvider implements AuthenticationProvider {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LegacyPasswordVerifier legacyVerifier;

    @Autowired
    public DatabaseAuthenticationProvider(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            LegacyPasswordVerifier legacyVerifier) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.legacyVerifier = legacyVerifier;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String identifier = authentication.getName();
        String password = authentication.getCredentials().toString();

        User user = userRepository.findByUsernameOrEmail(identifier)
                .orElseThrow(() -> new BadCredentialsException("Tên đăng nhập/email hoặc mật khẩu không chính xác."));

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new LockedException("Tài khoản đã bị khóa.");
        }
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new DisabledException("Tài khoản chưa được kích hoạt.");
        }

        boolean matches = passwordEncoder.matches(password, user.getPasswordHash());
        if (!matches && legacyVerifier.verifyLegacyHash(password, user.getPasswordHash())) {
            matches = true;
            user.setPasswordHash(passwordEncoder.encode(password));
            userRepository.save(user);
        }

        if (!matches) {
            throw new BadCredentialsException("Tên đăng nhập/email hoặc mật khẩu không chính xác.");
        }

        CurrentUser principal = new CurrentUser(user);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
