package vn.edu.hcmute.topicmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.hcmute.topicmanagement.entity.RegistrationPeriod;
import vn.edu.hcmute.topicmanagement.enums.RegistrationPeriodType;

import java.util.List;

@Repository
public interface RegistrationPeriodRepository extends JpaRepository<RegistrationPeriod, Long> {
    List<RegistrationPeriod> findByType(RegistrationPeriodType type);
    List<RegistrationPeriod> findAllByOrderByCreatedAtDesc();
}
