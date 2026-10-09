package topicmanagement.notification;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import topicmanagement.council.Defense;
import topicmanagement.entity.User;
@Entity
@Table(name="email_notifications", uniqueConstraints=@UniqueConstraint(name="uk_result_email",columnNames={"defense_id","student_id"}),
    indexes=@Index(name="idx_email_due",columnList="status,next_attempt_at"))
public class EmailNotification {
    public enum Status { PENDING, SENT, FAILED }
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Version public Long version;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) public Defense defense;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="student_id") public User student;
    @Column(nullable=false) public String recipient;
    @Column(nullable=false) public String subject;
    @Column(nullable=false,columnDefinition="text") public String body;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) public Status status=Status.PENDING;
    public int attempts;
    @Column(nullable=false) public LocalDateTime createdAt=LocalDateTime.now();
    @Column(nullable=false) public LocalDateTime nextAttemptAt=LocalDateTime.now();
    public LocalDateTime sentAt;
    @Column(length=300) public String lastError;
}
