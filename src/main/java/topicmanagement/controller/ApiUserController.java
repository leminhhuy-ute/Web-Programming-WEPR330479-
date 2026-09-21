package topicmanagement.controller;

import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import topicmanagement.dto.*;
import topicmanagement.dto.request.*;
import topicmanagement.dto.response.UserResponse;
import topicmanagement.enums.Role;
import topicmanagement.security.CurrentUser;
import topicmanagement.service.UserServiceV2;

@RestController
@RequestMapping("/api/admin/users")
public class ApiUserController {
  private final UserServiceV2 users;

  public ApiUserController(UserServiceV2 s) {
    users = s;
  }

  @GetMapping
  public ApiResponse<List<UserResponse>> list(
      @RequestParam(defaultValue = "") String keyword,
      @RequestParam(required = false) Role role,
      @RequestParam(required = false) Long departmentId) {
    return ApiResponse.ok("Danh sách tài khoản.", users.findAll(keyword, role, departmentId));
  }

  @GetMapping("/{id}")
  public ApiResponse<UserResponse> one(@PathVariable Long id) {
    return ApiResponse.ok("Chi tiết tài khoản.", users.findById(id));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody UserRequest r) {
    return ResponseEntity.status(201).body(ApiResponse.ok("Đã tạo tài khoản.", users.create(r)));
  }

  @PutMapping("/{id}")
  public ApiResponse<UserResponse> update(
      @PathVariable Long id, @Valid @RequestBody UserRequest r) {
    return ApiResponse.ok("Đã cập nhật tài khoản.", users.update(id, r));
  }

  @PatchMapping("/{id}/status")
  public ApiResponse<UserResponse> status(
      @PathVariable Long id, @Valid @RequestBody UserStatusRequest r) {
    return ApiResponse.ok("Đã cập nhật trạng thái.", users.setStatus(id, r.status()));
  }
  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(
      @PathVariable Long id, @AuthenticationPrincipal CurrentUser currentUser) {
    users.delete(id, currentUser.id());
    return ApiResponse.ok("Đã xóa tài khoản.", null);
  }
}
