package vn.edu.hcmute.topicmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import vn.edu.hcmute.topicmanagement.enums.Role;
import vn.edu.hcmute.topicmanagement.enums.UserStatus;

public record UserRequest(
        @NotBlank(message = "Mã người dùng không được để trống")
        String userCode,

        @NotBlank(message = "Tên đăng nhập không được để trống")
        String username,

        String password,

        @NotBlank(message = "Họ tên không được để trống")
        String fullName,

        @NotBlank(message = "Email không được để trống")
        String email,

        @NotNull(message = "Vai trò không được để trống")
        Role role,

        Long departmentId,

        UserStatus status
) {}
