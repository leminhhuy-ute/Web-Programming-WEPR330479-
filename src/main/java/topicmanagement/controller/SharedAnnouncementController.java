package topicmanagement.controller;
import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.*;
import topicmanagement.security.CurrentAccount;
import topicmanagement.service.NotificationService;
@RestController
public class SharedAnnouncementController {
    public record AnnouncementItem(Long id, String title, String content, LocalDateTime createdAt) {}
    private final CurrentAccount accounts;
    private final NotificationService notifications;
    public SharedAnnouncementController(CurrentAccount accounts,NotificationService notifications) {
        this.accounts=accounts;this.notifications=notifications;
    }
    @GetMapping({"/api/student/announcements","/api/lecturer/announcements"})
    public Object list() {
        return notifications.visibleAnnouncements(accounts.user()).stream()
            .map(a->new AnnouncementItem(a.getId(),a.getTitle(),a.getContent(),a.getCreatedAt())).toList();
    }
}
