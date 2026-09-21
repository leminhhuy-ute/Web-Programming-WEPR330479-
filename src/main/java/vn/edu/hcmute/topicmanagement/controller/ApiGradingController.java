package vn.edu.hcmute.topicmanagement.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmute.topicmanagement.dto.ApiResponse;
import vn.edu.hcmute.topicmanagement.dto.request.AssignCouncilAndReviewerRequest;
import vn.edu.hcmute.topicmanagement.dto.request.GradeEvaluationRequest;
import vn.edu.hcmute.topicmanagement.dto.response.EvaluationResponse;
import vn.edu.hcmute.topicmanagement.security.CustomUserDetails;
import vn.edu.hcmute.topicmanagement.service.GradingService;

import java.util.List;

@RestController
@RequestMapping("/api/grading")
public class ApiGradingController {

    private final GradingService gradingService;

    public ApiGradingController(GradingService gradingService) {
        this.gradingService = gradingService;
    }

    @PostMapping("/assign")
    public ApiResponse<Void> assignCouncilAndReviewer(@Valid @RequestBody AssignCouncilAndReviewerRequest request) {
        gradingService.assignCouncilAndReviewer(request);
        return ApiResponse.ok("Đã phân công hội đồng và cán bộ phản biện thành công.", null);
    }

    @PostMapping("/evaluate")
    public ApiResponse<EvaluationResponse> evaluateTopic(
            @Valid @RequestBody GradeEvaluationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long evaluatorId = userDetails != null ? userDetails.getId() : 1L;
        return ApiResponse.ok("Đã ghi nhận điểm và nhận xét đánh giá.", gradingService.gradeTopic(request, evaluatorId));
    }

    @GetMapping("/topics/{topicId}/evaluations")
    public ApiResponse<List<EvaluationResponse>> getTopicEvaluations(@PathVariable Long topicId) {
        return ApiResponse.ok("Danh sách đánh giá điểm của đề tài.", gradingService.getTopicEvaluations(topicId));
    }

    @GetMapping("/councils/{councilId}/evaluations")
    public ApiResponse<List<EvaluationResponse>> getCouncilEvaluations(@PathVariable Long councilId) {
        return ApiResponse.ok("Danh sách đánh giá điểm của hội đồng.", gradingService.getCouncilEvaluations(councilId));
    }
}
