package vn.edu.hcmute.topicmanagement.controller;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.hcmute.topicmanagement.dto.ApiResponse;
import vn.edu.hcmute.topicmanagement.dto.request.*;
import vn.edu.hcmute.topicmanagement.dto.response.*;
import vn.edu.hcmute.topicmanagement.security.CustomUserDetails;
import vn.edu.hcmute.topicmanagement.service.StudentGroupService;

import java.util.List;

@RestController
@RequestMapping("/api/student")
public class ApiStudentController {

    private final StudentGroupService studentGroupService;

    public ApiStudentController(StudentGroupService studentGroupService) {
        this.studentGroupService = studentGroupService;
    }

    @GetMapping("/workspace")
    public ApiResponse<StudentWorkspaceResponse> getWorkspace(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long studentId = userDetails != null ? userDetails.getId() : 1L;
        return ApiResponse.ok("Thông tin nhóm và học tập sinh viên.", studentGroupService.getWorkspace(studentId));
    }

    @PostMapping("/groups")
    public ResponseEntity<ApiResponse<StudentGroupResponse>> createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long studentId = userDetails != null ? userDetails.getId() : 1L;
        return ResponseEntity.status(201).body(ApiResponse.ok("Tạo nhóm sinh viên thành công.", studentGroupService.createGroup(request, studentId)));
    }

    @PostMapping("/groups/invite")
    public ApiResponse<InvitationResponse> inviteMember(
            @Valid @RequestBody InviteMemberRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long studentId = userDetails != null ? userDetails.getId() : 1L;
        return ApiResponse.ok("Đã gửi lời mời tham gia nhóm.", studentGroupService.inviteMember(request, studentId));
    }

    @PutMapping("/invitations/{id}")
    public ApiResponse<Void> respondInvitation(
            @PathVariable Long id,
            @RequestBody RespondInvitationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long studentId = userDetails != null ? userDetails.getId() : 1L;
        studentGroupService.respondInvitation(id, request.accept(), studentId);
        String msg = request.accept() ? "Đã chấp nhận tham gia nhóm." : "Đã từ chối lời mời.";
        return ApiResponse.ok(msg, null);
    }

    @PutMapping("/groups/transfer-leader")
    public ApiResponse<Void> transferLeader(
            @Valid @RequestBody TransferLeaderRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long studentId = userDetails != null ? userDetails.getId() : 1L;
        studentGroupService.transferLeader(request, studentId);
        return ApiResponse.ok("Đã chuyển giao vai trò trưởng nhóm thành công.", null);
    }

    @DeleteMapping("/groups/leave")
    public ApiResponse<Void> leaveGroup(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long studentId = userDetails != null ? userDetails.getId() : 1L;
        studentGroupService.leaveGroup(studentId);
        return ApiResponse.ok("Đã rời khỏi nhóm thành công.", null);
    }

    @PostMapping("/topics/register")
    public ApiResponse<TopicRegistrationResponse> registerTopic(
            @Valid @RequestBody RegisterTopicRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long studentId = userDetails != null ? userDetails.getId() : 1L;
        return ApiResponse.ok("Đã gửi đơn đăng ký đề tài.", studentGroupService.registerTopic(request, studentId));
    }

    @PostMapping(value = "/reports", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ApiResponse<ProgressReportResponse> submitReportWithFile(
            @RequestParam("reportTitle") String reportTitle,
            @RequestParam("stage") String stage,
            @RequestParam(value = "contentSummary", required = false) String contentSummary,
            @RequestParam(value = "attachmentUrl", required = false) String attachmentUrl,
            @RequestParam(value = "completionPercentage", defaultValue = "0") int completionPercentage,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long studentId = userDetails != null ? userDetails.getId() : 1L;
        SubmitReportRequest request = new SubmitReportRequest(reportTitle, stage, contentSummary, attachmentUrl, completionPercentage);
        return ApiResponse.ok("Đã nộp báo cáo tiến độ thành công.", studentGroupService.submitReport(request, file, studentId));
    }

    @PostMapping(value = "/reports", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ApiResponse<ProgressReportResponse> submitReportJson(
            @Valid @RequestBody SubmitReportRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long studentId = userDetails != null ? userDetails.getId() : 1L;
        return ApiResponse.ok("Đã nộp báo cáo tiến độ thành công.", studentGroupService.submitReport(request, null, studentId));
    }

    @GetMapping("/search-available")
    public ApiResponse<List<UserResponse>> searchAvailableStudents(@RequestParam(defaultValue = "") String keyword) {
        return ApiResponse.ok("Danh sách sinh viên chưa có nhóm.", studentGroupService.searchAvailableStudents(keyword));
    }
}
