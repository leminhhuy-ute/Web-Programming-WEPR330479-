package vn.edu.hcmute.topicmanagement.dto.response;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String userCode,
        String username,
        String fullName,
        String email,
        String role,
        Long departmentId,
        String departmentName,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
