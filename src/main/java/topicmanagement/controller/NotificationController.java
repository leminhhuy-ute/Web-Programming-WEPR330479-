package topicmanagement.controller;

import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import topicmanagement.dto.ApiResponse;
import topicmanagement.dto.request.AnnouncementRequest;
import topicmanagement.dto.response.AnnouncementResponse;
import topicmanagement.security.CurrentAccount;
import topicmanagement.service.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
  private final CurrentAccount accounts;
  private final NotificationService notifications;
  private final AnnouncementServiceV2 announcements;

  public NotificationController(CurrentAccount accounts, NotificationService notifications,
      AnnouncementServiceV2 announcements) {
    this.accounts = accounts;
    this.notifications = notifications;
    this.announcements = announcements;
  }

  @GetMapping
  public ResponseEntity<ApiResponse<NotificationService.Feed>> feed() {
    return ResponseEntity.ok().cacheControl(CacheControl.noStore())
        .body(ApiResponse.ok("Thông báo của bạn.", notifications.feed(accounts.user())));
  }

  @PostMapping("/{id}/read")
  public ApiResponse<Void> read(@PathVariable Long id) {
    notifications.markRead(accounts.user(), id);
    return ApiResponse.ok("Đã đánh dấu đã đọc.", null);
  }

  @PreAuthorize("hasRole('DEAN')")
  @PostMapping("/department")
  public ResponseEntity<ApiResponse<AnnouncementResponse>> createForStudents(
      @Valid @RequestBody AnnouncementRequest request) {
    return ResponseEntity.status(201).body(ApiResponse.ok("Đã gửi thông báo.",
        announcements.createForDepartment(request, accounts.user())));
  }
}
