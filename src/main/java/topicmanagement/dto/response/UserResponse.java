package topicmanagement.dto.response;

import topicmanagement.entity.User;

public class UserResponse {
    private Long id;
    private String userCode;
    private String username;
    private String fullName;
    private String email;
    private String role;
    private String status;
    private DepartmentResponse department;

    public UserResponse() {}

    public UserResponse(User user) {
        if (user != null) {
            this.id = user.getId();
            this.userCode = user.getUserCode();
            this.username = user.getUsername();
            this.fullName = user.getFullName();
            this.email = user.getEmail();
            this.role = user.getRole().name();
            this.status = user.getStatus().name();
            if (user.getDepartment() != null) {
                this.department = new DepartmentResponse(user.getDepartment());
            }
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserCode() { return userCode; }
    public void setUserCode(String userCode) { this.userCode = userCode; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public DepartmentResponse getDepartment() { return department; }
    public void setDepartment(DepartmentResponse department) { this.department = department; }
}
