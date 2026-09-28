package topicmanagement.controller;

import java.nio.charset.StandardCharsets;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import topicmanagement.dto.ApiResponse;
import topicmanagement.dto.response.ReportSummaryResponse;
import topicmanagement.dto.response.PageResponse;
import topicmanagement.security.CurrentAccount;
import topicmanagement.student.*;

@RestController
public class ReportAccessController {
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
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename(report.filename, StandardCharsets.UTF_8).build().toString())
            .header(HttpHeaders.CACHE_CONTROL, "no-store")
            .header("X-Content-Type-Options", "nosniff")
            .contentType(MediaType.parseMediaType(report.contentType))
            .body(report.content);
    }
}
