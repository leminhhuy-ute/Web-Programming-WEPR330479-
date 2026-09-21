package vn.edu.hcmute.topicmanagement.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record SubmitReportRequest(
        @NotBlank(message = "Tiêu đề báo cáo không được để trống")
        String reportTitle,

        @NotBlank(message = "Giai đoạn báo cáo không được để trống")
        String stage,

        String contentSummary,

        String attachmentUrl,

        @Min(value = 0, message = "Tiến độ từ 0 đến 100%")
        @Max(value = 100, message = "Tiến độ tối đa 100%")
        int completionPercentage
) {}
