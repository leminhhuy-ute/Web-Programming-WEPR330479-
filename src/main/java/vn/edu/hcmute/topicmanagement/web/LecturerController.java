package vn.edu.hcmute.topicmanagement.web;

import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.hcmute.topicmanagement.domain.Department;
import vn.edu.hcmute.topicmanagement.domain.Lecturer;
import vn.edu.hcmute.topicmanagement.repository.LecturerRepository;

@Controller
@RequestMapping("/lecturers")
public class LecturerController {
    private final LecturerRepository repository;
    public LecturerController(LecturerRepository repository) { this.repository = repository; }

    @ModelAttribute("departments") Department[] departments() { return Department.values(); }
    @GetMapping
    String list(Model model) { model.addAttribute("lecturers", repository.findAllByOrderByFullNameAsc()); return "lecturers/list"; }
    @GetMapping("/new")
    String create(Model model) { model.addAttribute("lecturer", new Lecturer()); return "lecturers/form"; }
    @GetMapping("/{id}/edit")
    String edit(@PathVariable Long id, Model model) {
        model.addAttribute("lecturer", repository.findById(id).orElseThrow()); return "lecturers/form";
    }
    @PostMapping("/save")
    String save(@Valid @ModelAttribute Lecturer lecturer, BindingResult result, RedirectAttributes redirect) {
        Long excludedId = lecturer.getId() == null ? -1L : lecturer.getId();
        if (repository.existsByCodeIgnoreCaseAndIdNot(lecturer.getCode(), excludedId)) result.rejectValue("code", "duplicate", "Mã giảng viên đã tồn tại.");
        if (repository.existsByEmailIgnoreCaseAndIdNot(lecturer.getEmail(), excludedId)) result.rejectValue("email", "duplicate", "Email đã tồn tại.");
        if (result.hasErrors()) return "lecturers/form";
        repository.save(lecturer); redirect.addFlashAttribute("success", "Đã lưu thông tin giảng viên."); return "redirect:/lecturers";
    }
    @PostMapping("/{id}/toggle")
    String toggle(@PathVariable Long id, RedirectAttributes redirect) {
        Lecturer lecturer = repository.findById(id).orElseThrow(); lecturer.setActive(!lecturer.isActive()); repository.save(lecturer);
        redirect.addFlashAttribute("success", "Đã cập nhật trạng thái giảng viên."); return "redirect:/lecturers";
    }
}
