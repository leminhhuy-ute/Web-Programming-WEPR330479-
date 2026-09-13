package topicmanagement.controller;

import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import topicmanagement.dto.*;
import topicmanagement.dto.request.AnnouncementRequest;
import topicmanagement.dto.response.AnnouncementResponse;
import topicmanagement.enums.AnnouncementAudience;
import topicmanagement.security.CurrentUser;
import topicmanagement.service.AnnouncementServiceV2;

@RestController
@RequestMapping("/api/admin/announcements")
public class ApiAnnouncementController {
  private final AnnouncementServiceV2 s;

  public ApiAnnouncementController(AnnouncementServiceV2 s) {
    this.s = s;
  }

  @GetMapping
  public ApiResponse<List<AnnouncementResponse>> list(
      @RequestParam(defaultValue = "") String keyword,
      @RequestParam(required = false) AnnouncementAudience audience) {
    return ApiResponse.ok("Danh sách thông báo.", s.findAll(keyword, audience));
  }

  @GetMapping("/{id}")
  public ApiResponse<AnnouncementResponse> one(@PathVariable Long id) {
    return ApiResponse.ok("Chi tiết thông báo.", s.findById(id));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<AnnouncementResponse>> create(
      @Valid @RequestBody AnnouncementRequest r, @AuthenticationPrincipal CurrentUser user) {
    return ResponseEntity.status(201)
        .body(ApiResponse.ok("Đã tạo thông báo.", s.create(r, user.id())));
  }

  @PutMapping("/{id}")
  public ApiResponse<AnnouncementResponse> update(
      @PathVariable Long id, @Valid @RequestBody AnnouncementRequest r) {
    return ApiResponse.ok("Đã cập nhật thông báo.", s.update(id, r));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable Long id) {
    s.delete(id);
    return ApiResponse.ok("Đã xóa thông báo.", null);
  }
}
