package topicmanagement.dto.response;

import java.time.LocalDateTime;

public record AnnouncementResponse(
    Long id,
    String title,
    String content,
    String audience,
    Long createdBy,
    LocalDateTime createdAt) {}
