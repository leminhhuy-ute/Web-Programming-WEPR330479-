package topicmanagement.notification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import topicmanagement.council.Defense;
import topicmanagement.repository.GroupMemberRepository;
@Service
@Transactional
public class ResultNotificationService {
    private final EmailNotificationRepository notifications;
    private final GroupMemberRepository members;
    public ResultNotificationService(EmailNotificationRepository notifications,GroupMemberRepository members) {
        this.notifications=notifications;this.members=members;
    }
    // Outbox rows commit with publication. SMTP runs later in a separate transaction.
    public void enqueue(Defense defense) {
        if (!defense.published || !defense.finalized || defense.finalScore==null)
            throw new IllegalArgumentException("Kết quả chưa được công bố.");
        var topic=defense.topic!=null?defense.topic:defense.group.getTopic();
        for(var membership:members.findByGroupId(defense.group.getId())) {
            var student=membership.getStudent();
            if(notifications.existsByDefenseIdAndStudentId(defense.id,student.getId())) continue;
            var email=new EmailNotification();email.defense=defense;email.student=student;
            email.recipient=student.getEmail();email.subject="Kết quả đề tài đã được công bố";
            email.body="Chào "+student.getFullName()+",\n\nKết quả đề tài của bạn đã được công bố."
                +"\nĐợt: "+topic.getPeriod().getName()+"\nNhóm: "+defense.group.getGroupName()
                +"\nĐề tài: "+topic.getTitle()+"\nĐiểm cuối cùng: "+defense.finalScore.toPlainString()
                +"\n\nVui lòng đăng nhập hệ thống quản lý đề tài để xem điểm thành phần và nhận xét."
                +"\nKhoa Công nghệ Thông tin";
            notifications.save(email);
        }
    }
}
