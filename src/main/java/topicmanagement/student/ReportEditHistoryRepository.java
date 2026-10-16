package topicmanagement.student;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import topicmanagement.dto.response.ReportEditHistoryResponse;

public interface ReportEditHistoryRepository extends JpaRepository<ReportEditHistory, Long> {
    @Query("select new topicmanagement.dto.response.ReportEditHistoryResponse(" +
        "h.id,h.report.id,h.filename,h.contentType,h.note,coalesce(h.reportSize,0),h.checksum,h.editedAt,h.changedBy.fullName) " +
        "from ReportEditHistory h where h.report.id=:reportId order by h.editedAt desc")
    List<ReportEditHistoryResponse> findSummariesByReportId(@Param("reportId") Long reportId);
}
