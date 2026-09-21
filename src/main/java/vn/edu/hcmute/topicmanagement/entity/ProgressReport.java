package vn.edu.hcmute.topicmanagement.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "progress_reports")
public class ProgressReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id", nullable = false)
    private StudentGroup group;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "submitted_by", nullable = false)
    private User submittedBy;

    @Column(name = "report_title", nullable = false)
    private String reportTitle;

    @Column(nullable = false, length = 50)
    private String stage; // Đề cương, Giữa kỳ, Báo cáo cuối kỳ

    @Column(name = "content_summary", columnDefinition = "TEXT")
    private String contentSummary;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "completion_percentage", nullable = false)
    private int completionPercentage = 0;

    @Column(name = "supervisor_score")
    private Double supervisorScore;

    @Column(name = "supervisor_feedback", columnDefinition = "TEXT")
    private String supervisorFeedback;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    public ProgressReport() {}

    public ProgressReport(StudentGroup group, User submittedBy, String reportTitle, String stage, String contentSummary, String attachmentUrl, String fileName, int completionPercentage) {
        this.group = group;
        this.submittedBy = submittedBy;
        this.reportTitle = reportTitle;
        this.stage = stage;
        this.contentSummary = contentSummary;
        this.attachmentUrl = attachmentUrl;
        this.fileName = fileName;
        this.completionPercentage = completionPercentage;
        this.submittedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public StudentGroup getGroup() { return group; }
    public void setGroup(StudentGroup group) { this.group = group; }
    public User getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(User submittedBy) { this.submittedBy = submittedBy; }
    public String getReportTitle() { return reportTitle; }
    public void setReportTitle(String reportTitle) { this.reportTitle = reportTitle; }
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public String getContentSummary() { return contentSummary; }
    public void setContentSummary(String contentSummary) { this.contentSummary = contentSummary; }
    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public int getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(int completionPercentage) { this.completionPercentage = completionPercentage; }
    public Double getSupervisorScore() { return supervisorScore; }
    public void setSupervisorScore(Double supervisorScore) { this.supervisorScore = supervisorScore; }
    public String getSupervisorFeedback() { return supervisorFeedback; }
    public void setSupervisorFeedback(String supervisorFeedback) { this.supervisorFeedback = supervisorFeedback; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}
