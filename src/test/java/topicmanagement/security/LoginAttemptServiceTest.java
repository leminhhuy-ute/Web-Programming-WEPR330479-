package topicmanagement.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.LockedException;

class LoginAttemptServiceTest {

  @Test
  void blocksAnIdentifierAfterFiveFailuresFromTheSameAddress() {
    LoginAttemptService service = new LoginAttemptService();

    for (int attempt = 0; attempt < 5; attempt++) {
      service.failed("127.0.0.1", "Dean01");
    }

    assertThrows(LockedException.class,
        () -> service.assertAllowed("127.0.0.1", "dean01"));
  }

  @Test
  void successfulLoginClearsTheFailureHistory() {
    LoginAttemptService service = new LoginAttemptService();

    for (int attempt = 0; attempt < 5; attempt++) {
      service.failed("127.0.0.1", "dean01");
    }
    service.succeeded("127.0.0.1", "DEAN01");

    assertDoesNotThrow(() -> service.assertAllowed("127.0.0.1", "dean01"));
  }

  @Test
  void failuresAreScopedByAddressAndIdentifier() {
    LoginAttemptService service = new LoginAttemptService();

    for (int attempt = 0; attempt < 5; attempt++) {
      service.failed("127.0.0.1", "dean01");
    }

    assertDoesNotThrow(() -> service.assertAllowed("127.0.0.2", "dean01"));
    assertDoesNotThrow(() -> service.assertAllowed("127.0.0.1", "lecturer01"));
  }
}
