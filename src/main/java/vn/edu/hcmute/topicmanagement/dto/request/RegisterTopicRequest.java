package vn.edu.hcmute.topicmanagement.dto.request;

import jakarta.validation.constraints.NotNull;

public record RegisterTopicRequest(
        @NotNull(message = "Mã đề tài không được để trống")
        Long topicId,

        String proposalNote
) {}
