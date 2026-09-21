package vn.edu.hcmute.topicmanagement.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmute.topicmanagement.dto.ApiResponse;
import vn.edu.hcmute.topicmanagement.dto.request.TopicRequest;
import vn.edu.hcmute.topicmanagement.dto.response.TopicResponse;
import vn.edu.hcmute.topicmanagement.enums.RegistrationPeriodType;
import vn.edu.hcmute.topicmanagement.enums.TopicStatus;
import vn.edu.hcmute.topicmanagement.security.CustomUserDetails;
import vn.edu.hcmute.topicmanagement.service.TopicService;

import java.util.List;

@RestController
@RequestMapping("/api/lecturer/topics")
public class ApiTopicController {

    private final TopicService topicService;

    public ApiTopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @GetMapping
    public ApiResponse<List<TopicResponse>> list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) RegistrationPeriodType periodType,
            @RequestParam(required = false) TopicStatus status) {
        return ApiResponse.ok("Danh sách đề tài.", topicService.filterTopics(keyword, departmentId, periodType, status));
    }

    @GetMapping("/my-topics")
    public ApiResponse<List<TopicResponse>> getMyTopics(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails != null ? userDetails.getId() : 1L;
        return ApiResponse.ok("Danh sách đề tài của tôi.", topicService.getMyTopics(userId));
    }

    @GetMapping("/{id}")
    public ApiResponse<TopicResponse> one(@PathVariable Long id) {
        return ApiResponse.ok("Chi tiết đề tài.", topicService.getTopicById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TopicResponse>> create(
            @Valid @RequestBody TopicRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails != null ? userDetails.getId() : 1L;
        return ResponseEntity.status(201).body(ApiResponse.ok("Đã gửi đề xuất đề tài thành công.", topicService.createTopic(request, userId)));
    }

    @PutMapping("/{id}")
    public ApiResponse<TopicResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TopicRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails != null ? userDetails.getId() : 1L;
        return ApiResponse.ok("Đã cập nhật đề tài thành công.", topicService.updateTopic(id, request, userId));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails != null ? userDetails.getId() : 1L;
        topicService.deleteTopic(id, userId);
        return ApiResponse.ok("Đã xóa đề tài thành công.", null);
    }
}
