package topicmanagement;

import java.time.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import topicmanagement.entity.RegistrationPeriod;
import topicmanagement.enums.RegistrationPeriodType;
import topicmanagement.service.RegistrationPeriodServiceV2;

class RegistrationPeriodServiceTest {
    private final RegistrationPeriodServiceV2 service = new RegistrationPeriodServiceV2(null);

    private RegistrationPeriod valid(RegistrationPeriodType type) {
        RegistrationPeriod p = new RegistrationPeriod();
        p.setName("Đợt kiểm thử");
        p.setType(type);
        p.setLecturerStartAt(LocalDateTime.of(2026, 9, 1, 8, 0));
        p.setLecturerEndAt(LocalDateTime.of(2026, 9, 5, 17, 0));
        p.setStudentStartAt(LocalDateTime.of(2026, 9, 6, 8, 0));
        p.setStudentEndAt(LocalDateTime.of(2026, 9, 10, 17, 0));
        p.setReviewDeadline(LocalDateTime.of(2026, 12, 15, 17, 0));
        p.setDefenseDate(LocalDate.of(2026, 12, 20));
        return p;
    }

    @Test
    void courseAndNckhClearOptionalDates() {
        for (var t : new RegistrationPeriodType[] { RegistrationPeriodType.COURSE, RegistrationPeriodType.NCKH }) {
            var p = valid(t);
            service.validate(p);
            assertNull(p.getReviewDeadline());
            assertNull(p.getDefenseDate());
        }
    }

    @Test
    void tlcnRequiresReviewAndClearsDefense() {
        var p = valid(RegistrationPeriodType.TLCN);
        service.validate(p);
        assertNull(p.getDefenseDate());
        p.setReviewDeadline(null);
        assertThrows(IllegalArgumentException.class, () -> service.validate(p));
    }

    @Test
    void kltnRequiresBothDates() {
        var p = valid(RegistrationPeriodType.KLTN);
        service.validate(p);
        p.setDefenseDate(null);
        assertThrows(IllegalArgumentException.class, () -> service.validate(p));
    }

    @Test
    void chronologicalRulesRemainStrict() {
        var p = valid(RegistrationPeriodType.COURSE);
        p.setStudentStartAt(p.getLecturerEndAt());
        assertThrows(IllegalArgumentException.class, () -> service.validate(p));
    }
}
