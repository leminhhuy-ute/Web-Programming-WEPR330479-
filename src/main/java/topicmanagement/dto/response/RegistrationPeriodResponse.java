package topicmanagement.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import topicmanagement.entity.RegistrationPeriod;

public class RegistrationPeriodResponse {
    private Long id;
    private String name;
    private String type;
    private LocalDateTime lecturerStartAt;
    private LocalDateTime lecturerEndAt;
    private LocalDateTime studentStartAt;
    private LocalDateTime studentEndAt;
    private LocalDateTime reviewDeadline;
    private LocalDate defenseDate;

    public RegistrationPeriodResponse() {}

    public RegistrationPeriodResponse(RegistrationPeriod p) {
        if (p != null) {
            this.id = p.getId();
            this.name = p.getName();
            this.type = p.getType().name();
            this.lecturerStartAt = p.getLecturerStartAt();
            this.lecturerEndAt = p.getLecturerEndAt();
            this.studentStartAt = p.getStudentStartAt();
            this.studentEndAt = p.getStudentEndAt();
            this.reviewDeadline = p.getReviewDeadline();
            this.defenseDate = p.getDefenseDate();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

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
}
