package topicmanagement.student;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import topicmanagement.entity.RegistrationPeriod;
import topicmanagement.enums.RegistrationPeriodType;

class ReportSubmissionPolicyTest {
  @Test
  void allowsBeforeAndAtDeadlineButRejectsAfterDeadline() {
    LocalDateTime deadline = LocalDateTime.of(2026, 10, 18, 12, 0);
    RegistrationPeriod period = new RegistrationPeriod();
    period.setType(RegistrationPeriodType.COURSE);
    period.setStudentStartAt(deadline.minusDays(5));
    period.setStudentEndAt(deadline);

    assertDoesNotThrow(() -> ReportSubmissionPolicy.assertOpen(period, deadline.minusNanos(1)));
    assertDoesNotThrow(() -> ReportSubmissionPolicy.assertOpen(period, deadline));
    assertThrows(IllegalArgumentException.class,
        () -> ReportSubmissionPolicy.assertOpen(period, deadline.plusNanos(1)));
  }

  @Test
  void rejectsBeforeSubmissionWindow() {
    LocalDateTime start = LocalDateTime.of(2026, 10, 18, 12, 0);
    RegistrationPeriod period = new RegistrationPeriod();
    period.setType(RegistrationPeriodType.COURSE);
    period.setStudentStartAt(start);
    period.setStudentEndAt(start.plusDays(1));
    assertThrows(IllegalArgumentException.class,
        () -> ReportSubmissionPolicy.assertOpen(period, start.minusNanos(1)));
  }
}
