package topicmanagement.security;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Service;

/** Lightweight per-node protection against repeated credential guessing. */
@Service
public class LoginAttemptService {
  private static final int MAX_FAILURES = 5;
  private static final Duration WINDOW = Duration.ofMinutes(15);
  private final Map<String, Deque<Instant>> failures = new ConcurrentHashMap<>();

  public void assertAllowed(String address, String identifier) {
    String key = key(address, identifier);
    Deque<Instant> attempts = failures.get(key);
    if (attempts == null) return;
    synchronized (attempts) {
      discardExpired(attempts);
      if (attempts.size() >= MAX_FAILURES)
        throw new LockedException("Đăng nhập tạm khóa trong 15 phút do thử sai quá nhiều lần.");
      if (attempts.isEmpty()) failures.remove(key, attempts);
    }
  }

  public void failed(String address, String identifier) {
    Deque<Instant> attempts = failures.computeIfAbsent(key(address, identifier), unused -> new ArrayDeque<>());
    synchronized (attempts) {
      discardExpired(attempts);
      attempts.addLast(Instant.now());
    }
  }

  public void succeeded(String address, String identifier) {
    failures.remove(key(address, identifier));
  }

  private void discardExpired(Deque<Instant> attempts) {
    Instant threshold = Instant.now().minus(WINDOW);
    while (!attempts.isEmpty() && attempts.getFirst().isBefore(threshold)) attempts.removeFirst();
  }

  private String key(String address, String identifier) {
    return (address == null ? "unknown" : address) + "|"
        + (identifier == null ? "" : identifier.strip().toLowerCase(Locale.ROOT));
  }
}
