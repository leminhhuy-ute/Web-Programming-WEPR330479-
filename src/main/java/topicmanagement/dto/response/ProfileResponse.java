package topicmanagement.dto.response;

public record ProfileResponse(
    String username,
    String userCode,
    String fullName,
    String email,
    String role,
    String departmentName,
    String studentClass) {}
