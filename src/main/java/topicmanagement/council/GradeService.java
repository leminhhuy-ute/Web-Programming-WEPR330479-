package topicmanagement.council;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import topicmanagement.entity.Topic;
import topicmanagement.entity.User;
import topicmanagement.enums.Role;
import topicmanagement.security.CurrentAccount;
import topicmanagement.service.AuditLogService;
import topicmanagement.service.TopicPolicy;

@Service
@Transactional
public class GradeService {
    private final GradeRepository grades;
    private final CurrentAccount accounts;
    private final EntityManager entityManager;
    private final AuditLogService audit;
    private final topicmanagement.notification.ResultNotificationService notifications;

    public GradeService(GradeRepository grades, CurrentAccount accounts, EntityManager entityManager,
            AuditLogService audit, topicmanagement.notification.ResultNotificationService notifications) {
        this.grades = grades;
        this.accounts = accounts;
        this.entityManager = entityManager;
        this.audit = audit;
        this.notifications = notifications;
    }

    public void grade(Long id, BigDecimal score, String comment) {
        User user = staff();
        Defense defense = locked(id);
        if (!member(defense.council, user) || advisor(defense.group.getTopic(), user.getId()))
            throw new AccessDeniedException("Giảng viên đang hướng dẫn đề tài không thể tham gia phản biện hoặc chấm điểm đề tài này.");
        if (defense.finalized) throw new IllegalArgumentException("Điểm đã tổng hợp, không thể sửa.");
        var deadline = defense.group.getTopic().getPeriod().getReviewDeadline();
        if (defense.reviewer.getId().equals(user.getId()) && deadline != null && LocalDateTime.now().isAfter(deadline))
            throw new IllegalArgumentException("Đã hết hạn nộp điểm phản biện.");
        if (score == null || score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(BigDecimal.TEN) > 0 || score.scale() > 2)
            throw new IllegalArgumentException("Điểm từ 0 đến 10, tối đa 2 chữ số thập phân.");
        if (comment == null || comment.isBlank() || comment.length() > 2000)
            throw new IllegalArgumentException("Nhận xét cần từ 1 đến 2.000 ký tự.");
        Grade grade = grades.findByDefenseIdAndEvaluatorId(id, user.getId()).orElseGet(Grade::new);
        grade.defense = defense;
        grade.evaluator = user;
        grade.score = score;
        grade.comment = comment.strip();
        grade.updatedAt = LocalDateTime.now();
        grades.save(grade);
    }

    public void finalizeScore(Long id) {
        User user = staff();
        Defense defense = locked(id);
        if (defense.council.getMembers().stream().noneMatch(member -> member.getLecturer().getId().equals(user.getId())
                && member.getRole() == CouncilRole.CHAIRPERSON))
            throw new AccessDeniedException("Chỉ Chủ tịch hội đồng tổng hợp điểm.");
        if (defense.finalized) throw new IllegalArgumentException("Đã tổng hợp điểm.");
        var all = grades.findByDefenseId(id);
        if (all.size() != defense.council.getMembers().size())
            throw new IllegalArgumentException("Chưa đủ điểm của tất cả thành viên.");
        defense.finalScore = all.stream().map(grade -> grade.score).reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(all.size()), 2, RoundingMode.HALF_UP);
        defense.finalized = true;
    }

    public void publish(Long id) {
        User actor = dean();
        Defense defense = locked(id);
        if (!defense.finalized) throw new IllegalArgumentException("Chủ tịch chưa tổng hợp điểm.");
        if (defense.published) return;
        defense.published = true;
        notifications.enqueue(defense);
        audit.log(actor, "PUBLISH_RESULT", "Defense", id, "published=false",
            "published=true,score=" + defense.finalScore);
    }

    private Defense locked(Long id) {
        Defense defense = entityManager.find(Defense.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (defense == null) throw new IllegalArgumentException("Không tìm thấy phân công.");
        entityManager.lock(defense.council, LockModeType.PESSIMISTIC_WRITE);
        entityManager.refresh(defense.council);
        return defense;
    }
    private User staff() { User user = accounts.user(); TopicPolicy.staff(user); return user; }
    private User dean() { User user = staff(); if (user.getRole() != Role.DEAN) throw new AccessDeniedException("Chỉ trưởng khoa được thực hiện."); return user; }
    private boolean member(Council council, User user) { return council.getMembers().stream().anyMatch(m -> m.getLecturer().getId().equals(user.getId())); }
    private boolean advisor(Topic topic, Long id) { return (topic.getAdvisor1()!=null&&topic.getAdvisor1().getId().equals(id))||(topic.getAdvisor2()!=null&&topic.getAdvisor2().getId().equals(id)); }
}
