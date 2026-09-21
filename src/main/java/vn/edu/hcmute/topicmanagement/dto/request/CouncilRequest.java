package vn.edu.hcmute.topicmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CouncilRequest(
        String code,

        @NotBlank(message = "Tên hội đồng không được để trống")
        String name,

        @NotNull(message = "Bộ môn không được để trống")
        Long departmentId,

        Long periodId,

        LocalDate defenseDate,

        String defenseTime,

        String room
) {}
