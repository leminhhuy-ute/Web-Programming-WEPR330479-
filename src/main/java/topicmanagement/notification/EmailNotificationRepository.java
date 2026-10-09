package topicmanagement.notification;
import java.time.LocalDateTime;
import java.util.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
public interface EmailNotificationRepository extends JpaRepository<EmailNotification,Long> {
    boolean existsByDefenseIdAndStudentId(Long defenseId,Long studentId);
    @Query("select n.id from EmailNotification n where n.status=topicmanagement.notification.EmailNotification.Status.PENDING and n.nextAttemptAt<=:now order by n.id")
    List<Long> due(LocalDateTime now,Pageable page);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select n from EmailNotification n where n.id=:id")
    Optional<EmailNotification> lockById(Long id);
    @EntityGraph(attributePaths={"student","defense","defense.group"})
    Page<EmailNotification> findByStatus(EmailNotification.Status status,Pageable page);
    @Override @EntityGraph(attributePaths={"student","defense","defense.group"})
    Page<EmailNotification> findAll(Pageable page);
}
