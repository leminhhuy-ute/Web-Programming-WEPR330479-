package topicmanagement.repository;

import java.util.List;
import java.util.Optional;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import topicmanagement.entity.StudentGroup;
import topicmanagement.enums.GroupStatus;

public interface StudentGroupRepository extends JpaRepository<StudentGroup, Long> {
    Optional<StudentGroup> findByGroupCode(String groupCode);
    Optional<StudentGroup> findByLeaderId(Long leaderId);
    boolean existsByGroupCode(String groupCode);
    long countByStatus(GroupStatus status);

    List<StudentGroup> findByTopicId(Long topicId);
    List<StudentGroup> findByStatus(GroupStatus status);
    List<StudentGroup> findByTopicIsNotNull();
    
    @Query("SELECT g FROM StudentGroup g JOIN g.topic t LEFT JOIN t.advisor1 a1 LEFT JOIN t.advisor2 a2 WHERE a1.id = :lecturerId OR a2.id = :lecturerId")
    List<StudentGroup> findGroupsForLecturer(@Param("lecturerId") Long lecturerId);

    @EntityGraph(attributePaths = {"topic", "topic.department", "leader"})
    @Query("SELECT g FROM StudentGroup g JOIN g.topic t LEFT JOIN t.advisor1 a1 LEFT JOIN t.advisor2 a2 WHERE a1.id = :lecturerId OR a2.id = :lecturerId")
    Page<StudentGroup> findGroupsForLecturer(@Param("lecturerId") Long lecturerId, Pageable pageable);

    @Query("SELECT g FROM StudentGroup g WHERE g.topic.department.id = :departmentId")
    List<StudentGroup> findGroupsByDepartment(@Param("departmentId") Long departmentId);

    @EntityGraph(attributePaths = {"topic", "topic.department", "leader"})
    @Query("SELECT g FROM StudentGroup g WHERE g.topic.department.id = :departmentId")
    Page<StudentGroup> findGroupsByDepartment(@Param("departmentId") Long departmentId, Pageable pageable);

    @EntityGraph(attributePaths = {"topic", "topic.department", "leader"})
    Page<StudentGroup> findByTopicIsNotNull(Pageable pageable);

    List<StudentGroup> findByTopicIdAndStatus(Long topicId, GroupStatus status);

    @Query("select g.topic.id,count(g) from StudentGroup g where g.topic.id in :topicIds "
        + "and g.status<>topicmanagement.enums.GroupStatus.REJECTED group by g.topic.id")
    List<Object[]> countActiveByTopicIds(@Param("topicIds") Collection<Long> topicIds);
}
