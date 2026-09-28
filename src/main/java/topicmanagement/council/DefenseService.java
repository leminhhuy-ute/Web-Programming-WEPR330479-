package topicmanagement.council;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import topicmanagement.entity.StudentGroup;
import topicmanagement.entity.Topic;
import topicmanagement.entity.User;
import topicmanagement.enums.GroupStatus;
import topicmanagement.enums.Role;
import topicmanagement.repository.UserRepository;
import topicmanagement.security.CurrentAccount;
import topicmanagement.service.TopicPolicy;

@Service
@Transactional
public class DefenseService {
    private final DefenseRepository defenses;
    private final CouncilRepository councils;
    private final UserRepository users;
    private final CurrentAccount accounts;
    private final EntityManager entityManager;
    private final topicmanagement.repository.TopicRegistrationRepository topicRegistrations;

    public DefenseService(DefenseRepository defenses, CouncilRepository councils, UserRepository users,
            CurrentAccount accounts, EntityManager entityManager) {
        this(defenses, councils, users, accounts, entityManager, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public DefenseService(DefenseRepository defenses, CouncilRepository councils, UserRepository users,
            CurrentAccount accounts, EntityManager entityManager,
            @org.springframework.beans.factory.annotation.Autowired(required = false) topicmanagement.repository.TopicRegistrationRepository topicRegistrations) {
        this.defenses = defenses;
        this.councils = councils;
        this.users = users;
        this.accounts = accounts;
        this.entityManager = entityManager;
        this.topicRegistrations = topicRegistrations;
    }

    public void assign(Long groupId, Long councilId, Long reviewerId) {
        dean();
        StudentGroup group = entityManager.find(StudentGroup.class, groupId, LockModeType.PESSIMISTIC_WRITE);
        if (group == null || group.getStatus() != GroupStatus.APPROVED)
            throw new IllegalArgumentException("Nhóm phải được giảng viên chấp thuận.");
        entityManager.lock(group.getTopic(), LockModeType.PESSIMISTIC_WRITE);
        entityManager.refresh(group.getTopic());
        if (defenses.findByGroupId(groupId).isPresent()) throw new IllegalArgumentException("Nhóm đã được phân công.");
        Council council = councils.findById(councilId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hội đồng."));
        if (council.getMembers().stream().noneMatch(member -> member.getLecturer().getId().equals(reviewerId)
                && member.getRole() == CouncilRole.REVIEWER))
            throw new IllegalArgumentException("GVPB phải là thành viên có vai trò phản biện trong hội đồng.");
        if (council.getMembers().stream().anyMatch(member -> advisor(group.getTopic(), member.getLecturer().getId())))
            throw new IllegalArgumentException("Giảng viên đang hướng dẫn đề tài không thể tham gia phản biện đề tài này.");
        var period = group.getTopic().getPeriod();
        if (period.getDefenseDate() != null && !period.getDefenseDate().equals(council.getDefenseDate().toLocalDate()))
            throw new IllegalArgumentException("Ngày hội đồng phải khớp ngày báo cáo của đợt đăng ký.");
        Defense defense = new Defense();
        defense.group = group;
        defense.council = council;
        defense.reviewer = users.findById(reviewerId).orElseThrow();
        defense.topic = group.getTopic();
        if (topicRegistrations != null) {
            topicRegistrations.findFirstByGroupIdAndStatusInOrderByCreatedAtDesc(
                groupId, java.util.List.of(topicmanagement.enums.RegistrationStatus.APPROVED))
                .ifPresent(reg -> defense.registration = reg);
        }
        defenses.save(defense);
    }

    private boolean advisor(Topic topic, Long id) {
        return (topic.getAdvisor1() != null && topic.getAdvisor1().getId().equals(id))
            || (topic.getAdvisor2() != null && topic.getAdvisor2().getId().equals(id));
    }

    private void dean() {
        User user = accounts.user();
        TopicPolicy.staff(user);
        if (user.getRole() != Role.DEAN) throw new AccessDeniedException("Chỉ trưởng khoa được thực hiện.");
    }
}
