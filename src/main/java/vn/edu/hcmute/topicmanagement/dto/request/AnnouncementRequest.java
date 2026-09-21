package vn.edu.hcmute.topicmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import vn.edu.hcmute.topicmanagement.enums.AnnouncementAudience;

public record AnnouncementRequest(
        @NotBlank(message = "Tiêu đề thông báo không được để trống")
        String title,

        @NotBlank(message = "Nội dung thông báo không được để trống")
        String content,

        @NotNull(message = "Đối tượng nhận thông báo không được để trống")
        AnnouncementAudience audience
) {}
