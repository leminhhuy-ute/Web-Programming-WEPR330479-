package vn.edu.hcmute.topicmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import vn.edu.hcmute.topicmanagement.enums.RegistrationPeriodType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record RegistrationPeriodRequest(
        @NotBlank(message = "Tên đợt đăng ký không được để trống")
        String name,

        @NotNull(message = "Loại đợt đăng ký không được để trống")
        RegistrationPeriodType type,

        @NotNull(message = "Thời gian GV bắt đầu không được để trống")
        LocalDateTime lecturerStartAt,

        @NotNull(message = "Thời gian GV kết thúc không được để trống")
        LocalDateTime lecturerEndAt,

        @NotNull(message = "Thời gian SV bắt đầu không được để trống")
        LocalDateTime studentStartAt,

        @NotNull(message = "Thời gian SV kết thúc không được để trống")
        LocalDateTime studentEndAt,

        LocalDateTime reviewDeadline,

        LocalDate defenseDate
) {}
