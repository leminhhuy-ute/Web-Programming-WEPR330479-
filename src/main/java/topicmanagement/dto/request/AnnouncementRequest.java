package topicmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import topicmanagement.enums.AnnouncementAudience;

public record AnnouncementRequest(
    @NotBlank @Size(max = 255) String title,
    @NotBlank @Size(max = 10_000) String content,
    @NotNull AnnouncementAudience audience) {}
