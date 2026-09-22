package topicmanagement.student;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import topicmanagement.entity.*;
@Entity @Table(name="student_reports")
public class Report {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional=false) public StudentGroup group;
    @ManyToOne(optional=false) public User submittedBy;
    @Column(nullable=false,length=180) public String filename;
    @Column(nullable=false) public String contentType;
    @Column(nullable=false) public String stage;
    @Column(nullable=false,length=1000) public String note;
    @Column(nullable=false) public LocalDateTime submittedAt=LocalDateTime.now();
    public boolean late;
    @Lob @Basic(fetch=FetchType.LAZY) @Column(nullable=false) public byte[] content;
}
