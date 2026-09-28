package topicmanagement.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import topicmanagement.entity.Topic;
import topicmanagement.enums.RegistrationPeriodType;
import topicmanagement.enums.TopicStatus;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Topic t where t.id=:id")
    Optional<Topic> lockById(@Param("id") Long id);
    Optional<Topic> findByTopicCode(String topicCode);
    boolean existsByTopicCode(String topicCode);
    boolean existsByPeriodId(Long periodId);
    long countByPeriodId(Long periodId);
    long countByStatus(TopicStatus status);

    List<Topic> findByCreatedById(Long createdById);
    List<Topic> findByDepartmentId(Long departmentId);
    List<Topic> findByDepartmentIdAndStatus(Long departmentId, TopicStatus status);
    List<Topic> findByStatus(TopicStatus status);

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

    @EntityGraph(attributePaths = {"department", "period", "createdBy", "advisor1", "advisor2"})
    @Query("SELECT t FROM Topic t WHERE " +
           "(:departmentId IS NULL OR t.department.id = :departmentId) AND " +
           "(:periodId IS NULL OR t.period.id = :periodId) AND " +
           "(:type IS NULL OR t.topicType = :type) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:createdById IS NULL OR t.createdBy.id = :createdById) AND " +
           "(:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(t.topicCode) LIKE LOWER(CONCAT('%', :search, '%')))" )
    Page<Topic> filterTopics(
        @Param("departmentId") Long departmentId,
        @Param("periodId") Long periodId,
        @Param("type") RegistrationPeriodType type,
        @Param("status") TopicStatus status,
        @Param("createdById") Long createdById,
        @Param("search") String search,
        Pageable pageable
    );

    @EntityGraph(attributePaths = {"department", "period", "advisor1", "advisor2"})
    @Query("select t from Topic t where t.status=topicmanagement.enums.TopicStatus.APPROVED "
        + "and (:search='' or lower(t.topicCode) like lower(concat('%',:search,'%')) "
        + "or lower(t.title) like lower(concat('%',:search,'%')) "
        + "or lower(coalesce(t.advisor1.fullName,'')) like lower(concat('%',:search,'%'))) "
        + "and (:department='' or t.department.name=:department) "
        + "and (:type is null or t.topicType=:type)")
    Page<Topic> searchPublishedCatalog(@Param("search") String search,
        @Param("department") String department, @Param("type") RegistrationPeriodType type,
        Pageable pageable);
}
