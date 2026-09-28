package topicmanagement.dto.response;

import java.time.LocalDateTime;

public record RegistrationHistoryResponse(
    Long registrationId,
    String topicCode,
    String topicTitle,
    String oldStatus,
    String newStatus,
    String changedBy,
    LocalDateTime changedAt,
    String note) {}
