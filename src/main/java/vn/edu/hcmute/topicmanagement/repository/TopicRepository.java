package vn.edu.hcmute.topicmanagement.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.hcmute.topicmanagement.domain.Topic;
import vn.edu.hcmute.topicmanagement.domain.TopicStatus;
import java.util.List;
import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    @EntityGraph(attributePaths = {"advisor", "reviewer", "council"})
    List<Topic> findAllByOrderByCodeAsc();
    @EntityGraph(attributePaths = {"advisor", "reviewer", "council"})
    Optional<Topic> findDetailById(Long id);
    long countByStatus(TopicStatus status);
    long countByCouncilId(Long councilId);
}
