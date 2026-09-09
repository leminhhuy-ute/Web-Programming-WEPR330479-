package vn.edu.hcmute.topicmanagement.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Entity
@Table(name = "topics", uniqueConstraints = @UniqueConstraint(name = "uk_topic_code", columnNames = "code"))
public class Topic {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank @Column(nullable = false, length = 30)
    private String code;
    @NotBlank @Column(nullable = false, length = 255)
    private String title;
    @NotBlank @Column(nullable = false, length = 100)
    private String studentGroup;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "advisor_id", nullable = false)
    private Lecturer advisor;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "council_id")
    private Council council;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "reviewer_id")
    private Lecturer reviewer;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private TopicStatus status = TopicStatus.WAITING_ASSIGNMENT;
    @Column(precision = 4, scale = 2)
    private BigDecimal finalScore;

    public Topic() {}
    public Topic(String code, String title, String studentGroup, Lecturer advisor) {
        this.code = code; this.title = title; this.studentGroup = studentGroup; this.advisor = advisor;
    }
    public Long getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getStudentGroup() { return studentGroup; }
    public void setStudentGroup(String studentGroup) { this.studentGroup = studentGroup; }
    public Lecturer getAdvisor() { return advisor; }
    public void setAdvisor(Lecturer advisor) { this.advisor = advisor; }
    public Council getCouncil() { return council; }
    public void setCouncil(Council council) { this.council = council; }
    public Lecturer getReviewer() { return reviewer; }
    public void setReviewer(Lecturer reviewer) { this.reviewer = reviewer; }
    public TopicStatus getStatus() { return status; }
    public void setStatus(TopicStatus status) { this.status = status; }
    public BigDecimal getFinalScore() { return finalScore; }
    public void setFinalScore(BigDecimal finalScore) { this.finalScore = finalScore; }
}
