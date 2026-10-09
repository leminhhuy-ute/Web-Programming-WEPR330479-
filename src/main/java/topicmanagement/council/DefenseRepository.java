package topicmanagement.council;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
public interface DefenseRepository extends JpaRepository<Defense,Long> {
    @EntityGraph(attributePaths = {"group", "group.topic", "group.topic.period", "group.topic.advisor1",
        "group.topic.advisor2", "reviewer"})
    List<Defense> findByCouncilId(Long councilId);
    Optional<Defense> findByGroupId(Long groupId);
    Optional<Defense> findByRegistrationId(Long registrationId);
    boolean existsByRegistrationId(Long registrationId);
    boolean existsByCouncilId(Long councilId);
    boolean existsByGroupTopicId(Long topicId);
    long countByPublishedTrue();
    @Query("select d.group.id from Defense d")
    List<Long> findAssignedGroupIds();
    @EntityGraph(attributePaths = {"group", "group.topic", "group.topic.advisor1", "group.topic.advisor2",
        "council", "council.members", "council.members.lecturer", "reviewer"})
    List<Defense> findAllByOrderByIdDesc();
}
