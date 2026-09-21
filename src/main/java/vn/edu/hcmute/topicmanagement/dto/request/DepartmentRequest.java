package vn.edu.hcmute.topicmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DepartmentRequest(
        @NotBlank(message = "Mã bộ môn không được để trống")
        String code,

        @NotBlank(message = "Tên bộ môn không được để trống")
        String name,

        String description
) {}
