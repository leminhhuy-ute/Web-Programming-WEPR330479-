package vn.edu.hcmute.topicmanagement.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.edu.hcmute.topicmanagement.enums.Role;
import vn.edu.hcmute.topicmanagement.enums.TopicStatus;
import vn.edu.hcmute.topicmanagement.repository.DepartmentRepository;
import vn.edu.hcmute.topicmanagement.repository.RegistrationPeriodRepository;
import vn.edu.hcmute.topicmanagement.repository.TopicRepository;
import vn.edu.hcmute.topicmanagement.repository.UserRepository;
import vn.edu.hcmute.topicmanagement.security.CustomUserDetails;
import vn.edu.hcmute.topicmanagement.service.CouncilService;
import vn.edu.hcmute.topicmanagement.service.GradingService;

@Controller
@RequestMapping("/council")
public class CouncilWebController {

    private final CouncilService councilService;
    private final GradingService gradingService;
    private final TopicRepository topicRepository;
    private final DepartmentRepository departmentRepository;
    private final RegistrationPeriodRepository periodRepository;
    private final UserRepository userRepository;

    public CouncilWebController(CouncilService councilService,
                                GradingService gradingService,
                                TopicRepository topicRepository,
                                DepartmentRepository departmentRepository,
                                RegistrationPeriodRepository periodRepository,
                                UserRepository userRepository) {
        this.councilService = councilService;
        this.gradingService = gradingService;
        this.topicRepository = topicRepository;
        this.departmentRepository = departmentRepository;
        this.periodRepository = periodRepository;
        this.userRepository = userRepository;
    }

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long deptId = userDetails != null && userDetails.getDepartment() != null ? userDetails.getDepartment().getId() : null;

        model.addAttribute("currentUser", userDetails);
        model.addAttribute("councils", councilService.findAll(deptId, null));
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("periods", periodRepository.findAll());
        model.addAttribute("lecturers", userRepository.findByRole(Role.LECTURER));
        model.addAttribute("approvedTopics", topicRepository.findByStatus(TopicStatus.APPROVED));

        return "council/dashboard";
    }

    @GetMapping("/list")
    public String listPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long deptId = userDetails != null && userDetails.getDepartment() != null ? userDetails.getDepartment().getId() : null;

        model.addAttribute("currentUser", userDetails);
        model.addAttribute("councils", councilService.findAll(deptId, null));
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("periods", periodRepository.findAll());
        model.addAttribute("lecturers", userRepository.findByRole(Role.LECTURER));

        return "council/list";
    }

    @GetMapping("/grading")
    public String gradingPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("currentUser", userDetails);
        model.addAttribute("approvedTopics", topicRepository.findByStatus(TopicStatus.APPROVED));
        model.addAttribute("councils", councilService.findAll(null, null));

        return "council/grading";
    }
}
