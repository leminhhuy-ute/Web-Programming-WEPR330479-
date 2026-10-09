package topicmanagement.notification;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class EmailDeliveryService {
    private final EmailNotificationRepository notifications;
    private final MailSender sender;
    private final boolean enabled;
    private final String from;
    public EmailDeliveryService(EmailNotificationRepository notifications,MailSender sender,
            @Value("${app.mail.enabled:false}") boolean enabled,@Value("${app.mail.from}") String from) {
        this.notifications=notifications;this.sender=sender;this.enabled=enabled;this.from=from;
    }
    @Transactional public void deliver(Long id) {
        if(!enabled) return;
        var email=notifications.lockById(id).orElse(null);
        var now=LocalDateTime.now();
        if(email==null || email.status!=EmailNotification.Status.PENDING || email.nextAttemptAt.isAfter(now)) return;
        var message=new SimpleMailMessage();message.setFrom(from);message.setTo(email.recipient);
        message.setSubject(email.subject);message.setText(email.body);email.attempts++;
        try {
            sender.send(message);email.status=EmailNotification.Status.SENT;email.sentAt=now;email.lastError=null;
        } catch(MailException ex) {
            email.status=email.attempts>=5?EmailNotification.Status.FAILED:EmailNotification.Status.PENDING;
            email.nextAttemptAt=now.plusMinutes(1L << (email.attempts-1));
            email.lastError="Gửi email thất bại ("+ex.getClass().getSimpleName()+"). Kiểm tra cấu hình SMTP.";
        }
    }
    @Transactional public void retry(Long id) {
        var email=notifications.lockById(id).orElseThrow(()->new IllegalArgumentException("Không tìm thấy email."));
        if(email.status!=EmailNotification.Status.FAILED) throw new IllegalArgumentException("Chỉ gửi lại email đã thất bại.");
        email.status=EmailNotification.Status.PENDING;email.attempts=0;email.nextAttemptAt=LocalDateTime.now();email.lastError=null;
    }
}
