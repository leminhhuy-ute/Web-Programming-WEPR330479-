package topicmanagement.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record TopicRegistrationResponse(
    Long id,
    Long groupId,
    String groupName,
    Long topicId,
    String topicCode,
    String topicTitle,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String decidedBy,
    LocalDateTime decidedAt,
    String reason,
    List<RegistrationHistoryResponse> history) {}
