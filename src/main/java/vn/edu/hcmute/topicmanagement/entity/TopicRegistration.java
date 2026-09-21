package vn.edu.hcmute.topicmanagement.entity;

import jakarta.persistence.*;
import vn.edu.hcmute.topicmanagement.enums.GroupStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "topic_registrations")
public class TopicRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id", nullable = false)
    private StudentGroup group;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupStatus status = GroupStatus.PENDING;

    @Column(name = "proposal_note", columnDefinition = "TEXT")
    private String proposalNote;

    @Column(name = "supervisor_feedback", columnDefinition = "TEXT")
    private String supervisorFeedback;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt = LocalDateTime.now();

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    public TopicRegistration() {}

    public TopicRegistration(StudentGroup group, Topic topic, String proposalNote) {
        this.group = group;
        this.topic = topic;
        this.proposalNote = proposalNote;
        this.status = GroupStatus.PENDING;
        this.registeredAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public StudentGroup getGroup() { return group; }
    public void setGroup(StudentGroup group) { this.group = group; }
    public Topic getTopic() { return topic; }
    public void setTopic(Topic topic) { this.topic = topic; }
    public GroupStatus getStatus() { return status; }
    public void setStatus(GroupStatus status) { this.status = status; }
    public String getProposalNote() { return proposalNote; }
    public void setProposalNote(String proposalNote) { this.proposalNote = proposalNote; }
    public String getSupervisorFeedback() { return supervisorFeedback; }
    public void setSupervisorFeedback(String supervisorFeedback) { this.supervisorFeedback = supervisorFeedback; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}
