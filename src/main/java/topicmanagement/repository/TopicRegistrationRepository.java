package topicmanagement.repository;

import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import topicmanagement.entity.TopicRegistration;
import topicmanagement.enums.RegistrationStatus;

public interface TopicRegistrationRepository extends JpaRepository<TopicRegistration, Long> {
    Optional<TopicRegistration> findFirstByGroupIdOrderByCreatedAtDesc(Long groupId);
    List<TopicRegistration> findByGroupIdOrderByCreatedAtDesc(Long groupId);
    boolean existsByTopicPeriodId(Long periodId);
    Optional<TopicRegistration> findFirstByGroupIdAndStatusInOrderByCreatedAtDesc(
        Long groupId, Collection<RegistrationStatus> statuses);

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from TopicRegistration r where r.id=:id")
    Optional<TopicRegistration> lockById(@Param("id") Long id);

    @Query("select count(r) from TopicRegistration r where r.topic.period.id=:periodId "
        + "and r.status=:status and (r.topic.advisor1.id=:lecturerId or r.topic.advisor2.id=:lecturerId)")
    long countAdvisorGroups(@Param("periodId") Long periodId, @Param("lecturerId") Long lecturerId,
        @Param("status") RegistrationStatus status);

    @Query("select count(r) from TopicRegistration r where r.topic.id=:topicId and r.status in :statuses")
    long countByTopicAndStatuses(@Param("topicId") Long topicId,
        @Param("statuses") Collection<RegistrationStatus> statuses);
}
