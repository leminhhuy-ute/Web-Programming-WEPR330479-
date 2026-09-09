package vn.edu.hcmute.topicmanagement.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.hcmute.topicmanagement.repository.*;
import vn.edu.hcmute.topicmanagement.service.*;
import java.math.BigDecimal;

@Controller
@RequestMapping("/grading")
public class GradingController {
    private final TopicRepository topics; private final CouncilMemberRepository members;
    private final EvaluationRepository evaluations; private final GradingService service;
    public GradingController(TopicRepository topics, CouncilMemberRepository members, EvaluationRepository evaluations, GradingService service) {
        this.topics = topics; this.members = members; this.evaluations = evaluations; this.service = service;
    }
    @GetMapping
    String list(Model model) { model.addAttribute("topics", topics.findAllByOrderByCodeAsc().stream().filter(t -> t.getCouncil() != null).toList()); return "grading/list"; }
    @GetMapping("/{topicId}")
    String detail(@PathVariable Long topicId, Model model) {
        var topic = topics.findDetailById(topicId).orElseThrow();
        model.addAttribute("topic", topic);
        model.addAttribute("members", members.findByCouncilId(topic.getCouncil().getId()));
        model.addAttribute("evaluations", evaluations.findByTopicIdOrderByEvaluatorFullNameAsc(topicId));
        return "grading/detail";
    }
    @PostMapping("/{topicId}")
    String grade(@PathVariable Long topicId, @RequestParam Long evaluatorId, @RequestParam BigDecimal score,
                 @RequestParam String comment, RedirectAttributes redirect) {
        try { service.grade(topicId, evaluatorId, score, comment); redirect.addFlashAttribute("success", "Đã lưu điểm và nhận xét."); }
        catch (BusinessException e) { redirect.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/grading/" + topicId;
    }
}
