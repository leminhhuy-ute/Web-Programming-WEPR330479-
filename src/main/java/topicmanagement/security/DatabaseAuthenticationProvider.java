package topicmanagement.security;

import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import topicmanagement.entity.User;
import topicmanagement.repository.UserRepository;

@Component
public class DatabaseAuthenticationProvider implements AuthenticationProvider {
  private final UserRepository users;
  private final PasswordEncoder encoder;

  public DatabaseAuthenticationProvider(UserRepository users, PasswordEncoder encoder) {
    this.users = users;
    this.encoder = encoder;
  }

  @Override
  @Transactional
  public Authentication authenticate(Authentication request) {
    String identifier = String.valueOf(request.getPrincipal()).trim();
    String password = String.valueOf(request.getCredentials());
    User user =
        users
            .findByUsernameIgnoreCaseOrEmailIgnoreCase(identifier, identifier)
            .orElseThrow(
                () -> new BadCredentialsException("Tên đăng nhập/email hoặc mật khẩu không đúng."));
    boolean bcrypt = user.getPasswordHash().startsWith("$2");
    boolean valid =
        bcrypt
            ? encoder.matches(password, user.getPasswordHash())
            : LegacyPasswordVerifier.matches(password, user.getPasswordHash());
    if (!valid || !CurrentUser.from(user).isEnabled())
      throw new BadCredentialsException(
          "Tên đăng nhập/email hoặc mật khẩu không đúng.");
    if (!bcrypt) {
      user.setPasswordHash(encoder.encode(password));
      users.save(user);
    }
    CurrentUser principal = CurrentUser.from(user);
    return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
  }

  @Override
  public boolean supports(Class<?> type) {
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(type);
  }
}
