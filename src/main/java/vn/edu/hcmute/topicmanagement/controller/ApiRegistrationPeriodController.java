package vn.edu.hcmute.topicmanagement.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmute.topicmanagement.dto.ApiResponse;
import vn.edu.hcmute.topicmanagement.dto.request.RegistrationPeriodRequest;
import vn.edu.hcmute.topicmanagement.dto.response.RegistrationPeriodResponse;
import vn.edu.hcmute.topicmanagement.enums.RegistrationPeriodType;
import vn.edu.hcmute.topicmanagement.service.RegistrationPeriodService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/registration-periods")
public class ApiRegistrationPeriodController {

    private final RegistrationPeriodService periodService;

    public ApiRegistrationPeriodController(RegistrationPeriodService periodService) {
        this.periodService = periodService;
    }

    @GetMapping
    public ApiResponse<List<RegistrationPeriodResponse>> list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) RegistrationPeriodType type) {
        return ApiResponse.ok("Danh sách đợt đăng ký.", periodService.findAll(keyword, type));
    }

    @GetMapping("/{id}")
    public ApiResponse<RegistrationPeriodResponse> one(@PathVariable Long id) {
        return ApiResponse.ok("Chi tiết đợt đăng ký.", periodService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RegistrationPeriodResponse>> create(@Valid @RequestBody RegistrationPeriodRequest r) {
        return ResponseEntity.status(201).body(ApiResponse.ok("Đã tạo đợt đăng ký thành công.", periodService.create(r)));
    }

    @PutMapping("/{id}")
    public ApiResponse<RegistrationPeriodResponse> update(
            @PathVariable Long id, @Valid @RequestBody RegistrationPeriodRequest r) {
        return ApiResponse.ok("Đã cập nhật đợt đăng ký thành công.", periodService.update(id, r));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        periodService.delete(id);
        return ApiResponse.ok("Đã xóa đợt đăng ký thành công.", null);
    }
}
