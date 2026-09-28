package topicmanagement.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import topicmanagement.entity.RegistrationStatusHistory;

public interface RegistrationStatusHistoryRepository extends JpaRepository<RegistrationStatusHistory, Long> {
    List<RegistrationStatusHistory> findByRegistrationGroupIdOrderByChangedAtDesc(Long groupId);
    List<RegistrationStatusHistory> findByRegistrationIdOrderByChangedAtDesc(Long registrationId);
}
