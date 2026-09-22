package topicmanagement.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import topicmanagement.entity.StudentGroup;
import topicmanagement.enums.GroupStatus;

public interface StudentGroupRepository extends JpaRepository<StudentGroup, Long> {
    Optional<StudentGroup> findByGroupCode(String groupCode);
    boolean existsByGroupCode(String groupCode);

    List<StudentGroup> findByTopicId(Long topicId);
    
    @Query("SELECT g FROM StudentGroup g JOIN g.topic t LEFT JOIN t.advisor1 a1 LEFT JOIN t.advisor2 a2 WHERE a1.id = :lecturerId OR a2.id = :lecturerId")
    List<StudentGroup> findGroupsForLecturer(@Param("lecturerId") Long lecturerId);

    @Query("SELECT g FROM StudentGroup g WHERE g.topic.department.id = :departmentId")
    List<StudentGroup> findGroupsByDepartment(@Param("departmentId") Long departmentId);

    List<StudentGroup> findByTopicIdAndStatus(Long topicId, GroupStatus status);
}
