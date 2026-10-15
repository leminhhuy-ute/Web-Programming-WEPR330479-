package topicmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import topicmanagement.enums.AnnouncementAudience;

public record AnnouncementRequest(
    @NotBlank(message = "Tiêu đề thông báo không được để trống") @Size(max = 255, message = "Tiêu đề thông báo tối đa 255 ký tự") String title,
    @NotBlank(message = "Nội dung thông báo không được để trống") @Size(max = 10_000, message = "Nội dung thông báo tối đa 10.000 ký tự") String content,
    @NotNull(message = "Vui lòng chọn đối tượng nhận thông báo") AnnouncementAudience audience) {}
