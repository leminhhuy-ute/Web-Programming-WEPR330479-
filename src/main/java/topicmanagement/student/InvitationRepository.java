package topicmanagement.student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface InvitationRepository extends JpaRepository<Invitation,Long> {
    List<Invitation> findByStudentIdAndStatusOrderByCreatedAtDesc(Long id,String status);
    List<Invitation> findByGroupIdOrderByCreatedAtDesc(Long id);
    Optional<Invitation> findByGroupIdAndStudentId(Long groupId,Long studentId);
}
