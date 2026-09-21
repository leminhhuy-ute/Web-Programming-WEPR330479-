package vn.edu.hcmute.topicmanagement.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmute.topicmanagement.dto.ApiResponse;
import vn.edu.hcmute.topicmanagement.dto.request.UserRequest;
import vn.edu.hcmute.topicmanagement.dto.request.UserStatusRequest;
import vn.edu.hcmute.topicmanagement.dto.response.UserResponse;
import vn.edu.hcmute.topicmanagement.enums.Role;
import vn.edu.hcmute.topicmanagement.security.CustomUserDetails;
import vn.edu.hcmute.topicmanagement.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class ApiUserController {

    private final UserService userService;

    public ApiUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Long departmentId) {
        return ApiResponse.ok("Danh sách tài khoản.", userService.findAll(keyword, role, departmentId));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> one(@PathVariable Long id) {
        return ApiResponse.ok("Chi tiết tài khoản.", userService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody UserRequest r) {
        return ResponseEntity.status(201).body(ApiResponse.ok("Đã tạo tài khoản thành công.", userService.create(r)));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserRequest r) {
        return ApiResponse.ok("Đã cập nhật tài khoản thành công.", userService.update(id, r));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<UserResponse> status(@PathVariable Long id, @Valid @RequestBody UserStatusRequest r) {
        return ApiResponse.ok("Đã cập nhật trạng thái tài khoản.", userService.setStatus(id, r.status()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long currentUserId = userDetails != null ? userDetails.getId() : null;
        userService.delete(id, currentUserId);
        return ApiResponse.ok("Đã xóa tài khoản thành công.", null);
    }
}
