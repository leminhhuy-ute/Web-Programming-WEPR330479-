package topicmanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_members")
public class GroupMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private StudentGroup group;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private User student;

    @Column(name = "member_role", nullable = false, length = 20)
    private String memberRole = "MEMBER";

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();

    public GroupMember() {}

    public GroupMember(StudentGroup group, User student, String memberRole) {
        this.group = group;
        this.student = student;
        this.memberRole = memberRole;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public StudentGroup getGroup() { return group; }
    public void setGroup(StudentGroup group) { this.group = group; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public String getMemberRole() { return memberRole; }
    public void setMemberRole(String memberRole) { this.memberRole = memberRole; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
}
