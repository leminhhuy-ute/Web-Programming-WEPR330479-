package vn.edu.hcmute.topicmanagement.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.edu.hcmute.topicmanagement.domain.TopicStatus;
import vn.edu.hcmute.topicmanagement.repository.*;

@Controller
public class HomeController {
    private final LecturerRepository lecturers;
    private final CouncilRepository councils;
    private final TopicRepository topics;

    public HomeController(LecturerRepository lecturers, CouncilRepository councils, TopicRepository topics) {
        this.lecturers = lecturers; this.councils = councils; this.topics = topics;
    }
    @GetMapping("/") public String home() { return "redirect:/dashboard"; }
    @GetMapping("/login") public String login() { return "login"; }
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("lecturerCount", lecturers.count());
        model.addAttribute("councilCount", councils.count());
        model.addAttribute("waitingCount", topics.countByStatus(TopicStatus.WAITING_ASSIGNMENT));
        model.addAttribute("gradedCount", topics.countByStatus(TopicStatus.GRADED));
        model.addAttribute("topics", topics.findAllByOrderByCodeAsc());
        return "dashboard";
    }
}
