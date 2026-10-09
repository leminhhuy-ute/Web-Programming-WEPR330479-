package topicmanagement.notification;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import topicmanagement.dto.ApiResponse;
import topicmanagement.dto.response.PageResponse;
@org.springframework.security.access.prepost.PreAuthorize("hasRole('DEAN')")
@RestController
@RequestMapping("/api/admin/email-notifications")
public class EmailNotificationController {
    private final EmailNotificationRepository notifications;
    private final EmailDeliveryService delivery;
    private final boolean enabled;
    public record EmailView(Long id,String student,String group,String recipient,String status,int attempts,
        LocalDateTime createdAt,LocalDateTime sentAt,LocalDateTime nextAttemptAt,String lastError) {}
    public record State(boolean enabled,PageResponse<EmailView> page) {}
    public EmailNotificationController(EmailNotificationRepository notifications,EmailDeliveryService delivery,
            @Value("${app.mail.enabled:false}") boolean enabled) {
        this.notifications=notifications;this.delivery=delivery;this.enabled=enabled;
    }
    @GetMapping @Transactional(readOnly=true)
    public ApiResponse<State> page(@RequestParam(required=false) EmailNotification.Status status,
            @RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {
        var pageable=PageRequest.of(Math.max(page,0),Math.min(100,Math.max(1,size)),Sort.by("id").descending());
        var data=status==null?notifications.findAll(pageable):notifications.findByStatus(status,pageable);
        var views=data.map(n->new EmailView(n.id,n.student.getFullName(),n.defense.group.getGroupName(),
            n.recipient,n.status.name(),n.attempts,n.createdAt,n.sentAt,n.nextAttemptAt,n.lastError));
        return ApiResponse.ok("Lịch sử thông báo điểm.",new State(enabled,PageResponse.from(views)));
    }
    @PostMapping("/{id}/retry") public ApiResponse<Void> retry(@PathVariable Long id) {
        delivery.retry(id);return ApiResponse.ok("Đã đưa email vào hàng đợi gửi lại.",null);
    }
}
