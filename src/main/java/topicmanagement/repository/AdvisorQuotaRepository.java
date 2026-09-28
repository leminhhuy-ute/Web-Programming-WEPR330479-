package topicmanagement.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import topicmanagement.entity.AdvisorQuota;

public interface AdvisorQuotaRepository extends JpaRepository<AdvisorQuota, Long> {
    Optional<AdvisorQuota> findByRegistrationPeriodIdAndLecturerId(Long periodId, Long lecturerId);

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select q from AdvisorQuota q where q.registrationPeriod.id=:periodId and q.lecturer.id=:lecturerId")
    Optional<AdvisorQuota> lockByPeriodAndLecturer(@Param("periodId") Long periodId,
        @Param("lecturerId") Long lecturerId);
}
