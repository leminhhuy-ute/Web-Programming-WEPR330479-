package topicmanagement.student;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import topicmanagement.entity.*;
@Entity @Table(name="student_reports")
public class Report {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional=false) public StudentGroup group;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="registration_id") public TopicRegistration registration;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="topic_id") public Topic topic;
    @ManyToOne(optional=false) public User submittedBy;
    @Column(nullable=false,length=180) public String filename;
    @Column(nullable=false) public String contentType;
    @Column(nullable=false) public String stage;
    @Column(nullable=false,length=1000) public String note;
    @Column(nullable=false) public LocalDateTime submittedAt=LocalDateTime.now();
    public boolean late;
    @Column(name="report_size") public Long reportSize;
    @Column(length=64) public String checksum;
    @Column(name="storage_path",length=500) public String storagePath;
    @Basic(fetch=FetchType.LAZY) @Column(nullable=false,length=10485760)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.LONGVARBINARY) public byte[] content;
}
