package vn.edu.hcmute.topicmanagement.dto.request;

import jakarta.validation.constraints.NotNull;

public record AssignAdvisorsRequest(
        @NotNull(message = "Giảng viên hướng dẫn 1 không được để trống")
        Long advisor1Id,

        Long advisor2Id
) {}
