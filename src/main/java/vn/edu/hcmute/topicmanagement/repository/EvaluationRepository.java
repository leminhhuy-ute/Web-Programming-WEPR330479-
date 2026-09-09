package vn.edu.hcmute.topicmanagement.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.hcmute.topicmanagement.domain.Evaluation;
import java.util.List;
import java.util.Optional;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    @EntityGraph(attributePaths = {"evaluator"})
    List<Evaluation> findByTopicIdOrderByEvaluatorFullNameAsc(Long topicId);
    Optional<Evaluation> findByTopicIdAndEvaluatorId(Long topicId, Long evaluatorId);
}
