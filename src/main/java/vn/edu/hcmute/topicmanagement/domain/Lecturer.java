package vn.edu.hcmute.topicmanagement.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "lecturers", uniqueConstraints = {
        @UniqueConstraint(name = "uk_lecturer_code", columnNames = "code"),
        @UniqueConstraint(name = "uk_lecturer_email", columnNames = "email")
})
public class Lecturer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank @Column(nullable = false, length = 30)
    private String code;
    @NotBlank @Column(nullable = false, length = 120)
    private String fullName;
    @NotBlank @Email @Column(nullable = false, length = 150)
    private String email;
    @NotNull @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40)
    private Department department;
    @Column(nullable = false)
    private boolean active = true;

    public Lecturer() {}
    public Lecturer(String code, String fullName, String email, Department department) {
        this.code = code; this.fullName = fullName; this.email = email; this.department = department;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
