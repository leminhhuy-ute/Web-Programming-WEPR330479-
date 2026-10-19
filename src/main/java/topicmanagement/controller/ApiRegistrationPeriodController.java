package topicmanagement.controller;

import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import topicmanagement.dto.*;
import topicmanagement.dto.request.RegistrationPeriodRequest;
import topicmanagement.dto.response.RegistrationPeriodResponse;
import topicmanagement.dto.response.PageResponse;
import topicmanagement.enums.RegistrationPeriodType;
import topicmanagement.service.RegistrationPeriodServiceV2;

@org.springframework.security.access.prepost.PreAuthorize("hasRole('DEAN')")
@RestController
@RequestMapping("/api/admin/registration-periods")
public class ApiRegistrationPeriodController {
  private final RegistrationPeriodServiceV2 s;

  public ApiRegistrationPeriodController(RegistrationPeriodServiceV2 s) {
    this.s = s;
  }

  @GetMapping
  public ApiResponse<List<RegistrationPeriodResponse>> list(
      @RequestParam(defaultValue = "") String keyword,
      @RequestParam(required = false) RegistrationPeriodType type) {
    return ApiResponse.ok("Danh sách đợt đăng ký.", s.findAll(keyword, type));
  }

  @GetMapping("/page")
  public ApiResponse<PageResponse<RegistrationPeriodResponse>> page(
      @RequestParam(defaultValue = "") String keyword,
      @RequestParam(required = false) RegistrationPeriodType type,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok("Trang đợt đăng ký.", s.findPage(keyword, type, page, size));
  }

  @GetMapping("/{id}")
  public ApiResponse<RegistrationPeriodResponse> one(@PathVariable Long id) {
    return ApiResponse.ok("Chi tiết đợt đăng ký.", s.findById(id));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<RegistrationPeriodResponse>> create(
      @Valid @RequestBody RegistrationPeriodRequest r) {
    return ResponseEntity.status(201).body(ApiResponse.ok("Đã tạo đợt đăng ký.", s.create(r)));
  }

  @PutMapping("/{id}")
  public ApiResponse<RegistrationPeriodResponse> update(
      @PathVariable Long id, @Valid @RequestBody RegistrationPeriodRequest r) {
    return ApiResponse.ok("Đã cập nhật đợt đăng ký.", s.update(id, r));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable Long id) {
    s.delete(id);
    return ApiResponse.ok("Đã xóa đợt đăng ký.", null);
  }
}
