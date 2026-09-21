package topicmanagement.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import topicmanagement.dto.ApiResponse;
import topicmanagement.dto.request.AssignAdvisorsRequest;
import topicmanagement.dto.request.TopicApprovalRequest;
import topicmanagement.dto.response.TopicResponse;
import topicmanagement.security.CurrentUser;
import topicmanagement.service.DepartmentTopicService;

@RestController
@RequestMapping("/api/lecturer/department-topics")
public class ApiDepartmentTopicController {

    private final DepartmentTopicService departmentTopicService;

    @Autowired
    public ApiDepartmentTopicController(DepartmentTopicService departmentTopicService) {
        this.departmentTopicService = departmentTopicService;
    }

    @GetMapping
    public ApiResponse<List<TopicResponse>> getDepartmentTopics(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal CurrentUser currentUser) {

        Long targetDeptId = departmentId;
        if (targetDeptId == null && currentUser != null && currentUser.getDepartmentId() != null) {
            targetDeptId = currentUser.getDepartmentId();
        }

        List<TopicResponse> topics = departmentTopicService.getDepartmentTopics(targetDeptId, status);
        return ApiResponse.ok("Lấy danh sách đề tài bộ môn thành công.", topics);
    }

    @PostMapping("/{id}/approval")
    public ApiResponse<TopicResponse> approveOrRejectTopic(
            @PathVariable Long id,
            @Valid @RequestBody TopicApprovalRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        TopicResponse topic = departmentTopicService.approveOrRejectTopic(id, request, currentUser.getUser());
        return ApiResponse.ok("Cập nhật trạng thái phê duyệt đề tài thành công.", topic);
    }

    @PostMapping("/{id}/assign-advisors")
    public ApiResponse<TopicResponse> assignAdvisors(
            @PathVariable Long id,
            @Valid @RequestBody AssignAdvisorsRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        TopicResponse topic = departmentTopicService.assignAdvisors(id, request, currentUser.getUser());
        return ApiResponse.ok("Phân công Giảng viên hướng dẫn thành công.", topic);
    }
}
