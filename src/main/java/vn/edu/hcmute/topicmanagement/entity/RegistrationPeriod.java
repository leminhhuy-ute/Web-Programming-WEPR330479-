package vn.edu.hcmute.topicmanagement.entity;

import jakarta.persistence.*;
import vn.edu.hcmute.topicmanagement.enums.RegistrationPeriodType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "registration_periods")
public class RegistrationPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RegistrationPeriodType type;

    @Column(name = "lecturer_start_at", nullable = false)
    private LocalDateTime lecturerStartAt;

    @Column(name = "lecturer_end_at", nullable = false)
    private LocalDateTime lecturerEndAt;

    @Column(name = "student_start_at", nullable = false)
    private LocalDateTime studentStartAt;

    @Column(name = "student_end_at", nullable = false)
    private LocalDateTime studentEndAt;

    @Column(name = "review_deadline")
    private LocalDateTime reviewDeadline;

    @Column(name = "defense_date")
    private LocalDate defenseDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public RegistrationPeriod() {}

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public RegistrationPeriodType getType() { return type; }
    public void setType(RegistrationPeriodType type) { this.type = type; }
    public LocalDateTime getLecturerStartAt() { return lecturerStartAt; }
    public void setLecturerStartAt(LocalDateTime lecturerStartAt) { this.lecturerStartAt = lecturerStartAt; }
    public LocalDateTime getLecturerEndAt() { return lecturerEndAt; }
    public void setLecturerEndAt(LocalDateTime lecturerEndAt) { this.lecturerEndAt = lecturerEndAt; }
    public LocalDateTime getStudentStartAt() { return studentStartAt; }
    public void setStudentStartAt(LocalDateTime studentStartAt) { this.studentStartAt = studentStartAt; }
    public LocalDateTime getStudentEndAt() { return studentEndAt; }
    public void setStudentEndAt(LocalDateTime studentEndAt) { this.studentEndAt = studentEndAt; }
    public LocalDateTime getReviewDeadline() { return reviewDeadline; }
    public void setReviewDeadline(LocalDateTime reviewDeadline) { this.reviewDeadline = reviewDeadline; }
    public LocalDate getDefenseDate() { return defenseDate; }
    public void setDefenseDate(LocalDate defenseDate) { this.defenseDate = defenseDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
