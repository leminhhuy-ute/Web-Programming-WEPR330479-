package topicmanagement.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import topicmanagement.enums.Role;
import topicmanagement.enums.UserStatus;

public record UserRequest(
    @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{1,50}") String userCode,
    @NotBlank @Pattern(regexp = "[A-Za-z0-9_.-]{1,100}") String username,
    @NotBlank @Size(max = 255) String fullName,
    @NotBlank @Email @Size(max = 255) String email,
    @NotNull Role role,
    Long departmentId,
    @NotNull UserStatus status,
    @Size(min = 8, max = 72) String password,
    @Size(max = 50) String studentClass) {
    public UserRequest(String userCode, String username, String fullName, String email,
            Role role, Long departmentId, UserStatus status, String password) {
        this(userCode, username, fullName, email, role, departmentId, status, password, null);
    }
}
