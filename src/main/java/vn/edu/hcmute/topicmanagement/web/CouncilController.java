package vn.edu.hcmute.topicmanagement.web;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.hcmute.topicmanagement.domain.*;
import vn.edu.hcmute.topicmanagement.repository.*;
import vn.edu.hcmute.topicmanagement.service.*;

@Controller
@RequestMapping("/councils")
public class CouncilController {
    private final CouncilRepository councils;
    private final LecturerRepository lecturers;
    private final TopicRepository topics;
    private final CouncilService service;
    public CouncilController(CouncilRepository councils, LecturerRepository lecturers, TopicRepository topics, CouncilService service) {
        this.councils = councils; this.lecturers = lecturers; this.topics = topics; this.service = service;
    }
    @ModelAttribute("roles") CouncilRole[] roles() { return CouncilRole.values(); }
    @GetMapping
    String list(Model model) { model.addAttribute("councils", councils.findAllByOrderByDefenseDateDesc()); return "councils/list"; }
    @GetMapping("/new")
    String create(Model model) { model.addAttribute("council", new Council()); return "councils/form"; }
    @PostMapping("/save")
    String save(@Valid @ModelAttribute Council council, BindingResult result, RedirectAttributes redirect) {
        if (result.hasErrors()) return "councils/form";
        Council saved = service.save(council); redirect.addFlashAttribute("success", "Đã tạo hội đồng."); return "redirect:/councils/" + saved.getId();
    }
    @GetMapping("/{id}")
    String detail(@PathVariable Long id, Model model) {
        model.addAttribute("council", councils.findWithMembersById(id).orElseThrow());
        model.addAttribute("lecturers", lecturers.findByActiveTrueOrderByFullNameAsc());
        model.addAttribute("topics", topics.findAllByOrderByCodeAsc().stream().filter(t -> t.getCouncil() != null && t.getCouncil().getId().equals(id)).toList());
        return "councils/detail";
    }
    @PostMapping("/{id}/members")
    String addMember(@PathVariable Long id, @RequestParam Long lecturerId, @RequestParam CouncilRole role, RedirectAttributes redirect) {
        try { service.addMember(id, lecturerId, role); redirect.addFlashAttribute("success", "Đã thêm thành viên hội đồng."); }
        catch (BusinessException e) { redirect.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/councils/" + id;
    }
    @PostMapping("/{id}/members/{memberId}/delete")
    String removeMember(@PathVariable Long id, @PathVariable Long memberId, RedirectAttributes redirect) {
        try { service.removeMember(id, memberId); redirect.addFlashAttribute("success", "Đã xóa thành viên."); }
        catch (BusinessException e) { redirect.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/councils/" + id;
    }
    @PostMapping("/{id}/ready")
    String ready(@PathVariable Long id, RedirectAttributes redirect) {
        try { service.markReady(id); redirect.addFlashAttribute("success", "Hội đồng đã sẵn sàng nhận đề tài."); }
        catch (BusinessException e) { redirect.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/councils/" + id;
    }
}
