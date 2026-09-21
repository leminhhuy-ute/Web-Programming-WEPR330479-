package vn.edu.hcmute.topicmanagement.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmute.topicmanagement.dto.ApiResponse;
import vn.edu.hcmute.topicmanagement.dto.request.AssignAdvisorsRequest;
import vn.edu.hcmute.topicmanagement.dto.request.GroupApprovalRequest;
import vn.edu.hcmute.topicmanagement.dto.request.TopicApprovalRequest;
import vn.edu.hcmute.topicmanagement.dto.response.TopicRegistrationResponse;
import vn.edu.hcmute.topicmanagement.dto.response.TopicResponse;
import vn.edu.hcmute.topicmanagement.enums.TopicStatus;
import vn.edu.hcmute.topicmanagement.security.CustomUserDetails;
import vn.edu.hcmute.topicmanagement.service.DepartmentTopicService;

import java.util.List;

@RestController
@RequestMapping("/api/lecturer/department-topics")
public class ApiDepartmentTopicController {

    private final DepartmentTopicService departmentTopicService;

    public ApiDepartmentTopicController(DepartmentTopicService departmentTopicService) {
        this.departmentTopicService = departmentTopicService;
    }

    @GetMapping
    public ApiResponse<List<TopicResponse>> list(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) TopicStatus status,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long deptId = (departmentId != null) ? departmentId :
                (userDetails != null && userDetails.getDepartment() != null ? userDetails.getDepartment().getId() : 1L);
        return ApiResponse.ok("Danh sách đề tài bộ môn.", departmentTopicService.getDepartmentTopics(deptId, status));
    }

    @PutMapping("/{id}/approval")
    public ApiResponse<TopicResponse> approveOrReject(
            @PathVariable Long id,
            @Valid @RequestBody TopicApprovalRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long reviewerId = userDetails != null ? userDetails.getId() : 1L;
        return ApiResponse.ok("Đã xử lý phê duyệt đề tài.", departmentTopicService.approveOrRejectTopic(id, request, reviewerId));
    }

    @PutMapping("/{id}/assign-advisors")
    public ApiResponse<TopicResponse> assignAdvisors(
            @PathVariable Long id,
            @Valid @RequestBody AssignAdvisorsRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long reviewerId = userDetails != null ? userDetails.getId() : 1L;
        return ApiResponse.ok("Đã phân công giảng viên hướng dẫn thành công.", departmentTopicService.assignAdvisors(id, request, reviewerId));
    }

    @GetMapping("/group-registrations")
    public ApiResponse<List<TopicRegistrationResponse>> listGroupRegistrations(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long advisorId = userDetails != null ? userDetails.getId() : 1L;
        return ApiResponse.ok("Danh sách sinh viên đăng ký đề tài.", departmentTopicService.getPendingRegistrationsForAdvisor(advisorId));
    }

    @PutMapping("/group-registrations/{id}/approval")
    public ApiResponse<TopicRegistrationResponse> reviewGroupRegistration(
            @PathVariable Long id,
            @Valid @RequestBody GroupApprovalRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long reviewerId = userDetails != null ? userDetails.getId() : 1L;
        return ApiResponse.ok("Đã xử lý phê duyệt nhóm sinh viên đăng ký đề tài.", departmentTopicService.reviewGroupRegistration(id, request, reviewerId));
    }
}
