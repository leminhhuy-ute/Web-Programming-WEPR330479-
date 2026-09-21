package topicmanagement.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import topicmanagement.entity.Topic;
import topicmanagement.enums.RegistrationPeriodType;
import topicmanagement.enums.TopicStatus;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    Optional<Topic> findByTopicCode(String topicCode);
    boolean existsByTopicCode(String topicCode);

    List<Topic> findByCreatedById(Long createdById);
    List<Topic> findByDepartmentId(Long departmentId);
    List<Topic> findByDepartmentIdAndStatus(Long departmentId, TopicStatus status);

    @Query("SELECT t FROM Topic t WHERE " +
           "(:departmentId IS NULL OR t.department.id = :departmentId) AND " +
           "(:periodId IS NULL OR t.period.id = :periodId) AND " +
           "(:type IS NULL OR t.topicType = :type) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:createdById IS NULL OR t.createdBy.id = :createdById) AND " +
           "(:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(t.topicCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Topic> filterTopics(
        @Param("departmentId") Long departmentId,
        @Param("periodId") Long periodId,
        @Param("type") RegistrationPeriodType type,
        @Param("status") TopicStatus status,
        @Param("createdById") Long createdById,
        @Param("search") String search
    );
}
