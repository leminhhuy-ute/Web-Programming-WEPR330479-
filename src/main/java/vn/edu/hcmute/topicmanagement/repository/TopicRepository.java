package vn.edu.hcmute.topicmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.hcmute.topicmanagement.entity.Department;
import vn.edu.hcmute.topicmanagement.entity.RegistrationPeriod;
import vn.edu.hcmute.topicmanagement.entity.Topic;
import vn.edu.hcmute.topicmanagement.entity.User;
import vn.edu.hcmute.topicmanagement.enums.RegistrationPeriodType;
import vn.edu.hcmute.topicmanagement.enums.TopicStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
    Optional<Topic> findByTopicCode(String topicCode);
    boolean existsByTopicCode(String topicCode);

    List<Topic> findByDepartment(Department department);
    List<Topic> findByDepartmentId(Long departmentId);
    List<Topic> findByDepartmentIdAndStatus(Long departmentId, TopicStatus status);
    List<Topic> findByCreatedBy(User createdBy);
    List<Topic> findByAdvisor1OrAdvisor2(User advisor1, User advisor2);
    List<Topic> findByCreatedByIdOrAdvisor1IdOrAdvisor2Id(Long createdById, Long advisor1Id, Long advisor2Id);
    List<Topic> findByPeriod(RegistrationPeriod period);
    List<Topic> findByStatus(TopicStatus status);
    List<Topic> findByDepartmentAndStatus(Department department, TopicStatus status);

    @Query("SELECT t FROM Topic t WHERE " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:departmentId IS NULL OR t.department.id = :departmentId) AND " +
           "(:periodType IS NULL OR t.topicType = :periodType) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           " LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(t.topicCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(t.createdBy.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(t.requirements) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Topic> filterTopics(@Param("keyword") String keyword,
                             @Param("departmentId") Long departmentId,
                             @Param("periodType") RegistrationPeriodType periodType,
                             @Param("status") TopicStatus status);
}
