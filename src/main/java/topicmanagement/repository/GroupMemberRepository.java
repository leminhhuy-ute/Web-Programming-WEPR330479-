package topicmanagement.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import topicmanagement.entity.GroupMember;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByGroupId(Long groupId);
    List<GroupMember> findByStudentId(Long studentId);
    boolean existsByGroupIdAndStudentId(Long groupId, Long studentId);
    boolean existsByRegistrationPeriodIdAndStudentId(Long periodId, Long studentId);
    Optional<GroupMember> findByRegistrationPeriodIdAndStudentId(Long periodId, Long studentId);
    void deleteByGroupIdAndStudentId(Long groupId, Long studentId);
    void deleteByGroupId(Long groupId);
}
