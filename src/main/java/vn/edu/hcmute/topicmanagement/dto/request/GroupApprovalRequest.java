package vn.edu.hcmute.topicmanagement.dto.request;

import jakarta.validation.constraints.NotNull;
import vn.edu.hcmute.topicmanagement.enums.GroupStatus;

public record GroupApprovalRequest(
        @NotNull(message = "Trạng thái phê duyệt không được để trống")
        GroupStatus status,

        String supervisorFeedback
) {}
