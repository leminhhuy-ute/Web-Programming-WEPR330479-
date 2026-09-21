package vn.edu.hcmute.topicmanagement.dto.request;

import jakarta.validation.constraints.NotNull;

public record TransferLeaderRequest(
        @NotNull(message = "ID thành viên nhóm trưởng mới không được để trống")
        Long newLeaderId
) {}
