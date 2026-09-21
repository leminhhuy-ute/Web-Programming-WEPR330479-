package vn.edu.hcmute.topicmanagement.dto.response;

import vn.edu.hcmute.topicmanagement.entity.Evaluation;

import java.time.LocalDateTime;

public record EvaluationResponse(
        Long id,
        Long councilId,
        String councilName,
        Long topicId,
        String topicCode,
        String topicTitle,
        Long evaluatorId,
        String evaluatorName,
        String evaluationType,
        Double score,
        String feedback,
        LocalDateTime evaluatedAt
) {
    public static EvaluationResponse fromEntity(Evaluation e) {
        return new EvaluationResponse(
                e.getId(),
                e.getCouncil() != null ? e.getCouncil().getId() : null,
                e.getCouncil() != null ? e.getCouncil().getName() : null,
                e.getTopic().getId(),
                e.getTopic().getTopicCode(),
                e.getTopic().getTitle(),
                e.getEvaluator().getId(),
                e.getEvaluator().getFullName(),
                e.getEvaluationType().name(),
                e.getScore(),
                e.getFeedback(),
                e.getEvaluatedAt()
        );
    }
}
