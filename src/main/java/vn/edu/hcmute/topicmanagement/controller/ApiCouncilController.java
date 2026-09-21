package vn.edu.hcmute.topicmanagement.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmute.topicmanagement.dto.ApiResponse;
import vn.edu.hcmute.topicmanagement.dto.request.CouncilMemberRequest;
import vn.edu.hcmute.topicmanagement.dto.request.CouncilRequest;
import vn.edu.hcmute.topicmanagement.dto.response.CouncilMemberResponse;
import vn.edu.hcmute.topicmanagement.dto.response.CouncilResponse;
import vn.edu.hcmute.topicmanagement.enums.CouncilStatus;
import vn.edu.hcmute.topicmanagement.service.CouncilService;

import java.util.List;

@RestController
@RequestMapping("/api/council")
public class ApiCouncilController {

    private final CouncilService councilService;

    public ApiCouncilController(CouncilService councilService) {
        this.councilService = councilService;
    }

    @GetMapping
    public ApiResponse<List<CouncilResponse>> list(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) CouncilStatus status) {
        return ApiResponse.ok("Danh sách hội đồng bảo vệ.", councilService.findAll(departmentId, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<CouncilResponse> one(@PathVariable Long id) {
        return ApiResponse.ok("Chi tiết hội đồng bảo vệ.", councilService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CouncilResponse>> create(@Valid @RequestBody CouncilRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.ok("Đã tạo hội đồng bảo vệ thành công.", councilService.createCouncil(request)));
    }

    @PutMapping("/{id}")
    public ApiResponse<CouncilResponse> update(@PathVariable Long id, @Valid @RequestBody CouncilRequest request) {
        return ApiResponse.ok("Đã cập nhật thông tin hội đồng thành công.", councilService.updateCouncil(id, request));
    }

    @PostMapping("/{id}/members")
    public ApiResponse<CouncilMemberResponse> addMember(
            @PathVariable Long id,
            @Valid @RequestBody CouncilMemberRequest request) {
        return ApiResponse.ok("Đã thêm thành viên vào hội đồng thành công.", councilService.addMember(id, request));
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public ApiResponse<Void> removeMember(@PathVariable Long id, @PathVariable Long memberId) {
        councilService.removeMember(id, memberId);
        return ApiResponse.ok("Đã xóa thành viên khỏi hội đồng.", null);
    }

    @PutMapping("/{id}/ready")
    public ApiResponse<CouncilResponse> markReady(@PathVariable Long id) {
        return ApiResponse.ok("Hội đồng đã kiểm tra hợp lệ và sẵn sàng bảo vệ.", councilService.markReady(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        councilService.deleteCouncil(id);
        return ApiResponse.ok("Đã xóa hội đồng thành công.", null);
    }
}
