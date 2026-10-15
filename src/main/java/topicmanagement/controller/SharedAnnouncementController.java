package topicmanagement.controller;
import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import topicmanagement.repository.AnnouncementRepository;
import topicmanagement.security.CurrentAccount;
import topicmanagement.service.NotificationService;
@RestController
public class SharedAnnouncementController {
    public record AnnouncementItem(Long id, String title, String content, LocalDateTime createdAt) {}
    private final AnnouncementRepository announcements;
    private final CurrentAccount accounts;
    private final NotificationService notifications;
    public SharedAnnouncementController(AnnouncementRepository announcements,CurrentAccount accounts,
            NotificationService notifications) {
        this.announcements=announcements;this.accounts=accounts;this.notifications=notifications;
    }
    @GetMapping({"/api/student/announcements","/api/lecturer/announcements"})
    @Transactional(readOnly=true)
    public Object list() {
        var recipient=accounts.user();
        return announcements.search("",null).stream()
            .filter(a->notifications.visibleTo(a,recipient))
            .map(a->new AnnouncementItem(a.getId(),a.getTitle(),a.getContent(),a.getCreatedAt())).toList();
    }
}
