package topicmanagement.controller;

import java.nio.charset.StandardCharsets;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import topicmanagement.dto.ApiResponse;
import topicmanagement.dto.response.ReportSummaryResponse;
import topicmanagement.dto.response.PageResponse;
import topicmanagement.security.CurrentAccount;
import topicmanagement.student.*;
import topicmanagement.enums.ReportStatus;

@org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('DEAN','LECTURER')")
@RestController
public class ReportAccessController {
    public record AdvisorFeedbackRequest(@NotBlank(message="Vui lòng nhập nhận xét.")
        @Size(max=2000,message="Nhận xét tối đa 2.000 ký tự.") String feedback) {}
    public record ReportReviewRequest(@NotNull(message="Vui lòng chọn trạng thái.") ReportStatus status,
        @Size(max=2000,message="Nhận xét tối đa 2.000 ký tự.") String feedback) {}
    private final ReportService reports;
    private final CurrentAccount accounts;

    public ReportAccessController(ReportService reports, CurrentAccount accounts) {
        this.reports = reports;
        this.accounts = accounts;
    }

    @GetMapping("/api/lecturer/groups/{groupId}/reports")
    public ApiResponse<?> advisorReports(@PathVariable Long groupId) {
        return ApiResponse.ok("Danh sách báo cáo của nhóm.", reports.listForAdvisor(accounts.user(), groupId));
    }

    @GetMapping("/api/lecturer/groups/{groupId}/reports/page")
    public ApiResponse<PageResponse<ReportSummaryResponse>> advisorReportPage(@PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok("Trang báo cáo của nhóm.",
            reports.pageForAdvisor(accounts.user(), groupId, page, size));
    }

    @GetMapping("/api/lecturer/reports/{id}")
    public ApiResponse<ReportSummaryResponse> advisorMetadata(@PathVariable Long id) {
        return ApiResponse.ok("Thông tin báo cáo.", reports.metadataForAdvisor(accounts.user(), id));
    }

    @GetMapping("/api/lecturer/reports/{id}/download")
    public ResponseEntity<byte[]> advisorDownload(@PathVariable Long id) {
        return download(reports.downloadForAdvisor(accounts.user(), id));
    }

    @PostMapping("/api/lecturer/reports/{id}/feedback")
    public ApiResponse<ReportSummaryResponse> advisorFeedback(@PathVariable Long id,
            @Valid @RequestBody AdvisorFeedbackRequest request) {
        return ApiResponse.ok("Đã lưu nhận xét của giảng viên hướng dẫn.",
            reports.saveAdvisorFeedback(accounts.user(), id, request.feedback()));
    }

    @PutMapping("/api/lecturer/reports/{id}/review")
    public ApiResponse<ReportSummaryResponse> reviewReport(@PathVariable Long id,
            @Valid @RequestBody ReportReviewRequest request) {
        return ApiResponse.ok("Đã cập nhật trạng thái và nhận xét báo cáo.",
            reports.reviewReport(accounts.user(), id, request.status(), request.feedback()));
    }

    @GetMapping("/api/lecturer/reports/{id}/history")
    public ApiResponse<?> reportHistory(@PathVariable Long id) {
        return ApiResponse.ok("Lịch sử chỉnh sửa báo cáo.", reports.editHistoryForAdvisor(accounts.user(), id));
    }

    @GetMapping("/api/lecturer/reports/{id}/history/{historyId}/download")
    public ResponseEntity<byte[]> downloadReportVersion(@PathVariable Long id, @PathVariable Long historyId) {
        ReportEditHistory version = reports.editVersionDownloadForAdvisor(accounts.user(), id, historyId);
        return download(version.filename, version.contentType, version.content);
    }

    @GetMapping("/api/council/defenses/{defenseId}/reports")
    public ApiResponse<?> councilReports(@PathVariable Long defenseId) {
        return ApiResponse.ok("Danh sách báo cáo của đề tài phản biện.",
            reports.listForCouncil(accounts.user(), defenseId));
    }

    @GetMapping("/api/council/defenses/{defenseId}/reports/page")
    public ApiResponse<PageResponse<ReportSummaryResponse>> councilReportPage(@PathVariable Long defenseId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok("Trang báo cáo của đề tài phản biện.",
            reports.pageForCouncil(accounts.user(), defenseId, page, size));
    }

    @GetMapping("/api/council/reports/{id}")
    public ApiResponse<ReportSummaryResponse> councilMetadata(@PathVariable Long id) {
        return ApiResponse.ok("Thông tin báo cáo.", reports.metadataForCouncil(accounts.user(), id));
    }

    @GetMapping("/api/council/reports/{id}/download")
    public ResponseEntity<byte[]> councilDownload(@PathVariable Long id) {
        return download(reports.downloadForCouncil(accounts.user(), id));
    }

    private ResponseEntity<byte[]> download(Report report) {
        return download(report.filename, report.contentType, report.content);
    }

    private ResponseEntity<byte[]> download(String filename, String contentType, byte[] content) {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build().toString())
            .header(HttpHeaders.CACHE_CONTROL, "no-store")
            .header("X-Content-Type-Options", "nosniff")
            .contentType(MediaType.parseMediaType(contentType))
            .body(content);
    }
}
