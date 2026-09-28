package topicmanagement.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import topicmanagement.dto.ApiResponse;
import topicmanagement.dto.request.AdvisorQuotaRequest;
import topicmanagement.dto.response.AdvisorQuotaResponse;
import topicmanagement.service.AdvisorQuotaService;

@RestController
@RequestMapping("/api/admin/advisor-quotas")
public class ApiAdvisorQuotaController {
    private final AdvisorQuotaService quotas;
    public ApiAdvisorQuotaController(AdvisorQuotaService quotas) { this.quotas = quotas; }

    @GetMapping
    public ApiResponse<List<AdvisorQuotaResponse>> list() {
        return ApiResponse.ok("Danh sách hạn mức hướng dẫn.", quotas.findAll());
    }

    @PostMapping
    public ApiResponse<AdvisorQuotaResponse> save(@Valid @RequestBody AdvisorQuotaRequest request) {
        return ApiResponse.ok("Đã lưu hạn mức hướng dẫn.", quotas.save(request));
    }
}
