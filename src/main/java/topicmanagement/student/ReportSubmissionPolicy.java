package topicmanagement.student;

import java.time.LocalDateTime;
import topicmanagement.entity.RegistrationPeriod;
import topicmanagement.enums.RegistrationPeriodType;

final class ReportSubmissionPolicy {
  private ReportSubmissionPolicy() {}

  static void assertOpen(RegistrationPeriod period, LocalDateTime now) {
    if (period == null) return;
    if (now.isBefore(period.getStudentStartAt()))
      throw new IllegalArgumentException("Chưa đến thời gian nộp báo cáo.");
    if (period.getType() == RegistrationPeriodType.KLTN && period.getDefenseDate() != null) {
      if (now.toLocalDate().isAfter(period.getDefenseDate()))
        throw new IllegalArgumentException("Đã quá ngày báo cáo hội đồng.");
    } else if (period.getReviewDeadline() != null) {
      if (now.isAfter(period.getReviewDeadline()))
        throw new IllegalArgumentException("Đã quá hạn nộp báo cáo.");
    } else if (now.isAfter(period.getStudentEndAt())) {
      throw new IllegalArgumentException("Đã quá hạn nộp báo cáo của đợt này.");
    }
  }
}
