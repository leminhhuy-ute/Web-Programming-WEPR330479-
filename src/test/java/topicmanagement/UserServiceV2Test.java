package topicmanagement;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Proxy;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import topicmanagement.entity.User;
import topicmanagement.enums.Role;
import topicmanagement.enums.UserStatus;
import topicmanagement.exception.ConflictException;
import topicmanagement.repository.UserRepository;
import topicmanagement.service.UserServiceV2;

class UserServiceV2Test {
  @Test
  void cannotLockOrDeleteTheLastActiveDean() {
    User dean = activeDean();
    UserServiceV2 service = new UserServiceV2(repository(dean, 1), null, null);

    assertThrows(ConflictException.class, () -> service.setStatus(1L, UserStatus.LOCKED));
    assertThrows(ConflictException.class, () -> service.delete(1L, 2L));
  }

  @Test
  void canLockADeanWhenAnotherActiveDeanRemains() {
    User dean = activeDean();
    UserServiceV2 service = new UserServiceV2(repository(dean, 2), null, null);

    service.setStatus(1L, UserStatus.LOCKED);

    assertEquals(UserStatus.LOCKED, dean.getStatus());
  }

  private User activeDean() {
    User dean = new User();
    dean.setId(1L);
    dean.setRole(Role.DEAN);
    dean.setStatus(UserStatus.ACTIVE);
    return dean;
  }

  private UserRepository repository(User dean, long activeDeanCount) {
    return (UserRepository)
        Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class<?>[] {UserRepository.class},
            (proxy, method, arguments) -> {
              return switch (method.getName()) {
                case "findById" -> Optional.of(dean);
                case "countByRoleAndStatus" -> activeDeanCount;
                case "save" -> arguments[0];
                case "delete" -> null;
                default -> throw new UnsupportedOperationException(method.getName());
              };
            });
  }
}
