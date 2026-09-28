package topicmanagement.dto.response;

public record AdvisorQuotaResponse(
    Long id,
    Long periodId,
    String periodName,
    Long lecturerId,
    String lecturerName,
    Integer maxGroups,
    long currentGroups) {}
