package topicmanagement.student;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import topicmanagement.entity.User;

@Entity
@Table(name = "student_report_edits")
public class ReportEditHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    public Report report;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_id", nullable = false)
    public User changedBy;

    @Column(name = "edited_at", nullable = false)
    public LocalDateTime editedAt = LocalDateTime.now();

    @Column(nullable = false, length = 180)
    public String filename;

    @Column(name = "content_type", nullable = false)
    public String contentType;

    @Column(nullable = false, length = 1000)
    public String note;

    @Column(name = "report_size", nullable = false)
    public Long reportSize;

    @Column(length = 64)
    public String checksum;

    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false, length = 10485760)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.LONGVARBINARY)
    public byte[] content;
}
