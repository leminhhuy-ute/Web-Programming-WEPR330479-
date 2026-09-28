package topicmanagement.dto.request;

import jakarta.validation.constraints.*;

public record AdvisorQuotaRequest(
    @NotNull Long periodId,
    @NotNull Long lecturerId,
    @NotNull @Min(1) @Max(100) Integer maxGroups) {}
