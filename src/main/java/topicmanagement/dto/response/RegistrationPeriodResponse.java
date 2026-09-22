package topicmanagement.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import topicmanagement.entity.RegistrationPeriod;

public record RegistrationPeriodResponse(
    Long id,
    String name,
    String type,
    String typeLabel,
    LocalDateTime lecturerStartAt,
    LocalDateTime lecturerEndAt,
    LocalDateTime studentStartAt,
    LocalDateTime studentEndAt,
    LocalDateTime reviewDeadline,
    LocalDate defenseDate,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {
    public RegistrationPeriodResponse(RegistrationPeriod p) {
        this(p.getId(), p.getName(), p.getType().name(), p.getType().name(),
                p.getLecturerStartAt(), p.getLecturerEndAt(), p.getStudentStartAt(),
                p.getStudentEndAt(), p.getReviewDeadline(), p.getDefenseDate(),
                p.getCreatedAt(), p.getUpdatedAt());
    }
}
