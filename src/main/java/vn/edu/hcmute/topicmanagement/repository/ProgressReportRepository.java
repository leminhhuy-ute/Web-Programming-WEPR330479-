package vn.edu.hcmute.topicmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.hcmute.topicmanagement.entity.ProgressReport;
import vn.edu.hcmute.topicmanagement.entity.StudentGroup;

import java.util.List;

@Repository
public interface ProgressReportRepository extends JpaRepository<ProgressReport, Long> {
    List<ProgressReport> findByGroupOrderBySubmittedAtDesc(StudentGroup group);
    List<ProgressReport> findByGroupAndStage(StudentGroup group, String stage);
}
