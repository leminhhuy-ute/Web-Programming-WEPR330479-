package topicmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import topicmanagement.enums.RegistrationPeriodType;

public record RegistrationPeriodRequest(
    @NotBlank @Size(max = 255) String name,
    @NotNull RegistrationPeriodType type,
    @NotNull LocalDateTime lecturerStartAt,
    @NotNull LocalDateTime lecturerEndAt,
    @NotNull LocalDateTime studentStartAt,
    @NotNull LocalDateTime studentEndAt,
    LocalDateTime reviewDeadline,
    LocalDate defenseDate) {}
