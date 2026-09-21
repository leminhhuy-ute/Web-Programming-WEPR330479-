package topicmanagement.entity;

import jakarta.persistence.*;
import java.time.*;
import topicmanagement.enums.RegistrationPeriodType;

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

  @Column(name = "created_at", insertable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", insertable = false, updatable = false)
  private LocalDateTime updatedAt;

  public Long getId() {
    return id;
  }

  public void setId(Long v) {
    id = v;
  }

  public String getName() {
    return name;
  }

  public void setName(String v) {
    name = v;
  }

  public RegistrationPeriodType getType() {
    return type;
  }

  public void setType(RegistrationPeriodType v) {
    type = v;
  }

  public LocalDateTime getLecturerStartAt() {
    return lecturerStartAt;
  }

  public void setLecturerStartAt(LocalDateTime v) {
    lecturerStartAt = v;
  }

  public LocalDateTime getLecturerEndAt() {
    return lecturerEndAt;
  }

  public void setLecturerEndAt(LocalDateTime v) {
    lecturerEndAt = v;
  }

  public LocalDateTime getStudentStartAt() {
    return studentStartAt;
  }

  public void setStudentStartAt(LocalDateTime v) {
    studentStartAt = v;
  }

  public LocalDateTime getStudentEndAt() {
    return studentEndAt;
  }

  public void setStudentEndAt(LocalDateTime v) {
    studentEndAt = v;
  }

  public LocalDateTime getReviewDeadline() {
    return reviewDeadline;
  }

  public void setReviewDeadline(LocalDateTime v) {
    reviewDeadline = v;
  }

  public LocalDate getDefenseDate() {
    return defenseDate;
  }

  public void setDefenseDate(LocalDate v) {
    defenseDate = v;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
