package vn.edu.hcmute.topicmanagement.dto.response;

import vn.edu.hcmute.topicmanagement.entity.Topic;

import java.time.LocalDateTime;

public record TopicResponse(
        Long id,
        String topicCode,
        String title,
        String description,
        String requirements,
        int maxStudents,
        String topicType,
        String topicTypeLabel,
        String status,
        String rejectionReason,
        Long departmentId,
        String departmentName,
        Long periodId,
        String periodName,
        Long createdById,
        String createdByName,
        Long advisor1Id,
        String advisor1Name,
        Long advisor2Id,
        String advisor2Name,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TopicResponse fromEntity(Topic t) {
        String typeLabel = switch (t.getTopicType()) {
            case KLTN -> "Khóa luận tốt nghiệp";
            case TLCN -> "Tiểu luận chuyên ngành";
            case COURSE -> "Đồ án môn học";
            case NCKH -> "Nghiên cứu khoa học";
        };

        return new TopicResponse(
                t.getId(),
                t.getTopicCode(),
                t.getTitle(),
                t.getDescription(),
                t.getRequirements(),
                t.getMaxStudents(),
                t.getTopicType().name(),
                typeLabel,
                t.getStatus().name(),
                t.getRejectionReason(),
                t.getDepartment() != null ? t.getDepartment().getId() : null,
                t.getDepartment() != null ? t.getDepartment().getName() : null,
                t.getPeriod() != null ? t.getPeriod().getId() : null,
                t.getPeriod() != null ? t.getPeriod().getName() : null,
                t.getCreatedBy() != null ? t.getCreatedBy().getId() : null,
                t.getCreatedBy() != null ? t.getCreatedBy().getFullName() : null,
                t.getAdvisor1() != null ? t.getAdvisor1().getId() : null,
                t.getAdvisor1() != null ? t.getAdvisor1().getFullName() : null,
                t.getAdvisor2() != null ? t.getAdvisor2().getId() : null,
                t.getAdvisor2() != null ? t.getAdvisor2().getFullName() : null,
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }
}
