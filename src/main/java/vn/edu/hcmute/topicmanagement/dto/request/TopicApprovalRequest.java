package vn.edu.hcmute.topicmanagement.dto.request;

import jakarta.validation.constraints.NotNull;
import vn.edu.hcmute.topicmanagement.enums.TopicStatus;

public record TopicApprovalRequest(
        @NotNull(message = "Trạng thái phê duyệt không được để trống")
        TopicStatus status,

        String rejectionReason
) {}
