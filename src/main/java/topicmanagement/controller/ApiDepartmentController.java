package topicmanagement.controller;

import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import topicmanagement.dto.*;
import topicmanagement.dto.request.DepartmentRequest;
import topicmanagement.dto.response.DepartmentResponse;
import topicmanagement.service.DepartmentServiceV2;

@RestController
@RequestMapping("/api/admin/departments")
public class ApiDepartmentController {
  private final DepartmentServiceV2 s;

  public ApiDepartmentController(DepartmentServiceV2 s) {
    this.s = s;
  }

  @GetMapping
  public ApiResponse<List<DepartmentResponse>> list(
      @RequestParam(defaultValue = "") String keyword) {
    return ApiResponse.ok("Danh sách bộ môn.", s.findAll(keyword));
  }

  @GetMapping("/{id}")
  public ApiResponse<DepartmentResponse> one(@PathVariable Long id) {
    return ApiResponse.ok("Chi tiết bộ môn.", s.findById(id));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<DepartmentResponse>> create(
      @Valid @RequestBody DepartmentRequest r) {
    return ResponseEntity.status(201).body(ApiResponse.ok("Đã tạo bộ môn.", s.create(r)));
  }

  @PutMapping("/{id}")
  public ApiResponse<DepartmentResponse> update(
      @PathVariable Long id, @Valid @RequestBody DepartmentRequest r) {
    return ApiResponse.ok("Đã cập nhật bộ môn.", s.update(id, r));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable Long id) {
    s.delete(id);
    return ApiResponse.ok("Đã xóa bộ môn.", null);
  }
}
