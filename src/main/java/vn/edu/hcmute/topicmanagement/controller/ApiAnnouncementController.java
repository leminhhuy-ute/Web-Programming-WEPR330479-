package vn.edu.hcmute.topicmanagement.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmute.topicmanagement.dto.ApiResponse;
import vn.edu.hcmute.topicmanagement.dto.request.AnnouncementRequest;
import vn.edu.hcmute.topicmanagement.dto.response.AnnouncementResponse;
import vn.edu.hcmute.topicmanagement.enums.AnnouncementAudience;
import vn.edu.hcmute.topicmanagement.security.CustomUserDetails;
import vn.edu.hcmute.topicmanagement.service.AnnouncementService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/announcements")
public class ApiAnnouncementController {

    private final AnnouncementService announcementService;

    public ApiAnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping
    public ApiResponse<List<AnnouncementResponse>> list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) AnnouncementAudience audience) {
        return ApiResponse.ok("Danh sách thông báo.", announcementService.findAll(keyword, audience));
    }

    @GetMapping("/{id}")
    public ApiResponse<AnnouncementResponse> one(@PathVariable Long id) {
        return ApiResponse.ok("Chi tiết thông báo.", announcementService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AnnouncementResponse>> create(
            @Valid @RequestBody AnnouncementRequest r,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long authorId = userDetails != null ? userDetails.getId() : 1L;
        return ResponseEntity.status(201).body(ApiResponse.ok("Đã tạo thông báo thành công.", announcementService.create(r, authorId)));
    }

    @PutMapping("/{id}")
    public ApiResponse<AnnouncementResponse> update(
            @PathVariable Long id, @Valid @RequestBody AnnouncementRequest r) {
        return ApiResponse.ok("Đã cập nhật thông báo thành công.", announcementService.update(id, r));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return ApiResponse.ok("Đã xóa thông báo thành công.", null);
    }
}
