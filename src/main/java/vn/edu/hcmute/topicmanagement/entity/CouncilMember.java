package vn.edu.hcmute.topicmanagement.entity;

import jakarta.persistence.*;
import vn.edu.hcmute.topicmanagement.enums.CouncilRole;

@Entity
@Table(name = "council_members", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"council_id", "lecturer_id"})
})
public class CouncilMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "council_id", nullable = false)
    private Council council;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lecturer_id", nullable = false)
    private User lecturer;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_role", nullable = false, length = 30)
    private CouncilRole memberRole;

    @Column(name = "joined_at", nullable = false)
    private java.time.LocalDateTime joinedAt = java.time.LocalDateTime.now();

    public CouncilMember() {}

    public CouncilMember(Council council, User lecturer, CouncilRole memberRole) {
        this.council = council;
        this.lecturer = lecturer;
        this.memberRole = memberRole;
        this.joinedAt = java.time.LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Council getCouncil() { return council; }
    public void setCouncil(Council council) { this.council = council; }
    public User getLecturer() { return lecturer; }
    public void setLecturer(User lecturer) { this.lecturer = lecturer; }
    public CouncilRole getMemberRole() { return memberRole; }
    public void setMemberRole(CouncilRole memberRole) { this.memberRole = memberRole; }
    public java.time.LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(java.time.LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
}
