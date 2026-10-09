package topicmanagement.controller;

import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.*;
import topicmanagement.dto.ApiResponse;
import topicmanagement.security.CurrentAccount;
import topicmanagement.service.TopicRegistrationService;

@org.springframework.security.access.prepost.PreAuthorize("hasRole('DEAN')")
@RestController
@RequestMapping("/api/admin/topic-registrations")
public class ApiTopicRegistrationController {
    record Cancellation(@Size(max = 2000) String note) {}

    private final TopicRegistrationService registrations;
    private final CurrentAccount accounts;

    public ApiTopicRegistrationController(TopicRegistrationService registrations, CurrentAccount accounts) {
        this.registrations = registrations;
        this.accounts = accounts;
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<?> cancel(@PathVariable Long id, @RequestBody Cancellation request) {
        var result = registrations.cancelByDean(id, accounts.user(), request.note());
        return ApiResponse.ok("Đã hủy đăng ký và giữ lại lịch sử.", result.getId());
    }
}
