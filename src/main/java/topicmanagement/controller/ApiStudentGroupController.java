package topicmanagement.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import topicmanagement.dto.ApiResponse;
import topicmanagement.dto.request.GroupApprovalRequest;
import topicmanagement.dto.response.StudentGroupResponse;
import topicmanagement.security.CurrentUser;
import topicmanagement.service.StudentGroupService;

@RestController
@RequestMapping("/api/lecturer/student-groups")
public class ApiStudentGroupController {
    @Autowired private topicmanagement.security.CurrentAccount accounts;

    private final StudentGroupService groupService;

    @Autowired
    public ApiStudentGroupController(StudentGroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping
    public ApiResponse<List<StudentGroupResponse>> getMyStudentGroups(
            @AuthenticationPrincipal CurrentUser currentUser) {
        List<StudentGroupResponse> groups = groupService.getGroupsForLecturer(accounts.user());
        return ApiResponse.ok("Lấy danh sách nhóm sinh viên đăng ký thành công.", groups);
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
