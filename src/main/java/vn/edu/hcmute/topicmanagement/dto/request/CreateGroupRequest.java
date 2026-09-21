package vn.edu.hcmute.topicmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateGroupRequest(
        @NotBlank(message = "Tên nhóm không được để trống")
        @Size(max = 80, message = "Tên nhóm tối đa 80 ký tự")
        String groupName,

        String notes
) {}
