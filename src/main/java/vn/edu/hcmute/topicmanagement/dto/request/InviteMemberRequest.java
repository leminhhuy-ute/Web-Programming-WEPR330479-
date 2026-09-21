package vn.edu.hcmute.topicmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;

public record InviteMemberRequest(
        @NotBlank(message = "Mã số sinh viên (MSSV) không được để trống")
        String studentCode,

        String message
) {}
