package vn.edu.hcmute.topicmanagement.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "evaluations", uniqueConstraints = @UniqueConstraint(name = "uk_topic_evaluator", columnNames = {"topic_id", "evaluator_id"}))
public class Evaluation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "evaluator_id", nullable = false)
    private Lecturer evaluator;
    @DecimalMin("0.0") @DecimalMax("10.0") @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal score;
    @NotBlank @Column(nullable = false, length = 2000)
    private String comment;
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Evaluation() {}
    public Evaluation(Topic topic, Lecturer evaluator, BigDecimal score, String comment) {
        this.topic = topic; this.evaluator = evaluator; this.score = score; this.comment = comment;
    }
    public Long getId() { return id; }
    public Topic getTopic() { return topic; }
    public Lecturer getEvaluator() { return evaluator; }
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; this.updatedAt = LocalDateTime.now(); }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; this.updatedAt = LocalDateTime.now(); }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
