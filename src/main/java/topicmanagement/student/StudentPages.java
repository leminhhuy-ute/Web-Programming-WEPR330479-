package topicmanagement.student;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class StudentPages {
    @GetMapping("/student") public String student() { return "student"; }
    @GetMapping("/login") public String login() { return "redirect:/login.html"; }
}
