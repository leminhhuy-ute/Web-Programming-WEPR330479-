package topicmanagement.security;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.LockedException;

class LoginAttemptServiceTest {
  @Test
  void entriesExpireAndTrackingRemainsBounded() {
    Instant start = Instant.parse("2026-01-01T00:00:00Z");
    LoginAttemptService service = new LoginAttemptService(
        Clock.fixed(start, ZoneOffset.UTC), Duration.ofMinutes(15), 10);
    for (int i = 0; i < 100; i++) service.failed("127.0.0.1", "user-" + i);
    assertTrue(service.trackedKeyCount() <= 10);

    service.purgeExpired(start.plus(Duration.ofMinutes(16)));
    assertEquals(0, service.trackedKeyCount());
  }

  @Test
  void repeatedFailuresAreLimitedByIdentifierAndByAddress() {
    LoginAttemptService service = new LoginAttemptService();
    for (int i = 0; i < 5; i++) service.failed("10.0.0.1", "same-user");
    assertThrows(LockedException.class, () -> service.assertAllowed("10.0.0.1", "same-user"));
    assertDoesNotThrow(() -> service.assertAllowed("10.0.0.1", "another-user"));

    LoginAttemptService addressLimited = new LoginAttemptService();
    for (int i = 0; i < 50; i++) addressLimited.failed("10.0.0.2", "rotating-" + i);
    assertThrows(LockedException.class,
        () -> addressLimited.assertAllowed("10.0.0.2", "never-seen-before"));
  }
}
