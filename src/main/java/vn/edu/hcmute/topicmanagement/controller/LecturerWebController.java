package vn.edu.hcmute.topicmanagement.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.edu.hcmute.topicmanagement.entity.User;
import vn.edu.hcmute.topicmanagement.enums.Role;
import vn.edu.hcmute.topicmanagement.enums.TopicStatus;
import vn.edu.hcmute.topicmanagement.repository.DepartmentRepository;
import vn.edu.hcmute.topicmanagement.repository.RegistrationPeriodRepository;
import vn.edu.hcmute.topicmanagement.repository.TopicRepository;
import vn.edu.hcmute.topicmanagement.repository.UserRepository;
import vn.edu.hcmute.topicmanagement.security.CustomUserDetails;
import vn.edu.hcmute.topicmanagement.service.DepartmentTopicService;
import vn.edu.hcmute.topicmanagement.service.TopicService;

import java.util.List;

@Controller
@RequestMapping("/lecturer")
public class LecturerWebController {

    private final TopicService topicService;
    private final DepartmentTopicService departmentTopicService;
    private final TopicRepository topicRepository;
    private final DepartmentRepository departmentRepository;
    private final RegistrationPeriodRepository periodRepository;
    private final UserRepository userRepository;

    public LecturerWebController(TopicService topicService,
                                 DepartmentTopicService departmentTopicService,
                                 TopicRepository topicRepository,
                                 DepartmentRepository departmentRepository,
                                 RegistrationPeriodRepository periodRepository,
                                 UserRepository userRepository) {
        this.topicService = topicService;
        this.departmentTopicService = departmentTopicService;
        this.topicRepository = topicRepository;
        this.departmentRepository = departmentRepository;
        this.periodRepository = periodRepository;
        this.userRepository = userRepository;
    }

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long userId = userDetails != null ? userDetails.getId() : 1L;
        Long deptId = userDetails != null && userDetails.getDepartment() != null ? userDetails.getDepartment().getId() : 1L;

        model.addAttribute("currentUser", userDetails);
        model.addAttribute("myTopics", topicService.getMyTopics(userId));
        model.addAttribute("departmentTopics", departmentTopicService.getDepartmentTopics(deptId, null));
        model.addAttribute("pendingRegistrations", departmentTopicService.getPendingRegistrationsForAdvisor(userId));
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("periods", periodRepository.findAll());
        model.addAttribute("lecturers", userRepository.findByRole(Role.LECTURER));

        return "lecturer/dashboard";
    }

    @GetMapping("/topics")
    public String myTopicsPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long userId = userDetails != null ? userDetails.getId() : 1L;
        model.addAttribute("currentUser", userDetails);
        model.addAttribute("myTopics", topicService.getMyTopics(userId));
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("periods", periodRepository.findAll());
        return "lecturer/topics";
    }

    @GetMapping("/approval")
    public String approvalPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long deptId = userDetails != null && userDetails.getDepartment() != null ? userDetails.getDepartment().getId() : 1L;
        model.addAttribute("currentUser", userDetails);
        model.addAttribute("departmentTopics", departmentTopicService.getDepartmentTopics(deptId, TopicStatus.PENDING));
        model.addAttribute("lecturers", userRepository.findByRole(Role.LECTURER));
        return "lecturer/approval";
    }

    @GetMapping("/student-groups")
    public String studentGroupsPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long userId = userDetails != null ? userDetails.getId() : 1L;
        model.addAttribute("currentUser", userDetails);
        model.addAttribute("registrations", departmentTopicService.getPendingRegistrationsForAdvisor(userId));
        return "lecturer/student-groups";
    }
}
