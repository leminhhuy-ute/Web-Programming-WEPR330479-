package topicmanagement.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import topicmanagement.dto.ApiResponse;
import topicmanagement.dto.response.DepartmentResponse;
import topicmanagement.dto.response.RegistrationPeriodResponse;
import topicmanagement.dto.response.UserResponse;
import topicmanagement.repository.DepartmentRepository;
import topicmanagement.repository.RegistrationPeriodRepository;
import topicmanagement.service.UserService;

@RestController
@RequestMapping("/api/common")
public class ApiCommonController {

    private final DepartmentRepository departmentRepository;
    private final RegistrationPeriodRepository periodRepository;
    private final UserService userService;

    @Autowired
    public ApiCommonController(
            DepartmentRepository departmentRepository,
            RegistrationPeriodRepository periodRepository,
            UserService userService) {
        this.departmentRepository = departmentRepository;
        this.periodRepository = periodRepository;
        this.userService = userService;
    }

    @GetMapping("/departments")
    public ApiResponse<List<DepartmentResponse>> getDepartments() {
        List<DepartmentResponse> list = departmentRepository.findAll()
                .stream().map(DepartmentResponse::new).collect(Collectors.toList());
        return ApiResponse.ok(list);
    }

    @GetMapping("/registration-periods")
    public ApiResponse<List<RegistrationPeriodResponse>> getRegistrationPeriods() {
        List<RegistrationPeriodResponse> list = periodRepository.findAll()
                .stream().map(RegistrationPeriodResponse::new).collect(Collectors.toList());
        return ApiResponse.ok(list);
    }

    @GetMapping("/lecturers")
    public ApiResponse<List<UserResponse>> getLecturers(@RequestParam(required = false) Long departmentId) {
        List<UserResponse> list = userService.getLecturersByDepartment(departmentId);
        return ApiResponse.ok(list);
    }
}
