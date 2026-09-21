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

    @org.springframework.data.jpa.repository.Query(
            "SELECT p FROM RegistrationPeriod p WHERE (:keyword = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:type IS NULL OR p.type = :type) ORDER BY p.id DESC")
    List<RegistrationPeriod> search(@org.springframework.data.repository.query.Param("keyword") String keyword,
                                    @org.springframework.data.repository.query.Param("type") RegistrationPeriodType type);
}
