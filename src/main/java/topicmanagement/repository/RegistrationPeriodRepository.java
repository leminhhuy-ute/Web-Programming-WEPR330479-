package topicmanagement.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import topicmanagement.entity.RegistrationPeriod;
import topicmanagement.enums.RegistrationPeriodType;

public interface RegistrationPeriodRepository extends JpaRepository<RegistrationPeriod, Long> {
    List<RegistrationPeriod> findByType(RegistrationPeriodType type);
}
