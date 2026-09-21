package vn.edu.hcmute.topicmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.hcmute.topicmanagement.entity.StudentGroup;
import vn.edu.hcmute.topicmanagement.entity.Topic;
import vn.edu.hcmute.topicmanagement.entity.TopicRegistration;
import vn.edu.hcmute.topicmanagement.enums.GroupStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicRegistrationRepository extends JpaRepository<TopicRegistration, Long> {
    List<TopicRegistration> findByGroupOrderByRegisteredAtDesc(StudentGroup group);
    List<TopicRegistration> findByGroupAndStatus(StudentGroup group, GroupStatus status);
    Optional<TopicRegistration> findFirstByGroupAndStatus(StudentGroup group, GroupStatus status);
    List<TopicRegistration> findByTopic(Topic topic);
    List<TopicRegistration> findByTopicAndStatus(Topic topic, GroupStatus status);
    long countByTopicAndStatus(Topic topic, GroupStatus status);

    @org.springframework.data.jpa.repository.Query(
            "SELECT tr FROM TopicRegistration tr WHERE " +
            "tr.topic.advisor1.id = :advisorId OR tr.topic.advisor2.id = :advisorId OR tr.topic.createdBy.id = :advisorId " +
            "ORDER BY tr.registeredAt DESC")
    List<TopicRegistration> findByAdvisorId(@org.springframework.data.repository.query.Param("advisorId") Long advisorId);

    List<TopicRegistration> findByTopic_Department_Id(Long departmentId);
}
