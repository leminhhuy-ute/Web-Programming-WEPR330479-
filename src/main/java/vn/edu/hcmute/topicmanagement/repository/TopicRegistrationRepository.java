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
}
