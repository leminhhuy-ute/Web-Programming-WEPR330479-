package vn.edu.hcmute.topicmanagement.dto.request;

import jakarta.validation.constraints.NotNull;
import vn.edu.hcmute.topicmanagement.enums.CouncilRole;

public record CouncilMemberRequest(
        @NotNull(message = "Giảng viên không được để trống")
        Long lecturerId,

        @NotNull(message = "Vai trò trong hội đồng không được để trống")
        CouncilRole role
) {}
