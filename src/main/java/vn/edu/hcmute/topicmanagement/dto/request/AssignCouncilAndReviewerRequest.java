package vn.edu.hcmute.topicmanagement.dto.request;

import jakarta.validation.constraints.NotNull;

public record AssignCouncilAndReviewerRequest(
        @NotNull(message = "Đề tài không được để trống")
        Long topicId,

        @NotNull(message = "Hội đồng không được để trống")
        Long councilId,

        @NotNull(message = "Giảng viên phản biện không được để trống")
        Long reviewerId
) {}
