package vn.edu.hcmute.topicmanagement.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmute.topicmanagement.dto.ApiResponse;
import vn.edu.hcmute.topicmanagement.dto.request.DepartmentRequest;
import vn.edu.hcmute.topicmanagement.dto.response.DepartmentResponse;
import vn.edu.hcmute.topicmanagement.service.DepartmentService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/departments")
public class ApiDepartmentController {

    private final DepartmentService departmentService;

    public ApiDepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public ApiResponse<List<DepartmentResponse>> list(@RequestParam(defaultValue = "") String keyword) {
        return ApiResponse.ok("Danh sách bộ môn.", departmentService.findAll(keyword));
    }

    @GetMapping("/{id}")
    public ApiResponse<DepartmentResponse> one(@PathVariable Long id) {
        return ApiResponse.ok("Chi tiết bộ môn.", departmentService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentResponse>> create(@Valid @RequestBody DepartmentRequest r) {
        return ResponseEntity.status(201).body(ApiResponse.ok("Đã tạo bộ môn thành công.", departmentService.create(r)));
    }

    @PutMapping("/{id}")
    public ApiResponse<DepartmentResponse> update(@PathVariable Long id, @Valid @RequestBody DepartmentRequest r) {
        return ApiResponse.ok("Đã cập nhật bộ môn thành công.", departmentService.update(id, r));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ApiResponse.ok("Đã xóa bộ môn thành công.", null);
    }
}
