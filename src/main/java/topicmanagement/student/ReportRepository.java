package topicmanagement.student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.Collection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import topicmanagement.dto.response.ReportSummaryResponse;
public interface ReportRepository extends JpaRepository<Report,Long> {
    List<Report> findByGroupIdOrderBySubmittedAtDesc(Long id);
    List<Report> findByRegistrationIdOrderBySubmittedAtDesc(Long registrationId);
    boolean existsByRegistrationId(Long registrationId);
    boolean existsByGroupIdAndStage(Long groupId, String stage);
    boolean existsByRegistrationIdAndStage(Long registrationId, String stage);

    @Query("select new topicmanagement.dto.response.ReportSummaryResponse("
        + "r.id,r.group.id,r.group.groupName,r.group.topic.topicCode,r.group.topic.title,"
        + "r.filename,r.contentType,r.stage,r.note,coalesce(r.reportSize,0),r.checksum,"
        + "r.submittedAt,r.late,r.submittedBy.userCode) "
        + "from Report r where r.group.id=:groupId order by r.submittedAt desc")
    List<ReportSummaryResponse> findSummariesByGroupId(@Param("groupId") Long groupId);

    @Query(value = "select new topicmanagement.dto.response.ReportSummaryResponse("
        + "r.id,r.group.id,r.group.groupName,r.group.topic.topicCode,r.group.topic.title,"
        + "r.filename,r.contentType,r.stage,r.note,coalesce(r.reportSize,0),r.checksum,"
        + "r.submittedAt,r.late,r.submittedBy.userCode) from Report r where r.group.id=:groupId",
        countQuery = "select count(r) from Report r where r.group.id=:groupId")
    Page<ReportSummaryResponse> findSummariesByGroupId(@Param("groupId") Long groupId, Pageable pageable);

    @Query("select new topicmanagement.dto.response.ReportSummaryResponse("
        + "r.id,r.group.id,r.group.groupName,r.group.topic.topicCode,r.group.topic.title,"
        + "r.filename,r.contentType,r.stage,r.note,coalesce(r.reportSize,0),r.checksum,"
        + "r.submittedAt,r.late,r.submittedBy.userCode) from Report r where r.id=:id")
    Optional<ReportSummaryResponse> findSummaryById(@Param("id") Long id);

    @Query("select new topicmanagement.dto.response.ReportSummaryResponse("
        + "r.id,r.group.id,r.group.groupName,r.group.topic.topicCode,r.group.topic.title,"
        + "r.filename,r.contentType,r.stage,r.note,coalesce(r.reportSize,0),r.checksum,"
        + "r.submittedAt,r.late,r.submittedBy.userCode) from Report r "
        + "where r.group.id in :groupIds order by r.submittedAt desc")
    List<ReportSummaryResponse> findSummariesByGroupIdIn(@Param("groupIds") Collection<Long> groupIds);
}
