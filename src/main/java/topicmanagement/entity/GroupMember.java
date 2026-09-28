package topicmanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_members", uniqueConstraints = {
    @UniqueConstraint(name = "uk_group_member_period_student", columnNames = {"registration_period_id", "student_id"})
})
public class GroupMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private StudentGroup group;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_period_id")
    private RegistrationPeriod registrationPeriod;

    @Column(name = "member_role", nullable = false, length = 20)
    private String memberRole = "MEMBER";

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();

    public GroupMember() {}

    public GroupMember(StudentGroup group, User student, String memberRole) {
        this.group = group;
        this.student = student;
        this.memberRole = memberRole;
        if (group != null) {
            this.registrationPeriod = group.getPeriod();
        }
    }

    public GroupMember(StudentGroup group, User student, String memberRole, RegistrationPeriod registrationPeriod) {
        this.group = group;
        this.student = student;
        this.memberRole = memberRole;
        this.registrationPeriod = registrationPeriod;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public StudentGroup getGroup() { return group; }
    public void setGroup(StudentGroup group) {
        this.group = group;
        if (group != null && this.registrationPeriod == null) {
            this.registrationPeriod = group.getPeriod();
        }
    }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public RegistrationPeriod getRegistrationPeriod() { return registrationPeriod; }
    public void setRegistrationPeriod(RegistrationPeriod registrationPeriod) { this.registrationPeriod = registrationPeriod; }

    public String getMemberRole() { return memberRole; }
    public void setMemberRole(String memberRole) { this.memberRole = memberRole; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
}
