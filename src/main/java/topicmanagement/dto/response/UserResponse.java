package topicmanagement.dto.response;

import java.time.LocalDateTime;
import topicmanagement.entity.User;

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
    LocalDateTime updatedAt) {
    public UserResponse(User u) {
        this(u.getId(), u.getUserCode(), u.getUsername(), u.getFullName(), u.getEmail(),
                u.getRole().name(), u.getDepartment() == null ? null : u.getDepartment().getId(),
                u.getDepartment() == null ? null : u.getDepartment().getName(),
                u.getStatus().name(), u.getCreatedAt(), u.getUpdatedAt());
    }
}
