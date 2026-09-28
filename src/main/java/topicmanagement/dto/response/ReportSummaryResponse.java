package topicmanagement.dto.response;

import java.time.LocalDateTime;

public record ReportSummaryResponse(
    Long id,
    Long groupId,
    String groupName,
    String topicCode,
    String topicTitle,
    String filename,
    String contentType,
    String stage,
    String note,
    long size,
    String checksum,
    LocalDateTime submittedAt,
    boolean late,
    String submittedBy) {}
