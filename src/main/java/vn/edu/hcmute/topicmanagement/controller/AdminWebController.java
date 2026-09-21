package vn.edu.hcmute.topicmanagement.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.edu.hcmute.topicmanagement.enums.Role;
import vn.edu.hcmute.topicmanagement.enums.UserStatus;
import vn.edu.hcmute.topicmanagement.repository.DepartmentRepository;
import vn.edu.hcmute.topicmanagement.repository.RegistrationPeriodRepository;
import vn.edu.hcmute.topicmanagement.repository.TopicRepository;
import vn.edu.hcmute.topicmanagement.repository.UserRepository;
import vn.edu.hcmute.topicmanagement.security.CustomUserDetails;
import vn.edu.hcmute.topicmanagement.service.DepartmentService;
import vn.edu.hcmute.topicmanagement.service.RegistrationPeriodService;
import vn.edu.hcmute.topicmanagement.service.UserService;

@Controller
@RequestMapping("/admin")
public class AdminWebController {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final RegistrationPeriodRepository periodRepository;
    private final TopicRepository topicRepository;
    private final UserService userService;
    private final DepartmentService departmentService;
    private final RegistrationPeriodService periodService;

    public AdminWebController(UserRepository userRepository,
                              DepartmentRepository departmentRepository,
                              RegistrationPeriodRepository periodRepository,
                              TopicRepository topicRepository,
                              UserService userService,
                              DepartmentService departmentService,
                              RegistrationPeriodService periodService) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.periodRepository = periodRepository;
        this.topicRepository = topicRepository;
        this.userService = userService;
        this.departmentService = departmentService;
        this.periodService = periodService;
    }

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("currentUser", userDetails);
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalStudents", userRepository.countByRoleAndStatus(Role.STUDENT, UserStatus.ACTIVE));
        model.addAttribute("totalLecturers", userRepository.countByRoleAndStatus(Role.LECTURER, UserStatus.ACTIVE));
        model.addAttribute("totalDepartments", departmentRepository.count());
        model.addAttribute("totalPeriods", periodRepository.count());
        model.addAttribute("totalTopics", topicRepository.count());
        model.addAttribute("departments", departmentService.findAll(""));
        model.addAttribute("periods", periodService.findAll("", null));
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String usersPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("currentUser", userDetails);
        model.addAttribute("users", userService.findAll("", null, null));
        model.addAttribute("departments", departmentService.findAll(""));
        return "admin/users";
    }

    @GetMapping("/departments")
    public String departmentsPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("currentUser", userDetails);
        model.addAttribute("departments", departmentService.findAll(""));
        return "admin/departments";
    }

    @GetMapping("/registration-periods")
    public String periodsPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("currentUser", userDetails);
        model.addAttribute("periods", periodService.findAll("", null));
        return "admin/periods";
    }
}
