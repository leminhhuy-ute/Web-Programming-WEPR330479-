package vn.edu.hcmute.topicmanagement.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.edu.hcmute.topicmanagement.enums.TopicStatus;
import vn.edu.hcmute.topicmanagement.security.CustomUserDetails;
import vn.edu.hcmute.topicmanagement.service.StudentGroupService;
import vn.edu.hcmute.topicmanagement.service.TopicService;

@Controller
@RequestMapping("/student")
public class StudentWebController {

    private final StudentGroupService studentGroupService;
    private final TopicService topicService;

    public StudentWebController(StudentGroupService studentGroupService, TopicService topicService) {
        this.studentGroupService = studentGroupService;
        this.topicService = topicService;
    }

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long studentId = userDetails != null ? userDetails.getId() : 1L;

        model.addAttribute("currentUser", userDetails);
        model.addAttribute("workspace", studentGroupService.getWorkspace(studentId));
        model.addAttribute("availableTopics", topicService.filterTopics("", null, null, TopicStatus.APPROVED));
        model.addAttribute("availableStudents", studentGroupService.searchAvailableStudents(""));

        return "student/dashboard";
    }

    @GetMapping("/group")
    public String groupPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long studentId = userDetails != null ? userDetails.getId() : 1L;
        model.addAttribute("currentUser", userDetails);
        model.addAttribute("workspace", studentGroupService.getWorkspace(studentId));
        model.addAttribute("availableStudents", studentGroupService.searchAvailableStudents(""));
        return "student/group";
    }

    @GetMapping("/topics")
    public String topicsPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long studentId = userDetails != null ? userDetails.getId() : 1L;
        model.addAttribute("currentUser", userDetails);
        model.addAttribute("workspace", studentGroupService.getWorkspace(studentId));
        model.addAttribute("availableTopics", topicService.filterTopics("", null, null, TopicStatus.APPROVED));
        return "student/topics";
    }

    @GetMapping("/reports")
    public String reportsPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long studentId = userDetails != null ? userDetails.getId() : 1L;
        model.addAttribute("currentUser", userDetails);
        model.addAttribute("workspace", studentGroupService.getWorkspace(studentId));
        return "student/reports";
    }
}
