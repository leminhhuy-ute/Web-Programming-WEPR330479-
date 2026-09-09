package vn.edu.hcmute.topicmanagement.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.hcmute.topicmanagement.domain.*;
import vn.edu.hcmute.topicmanagement.repository.*;
import vn.edu.hcmute.topicmanagement.service.*;

@Controller
@RequestMapping("/assignments")
public class AssignmentController {
    private final TopicRepository topics; private final CouncilRepository councils;
    private final LecturerRepository lecturers; private final GradingService service;
    public AssignmentController(TopicRepository topics, CouncilRepository councils, LecturerRepository lecturers, GradingService service) {
        this.topics = topics; this.councils = councils; this.lecturers = lecturers; this.service = service;
    }
    @GetMapping
    String page(Model model) {
        model.addAttribute("topics", topics.findAllByOrderByCodeAsc());
        model.addAttribute("councils", councils.findAllByOrderByDefenseDateDesc().stream().filter(c -> c.getStatus() == CouncilStatus.READY).toList());
        model.addAttribute("lecturers", lecturers.findByActiveTrueOrderByFullNameAsc());
        return "assignments/list";
    }
    @PostMapping("/{topicId}")
    String assign(@PathVariable Long topicId, @RequestParam Long councilId, @RequestParam Long reviewerId, RedirectAttributes redirect) {
        try { service.assign(topicId, councilId, reviewerId); redirect.addFlashAttribute("success", "Đã phân công hội đồng và giảng viên phản biện."); }
        catch (BusinessException e) { redirect.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/assignments";
    }
}
