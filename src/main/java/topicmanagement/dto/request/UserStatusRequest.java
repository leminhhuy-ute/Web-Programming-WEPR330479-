package topicmanagement.dto.request;

import jakarta.validation.constraints.NotNull;
import topicmanagement.enums.UserStatus;

public record UserStatusRequest(@NotNull UserStatus status) {}
