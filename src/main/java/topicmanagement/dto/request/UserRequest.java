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
    @Size(min = 8, max = 128) String password) {}
