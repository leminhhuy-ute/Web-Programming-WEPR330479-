package topicmanagement.repository;

import java.util.List;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import topicmanagement.entity.RegistrationPeriod;
import topicmanagement.enums.RegistrationPeriodType;

public interface RegistrationPeriodRepository extends JpaRepository<RegistrationPeriod, Long> {
  @Query(
      "select p from RegistrationPeriod p "
          + "where (:keyword='' or lower(p.name) like lower(concat('%',:keyword,'%'))) "
          + "and (:type is null or p.type=:type) "
          + "order by p.id desc")
  List<RegistrationPeriod> search(
      @Param("keyword") String keyword, @Param("type") RegistrationPeriodType type);

  @Query("select count(p) from RegistrationPeriod p where "
      + "(:now between p.lecturerStartAt and p.lecturerEndAt) or (:now between p.studentStartAt and p.studentEndAt)")
  long countActiveAt(@Param("now") LocalDateTime now);

  @Query("select p from RegistrationPeriod p where :now between p.studentStartAt and p.studentEndAt order by p.id desc")
  List<RegistrationPeriod> findActiveStudentPeriods(@Param("now") LocalDateTime now);

  java.util.Optional<RegistrationPeriod> findFirstByOrderByIdDesc();
}
