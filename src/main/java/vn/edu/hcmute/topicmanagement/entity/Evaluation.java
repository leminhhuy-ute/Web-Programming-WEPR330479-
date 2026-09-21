package vn.edu.hcmute.topicmanagement.entity;

import jakarta.persistence.*;
import vn.edu.hcmute.topicmanagement.enums.EvaluationType;

import java.time.LocalDateTime;

@Entity
@Table(name = "evaluations")
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "council_id")
    private Council council;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "evaluator_id", nullable = false)
    private User evaluator;

    @Enumerated(EnumType.STRING)
    @Column(name = "evaluation_type", nullable = false, length = 30)
    private EvaluationType evaluationType;

    @Column(nullable = false)
    private Double score;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "evaluated_at", nullable = false)
    private LocalDateTime evaluatedAt = LocalDateTime.now();

    public Evaluation() {}

    public Evaluation(Council council, Topic topic, User evaluator, EvaluationType evaluationType, Double score, String feedback) {
        this.council = council;
        this.topic = topic;
        this.evaluator = evaluator;
        this.evaluationType = evaluationType;
        this.score = score;
        this.feedback = feedback;
        this.evaluatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Council getCouncil() { return council; }
    public void setCouncil(Council council) { this.council = council; }
    public Topic getTopic() { return topic; }
    public void setTopic(Topic topic) { this.topic = topic; }
    public User getEvaluator() { return evaluator; }
    public void setEvaluator(User evaluator) { this.evaluator = evaluator; }
    public EvaluationType getEvaluationType() { return evaluationType; }
    public void setEvaluationType(EvaluationType evaluationType) { this.evaluationType = evaluationType; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public LocalDateTime getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(LocalDateTime evaluatedAt) { this.evaluatedAt = evaluatedAt; }
}
