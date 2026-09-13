package topicmanagement.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    LocalDateTime updatedAt) {}
