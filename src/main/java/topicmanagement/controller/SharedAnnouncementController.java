package topicmanagement.controller;
import java.util.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import topicmanagement.enums.*;
import topicmanagement.repository.AnnouncementRepository;
import topicmanagement.security.CurrentAccount;
@RestController
public class SharedAnnouncementController {
    private final AnnouncementRepository announcements;
    private final CurrentAccount accounts;
    public SharedAnnouncementController(AnnouncementRepository announcements,CurrentAccount accounts) {
        this.announcements=announcements;this.accounts=accounts;
    }
    @GetMapping({"/api/student/announcements","/api/lecturer/announcements"})
    @Transactional(readOnly=true)
    public Object list() {
        var role=accounts.user().getRole();
        var audience=role==Role.STUDENT?AnnouncementAudience.STUDENT:AnnouncementAudience.LECTURER;
        return announcements.search("",null).stream()
            .filter(a->a.getAudience()==AnnouncementAudience.ALL||a.getAudience()==audience)
            .map(a->Map.of("id",a.getId(),"title",a.getTitle(),"content",a.getContent())).toList();
    }
}
