package vn.edu.hcmute.topicmanagement.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import vn.edu.hcmute.topicmanagement.enums.EvaluationType;

public record GradeEvaluationRequest(
        @NotNull(message = "Đề tài không được để trống")
        Long topicId,

        Long councilId,

        @NotNull(message = "Điểm đánh giá không được để trống")
        @DecimalMin(value = "0.0", message = "Điểm phải từ 0.0 đến 10.0")
        @DecimalMax(value = "10.0", message = "Điểm phải từ 0.0 đến 10.0")
        Double score,

        EvaluationType evaluationType,

        @NotBlank(message = "Nhận xét đánh giá không được để trống")
        String feedback
) {}
