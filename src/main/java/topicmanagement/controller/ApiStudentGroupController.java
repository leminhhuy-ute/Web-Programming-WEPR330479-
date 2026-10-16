package topicmanagement.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import topicmanagement.dto.ApiResponse;
import topicmanagement.dto.request.GroupApprovalRequest;
import topicmanagement.dto.request.AdvisorGuidanceRequest;
import topicmanagement.dto.response.StudentGroupResponse;
import topicmanagement.dto.response.PageResponse;
import topicmanagement.security.CurrentUser;
import topicmanagement.service.StudentGroupService;

@org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('DEAN','HEAD_OF_DEPT','LECTURER')")
@RestController
@RequestMapping("/api/lecturer/student-groups")
public class ApiStudentGroupController {
    @Autowired private topicmanagement.security.CurrentAccount accounts;

    private final StudentGroupService groupService;

    @Autowired
    public ApiStudentGroupController(StudentGroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping("/{id}/guidance")
    public ApiResponse<StudentGroupService.Guidance> guidance(@PathVariable Long id) {
        return ApiResponse.ok("Lấy nhiệm vụ hướng dẫn thành công.", groupService.getGuidance(id, accounts.user()));
    }

    @PutMapping("/{id}/guidance")
    public ApiResponse<StudentGroupService.Guidance> updateGuidance(@PathVariable Long id,
            @Valid @RequestBody AdvisorGuidanceRequest request) {
        return ApiResponse.ok("Đã lưu nhiệm vụ và yêu cầu.",
            groupService.updateGuidance(id, request.tasks(), request.requirements(), accounts.user()));
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<StudentGroupService.Guidance> complete(@PathVariable Long id) {
        return ApiResponse.ok("Đã xác nhận hoàn thành đề tài.", groupService.confirmCompletion(id, accounts.user()));
    }

    @GetMapping
    public ApiResponse<List<StudentGroupResponse>> getMyStudentGroups(
            @AuthenticationPrincipal CurrentUser currentUser) {
        List<StudentGroupResponse> groups = groupService.getGroupsForLecturer(accounts.user());
        return ApiResponse.ok("Lấy danh sách nhóm sinh viên đăng ký thành công.", groups);
    }

    @GetMapping("/page")
    public ApiResponse<PageResponse<StudentGroupResponse>> getMyStudentGroupsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok("Lấy trang nhóm sinh viên thành công.",
            groupService.getGroupsForLecturer(accounts.user(), page, size));
    }

    @GetMapping("/topic/{topicId}")
    public ApiResponse<List<StudentGroupResponse>> getGroupsByTopic(@PathVariable Long topicId) {
        List<StudentGroupResponse> groups = groupService.getGroupsByTopic(topicId, accounts.user());
        return ApiResponse.ok("Lấy danh sách nhóm sinh viên theo đề tài thành công.", groups);
    }

    @PostMapping("/{id}/approval")
    public ApiResponse<StudentGroupResponse> approveOrRejectGroup(
            @PathVariable Long id,
            @Valid @RequestBody GroupApprovalRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        StudentGroupResponse response = groupService.approveOrRejectGroup(id, request, accounts.user());
        return ApiResponse.ok("Cập nhật trạng thái nhóm sinh viên thành công.", response);
    }
}
