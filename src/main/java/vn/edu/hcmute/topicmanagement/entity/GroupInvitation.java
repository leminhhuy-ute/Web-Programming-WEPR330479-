package vn.edu.hcmute.topicmanagement.entity;

import jakarta.persistence.*;
import vn.edu.hcmute.topicmanagement.enums.InvitationStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_invitations")
public class GroupInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id", nullable = false)
    private StudentGroup group;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "inviter_id", nullable = false)
    private User inviter;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "invitee_id", nullable = false)
    private User invitee;

    @Column(length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvitationStatus status = InvitationStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public GroupInvitation() {}

    public GroupInvitation(StudentGroup group, User inviter, User invitee, String message) {
        this.group = group;
        this.inviter = inviter;
        this.invitee = invitee;
        this.message = message;
        this.status = InvitationStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public StudentGroup getGroup() { return group; }
    public void setGroup(StudentGroup group) { this.group = group; }
    public User getInviter() { return inviter; }
    public void setInviter(User inviter) { this.inviter = inviter; }
    public User getInvitee() { return invitee; }
    public void setInvitee(User invitee) { this.invitee = invitee; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public InvitationStatus getStatus() { return status; }
    public void setStatus(InvitationStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
