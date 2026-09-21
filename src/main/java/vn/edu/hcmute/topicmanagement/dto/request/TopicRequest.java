package vn.edu.hcmute.topicmanagement.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import vn.edu.hcmute.topicmanagement.enums.RegistrationPeriodType;

public record TopicRequest(
        String topicCode,

        @NotBlank(message = "Tên đề tài không được để trống")
        String title,

        @NotBlank(message = "Mô tả đề tài không được để trống")
        String description,

        String requirements,

        @Min(value = 1, message = "Số lượng sinh viên tối thiểu là 1")
        int maxStudents,

        @NotNull(message = "Loại đề tài không được để trống")
        RegistrationPeriodType topicType,

        @NotNull(message = "Bộ môn không được để trống")
        Long departmentId,

        @NotNull(message = "Đợt đăng ký không được để trống")
        Long periodId
) {}
