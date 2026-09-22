package topicmanagement.council;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface DefenseRepository extends JpaRepository<Defense,Long> {
    Optional<Defense> findByGroupId(Long groupId);
    boolean existsByGroupTopicId(Long topicId);
}
