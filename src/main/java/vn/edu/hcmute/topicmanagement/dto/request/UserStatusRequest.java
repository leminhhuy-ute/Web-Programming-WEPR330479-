package vn.edu.hcmute.topicmanagement.dto.request;

import jakarta.validation.constraints.NotNull;
import vn.edu.hcmute.topicmanagement.enums.UserStatus;

public record UserStatusRequest(
        @NotNull(message = "Trạng thái không được để trống")
        UserStatus status
) {}
