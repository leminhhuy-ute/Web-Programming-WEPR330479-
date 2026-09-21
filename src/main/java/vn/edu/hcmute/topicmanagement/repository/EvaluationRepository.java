package vn.edu.hcmute.topicmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.hcmute.topicmanagement.entity.Council;
import vn.edu.hcmute.topicmanagement.entity.Evaluation;
import vn.edu.hcmute.topicmanagement.entity.Topic;
import vn.edu.hcmute.topicmanagement.entity.User;
import vn.edu.hcmute.topicmanagement.enums.EvaluationType;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    List<Evaluation> findByTopic(Topic topic);
    List<Evaluation> findByTopicAndEvaluationType(Topic topic, EvaluationType type);
    List<Evaluation> findByCouncil(Council council);
    List<Evaluation> findByEvaluator(User evaluator);
    Optional<Evaluation> findByTopicAndEvaluatorAndEvaluationType(Topic topic, User evaluator, EvaluationType type);
}
