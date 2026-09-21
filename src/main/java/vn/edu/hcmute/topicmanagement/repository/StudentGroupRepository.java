package vn.edu.hcmute.topicmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.hcmute.topicmanagement.entity.StudentGroup;
import vn.edu.hcmute.topicmanagement.entity.Topic;
import vn.edu.hcmute.topicmanagement.entity.User;
import vn.edu.hcmute.topicmanagement.enums.GroupStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentGroupRepository extends JpaRepository<StudentGroup, Long> {
    Optional<StudentGroup> findByGroupCode(String groupCode);
    List<StudentGroup> findByLeader(User leader);
    List<StudentGroup> findByTopic(Topic topic);
    List<StudentGroup> findByTopicAndStatus(Topic topic, GroupStatus status);

    @Query("SELECT g FROM StudentGroup g JOIN g.members m WHERE m.student.id = :studentId")
    Optional<StudentGroup> findByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT g FROM StudentGroup g WHERE g.topic.createdBy.id = :lecturerId OR g.topic.advisor1.id = :lecturerId OR g.topic.advisor2.id = :lecturerId")
    List<StudentGroup> findByLecturerId(@Param("lecturerId") Long lecturerId);
}
