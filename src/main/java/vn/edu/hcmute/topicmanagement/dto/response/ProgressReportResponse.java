package vn.edu.hcmute.topicmanagement.dto.response;

import vn.edu.hcmute.topicmanagement.entity.ProgressReport;

import java.time.LocalDateTime;

public record ProgressReportResponse(
        Long id,
        Long groupId,
        Long submittedById,
        String submittedByName,
        String reportTitle,
        String stage,
        String contentSummary,
        String attachmentUrl,
        String fileName,
        int completionPercentage,
        Double supervisorScore,
        String supervisorFeedback,
        LocalDateTime submittedAt,
        LocalDateTime reviewedAt
) {
    public static ProgressReportResponse fromEntity(ProgressReport r) {
        return new ProgressReportResponse(
                r.getId(),
                r.getGroup().getId(),
                r.getSubmittedBy() != null ? r.getSubmittedBy().getId() : null,
                r.getSubmittedBy() != null ? r.getSubmittedBy().getFullName() : null,
                r.getReportTitle(),
                r.getStage(),
                r.getContentSummary(),
                r.getAttachmentUrl(),
                r.getFileName(),
                r.getCompletionPercentage(),
                r.getSupervisorScore(),
                r.getSupervisorFeedback(),
                r.getSubmittedAt(),
                r.getReviewedAt()
        );
    }
}
