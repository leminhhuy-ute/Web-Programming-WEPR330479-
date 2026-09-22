package topicmanagement.student;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import topicmanagement.entity.*;
@Entity @Table(name="group_invitations",uniqueConstraints=@UniqueConstraint(columnNames={"group_id","student_id"}))
public class Invitation {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional=false) @JoinColumn(name="group_id") public StudentGroup group;
    @ManyToOne(optional=false) @JoinColumn(name="student_id") public User student;
    @Column(nullable=false) public String status="PENDING";
    @Column(nullable=false) public LocalDateTime createdAt=LocalDateTime.now();
}
