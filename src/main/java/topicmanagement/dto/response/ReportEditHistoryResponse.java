package topicmanagement.dto.response;

import java.time.LocalDateTime;

public record ReportEditHistoryResponse(
    Long id,
    Long reportId,
    String filename,
    String contentType,
    String note,
    long size,
    String checksum,
    LocalDateTime editedAt,
    String editedBy) {}
