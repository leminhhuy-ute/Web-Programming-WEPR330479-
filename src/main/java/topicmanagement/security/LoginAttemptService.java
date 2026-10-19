package topicmanagement.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Service;

/** Bounded per-node protection against repeated credential guessing. */
@Service
public class LoginAttemptService {
  static final int DEFAULT_MAX_TRACKED_KEYS = 10_000;
  private static final int MAX_IDENTIFIER_FAILURES = 5;
  private static final int MAX_ADDRESS_FAILURES = 50;
  private static final Duration DEFAULT_WINDOW = Duration.ofMinutes(15);
  private static final long MAINTENANCE_INTERVAL = 64;
  private final Map<String, Deque<Instant>> failures = new ConcurrentHashMap<>();
  private final Object capacityLock = new Object();
  private final AtomicLong operations = new AtomicLong();
  private final Clock clock;
  private final Duration window;
  private final int maxTrackedKeys;

  public LoginAttemptService() { this(Clock.systemUTC(), DEFAULT_WINDOW, DEFAULT_MAX_TRACKED_KEYS); }

  LoginAttemptService(Clock clock, Duration window, int maxTrackedKeys) {
    this.clock = clock;
    this.window = window;
    this.maxTrackedKeys = Math.max(2, maxTrackedKeys);
  }

  public void assertAllowed(String address, String identifier) {
    Instant now = clock.instant();
    maintain(now);
    assertBelowLimit(identifierKey(address, identifier), MAX_IDENTIFIER_FAILURES, now);
    assertBelowLimit(addressKey(address), MAX_ADDRESS_FAILURES, now);
  }

  public void failed(String address, String identifier) {
    Instant now = clock.instant();
    maintain(now);
    addFailure(identifierKey(address, identifier), now);
    addFailure(addressKey(address), now);
  }

  public void succeeded(String address, String identifier) {
    failures.remove(identifierKey(address, identifier));
  }

  private void assertBelowLimit(String key, int limit, Instant now) {
    Deque<Instant> attempts = failures.get(key);
    if (attempts == null) return;
    synchronized (attempts) {
      discardExpired(attempts, now);
      if (attempts.size() >= limit)
        throw new LockedException("Đăng nhập tạm khóa trong 15 phút do thử sai quá nhiều lần.");
      if (attempts.isEmpty()) failures.remove(key, attempts);
    }
  }

  private void addFailure(String key, Instant now) {
    Deque<Instant> attempts = failures.get(key);
    if (attempts == null) {
      synchronized (capacityLock) {
        attempts = failures.get(key);
        if (attempts == null) {
          ensureCapacity(now);
          attempts = new ArrayDeque<>();
          failures.put(key, attempts);
        }
      }
    }
    synchronized (attempts) {
      discardExpired(attempts, now);
      attempts.addLast(now);
    }
  }

  private void maintain(Instant now) {
    if (operations.incrementAndGet() % MAINTENANCE_INTERVAL == 0 || failures.size() >= maxTrackedKeys)
      purgeExpired(now);
  }

  private void ensureCapacity(Instant now) {
    if (failures.size() < maxTrackedKeys) return;
    purgeExpired(now);
    while (failures.size() >= maxTrackedKeys) {
      String oldestKey = failures.entrySet().stream()
          .min((left, right) -> lastAttempt(left.getValue()).compareTo(lastAttempt(right.getValue())))
          .map(Map.Entry::getKey).orElse(null);
      if (oldestKey == null) return;
      failures.remove(oldestKey);
    }
  }

  void purgeExpired(Instant now) {
    failures.forEach((key, attempts) -> {
      synchronized (attempts) {
        discardExpired(attempts, now);
        if (attempts.isEmpty()) failures.remove(key, attempts);
      }
    });
  }

  int trackedKeyCount() { return failures.size(); }

  private Instant lastAttempt(Deque<Instant> attempts) {
    synchronized (attempts) { return attempts.isEmpty() ? Instant.MIN : attempts.getLast(); }
  }

  private void discardExpired(Deque<Instant> attempts, Instant now) {
    Instant threshold = now.minus(window);
    while (!attempts.isEmpty() && attempts.getFirst().isBefore(threshold)) attempts.removeFirst();
  }

  private String normalizedAddress(String address) {
    return address == null || address.isBlank() ? "unknown" : address.strip();
  }
  private String identifierKey(String address, String identifier) {
    return "identifier|" + normalizedAddress(address) + "|"
        + (identifier == null ? "" : identifier.strip().toLowerCase(Locale.ROOT));
  }
  private String addressKey(String address) { return "address|" + normalizedAddress(address); }
}
