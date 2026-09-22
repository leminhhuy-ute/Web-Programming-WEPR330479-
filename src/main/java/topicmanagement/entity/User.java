package topicmanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import topicmanagement.enums.Role;
import topicmanagement.enums.UserStatus;

@Entity
@Table(name = "users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_code", nullable = false, unique = true, length = 50)
  private String userCode;

  @Column(nullable = false, unique = true, length = 100)
  private String username;

  @Column(name = "password_hash", nullable = false, length = 512)
  private String passwordHash;

  @Column(name = "full_name", nullable = false)
  private String fullName;

  @Column(nullable = false, unique = true)
  private String email;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private Role role;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "department_id")
  private Department department;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private UserStatus status;

  @org.hibernate.annotations.CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @org.hibernate.annotations.UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public Long getId() {
    return id;
  }

  public void setId(Long v) {
    id = v;
  }

  public String getUserCode() {
    return userCode;
  }

  public void setUserCode(String v) {
    userCode = v;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String v) {
    username = v;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String v) {
    passwordHash = v;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String v) {
    fullName = v;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String v) {
    email = v;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role v) {
    role = v;
  }

  public Department getDepartment() {
    return department;
  }

  public void setDepartment(Department v) {
    department = v;
  }

  public UserStatus getStatus() {
    return status;
  }

  public void setStatus(UserStatus v) {
    status = v;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
