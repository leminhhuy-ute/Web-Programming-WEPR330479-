package topicmanagement.security;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Proxy;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import topicmanagement.entity.User;
import topicmanagement.enums.Role;
import topicmanagement.enums.UserStatus;
import topicmanagement.repository.UserRepository;

class DatabaseAuthenticationProviderTest {

  private PasswordEncoder passwordEncoder;
  private User activeUser;
  private User lockedUser;
  private DatabaseAuthenticationProvider provider;

  @BeforeEach
  void setUp() {
    passwordEncoder = new BCryptPasswordEncoder();

    activeUser = new User();
    activeUser.setId(1L);
    activeUser.setUsername("dean01");
    activeUser.setEmail("dean01@hcmute.edu.vn");
    activeUser.setFullName("Nguyễn Quang Huy");
    activeUser.setPasswordHash(passwordEncoder.encode("Demo@12345"));
    activeUser.setRole(Role.DEAN);
    activeUser.setStatus(UserStatus.ACTIVE);

    lockedUser = new User();
    lockedUser.setId(2L);
    lockedUser.setUsername("locked01");
    lockedUser.setEmail("locked01@hcmute.edu.vn");
    lockedUser.setFullName("Người dùng bị khóa");
    lockedUser.setPasswordHash(passwordEncoder.encode("Demo@12345"));
    lockedUser.setRole(Role.LECTURER);
    lockedUser.setStatus(UserStatus.LOCKED);

    UserRepository mockRepository =
        (UserRepository)
            Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[] {UserRepository.class},
                (proxy, method, args) -> {
                  if ("findByUsernameIgnoreCaseOrEmailIgnoreCase".equals(method.getName())) {
                    String username = (String) args[0];
                    String email = (String) args[1];

                    if (activeUser.getUsername().equalsIgnoreCase(username)
                        || activeUser.getEmail().equalsIgnoreCase(email)) {
                      return Optional.of(activeUser);
                    }

                    if (lockedUser.getUsername().equalsIgnoreCase(username)
                        || lockedUser.getEmail().equalsIgnoreCase(email)) {
                      return Optional.of(lockedUser);
                    }

                    return Optional.empty();
                  }

                  return null;
                });

    provider = new DatabaseAuthenticationProvider(mockRepository, passwordEncoder);
  }

  @Test
  void test1_ValidUsernameAndValidPassword_Passes() {
    Authentication request =
        UsernamePasswordAuthenticationToken.unauthenticated(
            "dean01",
            "Demo@12345");

    Authentication result = provider.authenticate(request);

    assertNotNull(result);
    assertTrue(result.isAuthenticated());
    assertEquals("dean01", result.getName());
    assertEquals(
        "ROLE_DEAN",
        result.getAuthorities().iterator().next().getAuthority());
  }

  @Test
  void test2_ValidEmailAndValidPassword_Passes() {
    Authentication request =
        UsernamePasswordAuthenticationToken.unauthenticated(
            "dean01@hcmute.edu.vn",
            "Demo@12345");

    Authentication result = provider.authenticate(request);

    assertNotNull(result);
    assertTrue(result.isAuthenticated());

    // Principal name must still be username, not email.
    assertEquals("dean01", result.getName());
    assertEquals(
        "ROLE_DEAN",
        result.getAuthorities().iterator().next().getAuthority());
  }

  @Test
  void test2b_CaseInsensitiveEmail_Passes() {
    Authentication request =
        UsernamePasswordAuthenticationToken.unauthenticated(
            "Dean01@HCMUTE.EDU.VN",
            "Demo@12345");

    Authentication result = provider.authenticate(request);

    assertNotNull(result);
    assertTrue(result.isAuthenticated());
    assertEquals("dean01", result.getName());
  }

  @Test
  void test3_InvalidUsernameAndValidPassword_ThrowsBadCredentialsException() {
    Authentication request =
        UsernamePasswordAuthenticationToken.unauthenticated(
            "wronguser",
            "Demo@12345");

    BadCredentialsException ex =
        assertThrows(
            BadCredentialsException.class,
            () -> provider.authenticate(request));

    assertEquals(
        "Tên đăng nhập/email hoặc mật khẩu không đúng.",
        ex.getMessage());
  }

  @Test
  void test4_InvalidEmailAndValidPassword_ThrowsBadCredentialsException() {
    Authentication request =
        UsernamePasswordAuthenticationToken.unauthenticated(
            "wrong@hcmute.edu.vn",
            "Demo@12345");

    BadCredentialsException ex =
        assertThrows(
            BadCredentialsException.class,
            () -> provider.authenticate(request));

    assertEquals(
        "Tên đăng nhập/email hoặc mật khẩu không đúng.",
        ex.getMessage());
  }

  @Test
  void test5_ValidUsernameOrEmailWithWrongPassword_ThrowsBadCredentialsException() {
    Authentication requestUsername =
        UsernamePasswordAuthenticationToken.unauthenticated(
            "dean01",
            "WrongPassword");

    BadCredentialsException exUser =
        assertThrows(
            BadCredentialsException.class,
            () -> provider.authenticate(requestUsername));

    assertEquals(
        "Tên đăng nhập/email hoặc mật khẩu không đúng.",
        exUser.getMessage());

    Authentication requestEmail =
        UsernamePasswordAuthenticationToken.unauthenticated(
            "dean01@hcmute.edu.vn",
            "WrongPassword");

    BadCredentialsException exEmail =
        assertThrows(
            BadCredentialsException.class,
            () -> provider.authenticate(requestEmail));

    assertEquals(
        "Tên đăng nhập/email hoặc mật khẩu không đúng.",
        exEmail.getMessage());
  }

  @Test
  void test6_DisabledOrLockedUser_LoginByUsername_ThrowsBadCredentialsException() {
    Authentication request =
        UsernamePasswordAuthenticationToken.unauthenticated(
            "locked01",
            "Demo@12345");

    BadCredentialsException ex =
        assertThrows(
            BadCredentialsException.class,
            () -> provider.authenticate(request));

    assertEquals(
        "Tên đăng nhập/email hoặc mật khẩu không đúng.",
        ex.getMessage());
  }

  @Test
  void test7_DisabledOrLockedUser_LoginByEmail_ThrowsBadCredentialsException() {
    Authentication request =
        UsernamePasswordAuthenticationToken.unauthenticated(
            "locked01@hcmute.edu.vn",
            "Demo@12345");

    BadCredentialsException ex =
        assertThrows(
            BadCredentialsException.class,
            () -> provider.authenticate(request));

    assertEquals(
        "Tên đăng nhập/email hoặc mật khẩu không đúng.",
        ex.getMessage());
  }
}