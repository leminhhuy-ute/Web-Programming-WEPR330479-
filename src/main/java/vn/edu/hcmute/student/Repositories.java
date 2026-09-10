package vn.edu.hcmute.student;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

interface StudentRepository extends JpaRepository<Student,String> {
    List<Student> findByGroupIdOrderById(Long groupId);
    long countByGroupId(Long groupId);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select s from Student s where s.id=:id")
    Optional<Student> lock(@Param("id") String id);
}
interface GroupRepository extends JpaRepository<StudentGroup,Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select g from StudentGroup g where g.id=:id")
    Optional<StudentGroup> lock(@Param("id") Long id);
}
interface InvitationRepository extends JpaRepository<Invitation,Long> {
    List<Invitation> findByStudentIdAndStatusOrderByCreatedAtDesc(String id,String status);
    List<Invitation> findByGroupIdOrderByCreatedAtDesc(Long id);
    Optional<Invitation> findByGroupIdAndStudentId(Long groupId,String studentId);
}
interface TopicRepository extends JpaRepository<Topic,String> {
    List<Topic> findByPublishedTrueOrderById();
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select t from Topic t where t.id=:id")
    Optional<Topic> lock(@Param("id") String id);
}
interface RegistrationRepository extends JpaRepository<Registration,Long> {
    Optional<Registration> findByGroupId(Long id);
    long countByTopicIdAndStatusIn(String id,Collection<String> statuses);
}
interface ReportRepository extends JpaRepository<Report,Long> {
    List<Report> findByGroupIdOrderBySubmittedAtDesc(Long id);
}
