package topicmanagement.dto.request;

import jakarta.validation.constraints.Size;

public record AdvisorGuidanceRequest(
    @Size(max = 10000) String tasks,
    @Size(max = 10000) String requirements) {}
