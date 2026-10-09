package topicmanagement.notification;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Component
public class EmailWorker {
    private final EmailNotificationRepository notifications;
    private final EmailDeliveryService delivery;
    private final boolean enabled;
    public EmailWorker(EmailNotificationRepository notifications,EmailDeliveryService delivery,
            @Value("${app.mail.enabled:false}") boolean enabled) {
        this.notifications=notifications;this.delivery=delivery;this.enabled=enabled;
    }
    @Scheduled(initialDelayString="${app.mail.initial-delay-ms:10000}",fixedDelayString="${app.mail.poll-ms:30000}")
    public void dispatch() {
        if(!enabled) return;
        for(var id:notifications.due(LocalDateTime.now(),PageRequest.of(0,20))) delivery.deliver(id);
    }
}
