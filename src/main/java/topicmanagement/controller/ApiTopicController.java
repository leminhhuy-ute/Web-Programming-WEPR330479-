package topicmanagement.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import topicmanagement.dto.ApiResponse;
import topicmanagement.dto.request.TopicRequest;
import topicmanagement.dto.response.TopicResponse;
import topicmanagement.security.CurrentUser;
import topicmanagement.service.TopicService;

@RestController
@RequestMapping("/api/lecturer/topics")
public class ApiTopicController {
    @Autowired private topicmanagement.security.CurrentAccount accounts;

    private final TopicService topicService;

    @Autowired
    public ApiTopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @GetMapping
    public ApiResponse<List<TopicResponse>> filterTopics(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long periodId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean myOnly,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal CurrentUser currentUser) {

        Long createdById = (Boolean.TRUE.equals(myOnly) && currentUser != null) ? currentUser.id() : null;
        List<TopicResponse> topics = topicService.filterTopics(departmentId, periodId, type, status, createdById, search);
        return ApiResponse.ok("Lấy danh sách đề tài thành công.", topics);
    }

    @GetMapping("/{id}")
    public ApiResponse<TopicResponse> getTopicById(@PathVariable Long id) {
        TopicResponse topic = topicService.getTopicById(id);
        return ApiResponse.ok("Lấy chi tiết đề tài thành công.", topic);
    }

    @PostMapping
    public ApiResponse<TopicResponse> createTopic(
            @Valid @RequestBody TopicRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        TopicResponse created = topicService.createTopic(request, accounts.user());
        return ApiResponse.ok("Đăng ký / đề xuất đề tài mới thành công.", created);
    }

    @PutMapping("/{id}")
    public ApiResponse<TopicResponse> updateTopic(
            @PathVariable Long id,
            @Valid @RequestBody TopicRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        TopicResponse updated = topicService.updateTopic(id, request, accounts.user());
        return ApiResponse.ok("Cập nhật thông tin đề tài thành công.", updated);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTopic(
            @PathVariable Long id,
            @AuthenticationPrincipal CurrentUser currentUser) {
        topicService.deleteTopic(id, accounts.user());
        return ApiResponse.ok("Xóa đề tài thành công.", null);
    }
}
