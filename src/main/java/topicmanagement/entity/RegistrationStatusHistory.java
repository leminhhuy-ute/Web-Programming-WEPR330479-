package topicmanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import topicmanagement.enums.RegistrationStatus;

@Entity
@Table(name = "registration_status_history", indexes =
    @Index(name = "idx_registration_history", columnList = "registration_id,changed_at"))
public class RegistrationStatusHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registration_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private TopicRegistration registration;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 20)
    private RegistrationStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 20)
    private RegistrationStatus newStatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by", nullable = false)
    private User changedBy;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt = LocalDateTime.now();

    @Column(length = 2000)
    private String note;

    public Long getId() { return id; }
    public TopicRegistration getRegistration() { return registration; }
    public void setRegistration(TopicRegistration registration) { this.registration = registration; }
    public RegistrationStatus getOldStatus() { return oldStatus; }
    public void setOldStatus(RegistrationStatus oldStatus) { this.oldStatus = oldStatus; }
    public RegistrationStatus getNewStatus() { return newStatus; }
    public void setNewStatus(RegistrationStatus newStatus) { this.newStatus = newStatus; }
    public User getChangedBy() { return changedBy; }
    public void setChangedBy(User changedBy) { this.changedBy = changedBy; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
