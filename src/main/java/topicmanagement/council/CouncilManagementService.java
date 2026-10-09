package topicmanagement.council;

import java.time.LocalDateTime;
import java.util.Objects;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import topicmanagement.entity.User;
import topicmanagement.enums.Role;
import topicmanagement.exception.ConflictException;
import topicmanagement.repository.UserRepository;
import topicmanagement.security.CurrentAccount;
import topicmanagement.service.TopicPolicy;

@Service
@Transactional
public class CouncilManagementService {
    private final CouncilRepository councils;
    private final UserRepository users;
    private final CurrentAccount accounts;
    private final DefenseRepository defenses;
    private final GradeRepository grades;

    public CouncilManagementService(CouncilRepository councils, UserRepository users, CurrentAccount accounts) {
        this(councils, users, accounts, null, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public CouncilManagementService(CouncilRepository councils, UserRepository users, CurrentAccount accounts,
            @org.springframework.beans.factory.annotation.Autowired(required = false) DefenseRepository defenses,
            @org.springframework.beans.factory.annotation.Autowired(required = false) GradeRepository grades) {
        this.councils = councils;
        this.users = users;
        this.accounts = accounts;
        this.defenses = defenses;
        this.grades = grades;
    }

    public void create(CouncilService.CreateInput input) {
        dean();
        validateMembers(input);
        if (input.defenseDate().isBefore(LocalDateTime.now()))
            throw new IllegalArgumentException("Ngày hội đồng không được trong quá khứ.");
        if (councils.existsByCode(input.code().strip()))
            throw new ConflictException("Mã hội đồng đã tồn tại.");

        Council council = new Council(input.code().strip(), input.name().strip(), input.defenseDate(), input.room().strip());
        for (var member : input.members()) {
            User lecturer = users.findById(member.userId())
                .orElseThrow(() -> new IllegalArgumentException("Giảng viên không tồn tại."));
            TopicPolicy.staff(lecturer);
            council.getMembers().add(new CouncilMember(council, lecturer, member.role()));
        }
        council.setStatus(CouncilStatus.READY);
        councils.save(council);
    }

    public void update(Long id, CouncilService.CreateInput input) {
        dean();
        Council council = councils.lockById(id)
            .orElseThrow(() -> new IllegalArgumentException("Hội đồng không tồn tại."));
        if (grades != null && grades.existsByDefenseCouncilId(id)) {
            throw new ConflictException("Không thể chỉnh sửa hội đồng khi đã có điểm đánh giá.");
        }
        validateMembers(input);
        if (input.defenseDate().isBefore(LocalDateTime.now()))
            throw new IllegalArgumentException("Ngày hội đồng không được trong quá khứ.");
        if (councils.existsByCodeAndIdNot(input.code().strip(), id)) {
            throw new ConflictException("Mã hội đồng đã tồn tại.");
        }

        // Check every proposed member, including lecturers retained from the previous roster.
        for (var member : input.members()) {
            TopicPolicy.staff(users.findById(member.userId())
                .orElseThrow(() -> new IllegalArgumentException("Giảng viên không tồn tại.")));
        }
        validateAssignments(id, input);

        council.setCode(input.code().strip());
        council.setName(input.name().strip());
        council.setDefenseDate(input.defenseDate());
        council.setRoom(input.room().strip());

        java.util.Map<Long, CouncilMember> existingMap = council.getMembers().stream()
            .collect(java.util.stream.Collectors.toMap(m -> m.getLecturer().getId(), m -> m));
        java.util.Set<Long> inputLecturerIds = input.members().stream()
            .map(CouncilService.MemberInput::userId)
            .collect(java.util.stream.Collectors.toSet());

        council.getMembers().removeIf(m -> !inputLecturerIds.contains(m.getLecturer().getId()));

        for (var member : input.members()) {
            CouncilMember existing = existingMap.get(member.userId());
            if (existing != null) {
                existing.setRole(member.role());
            } else {
                User lecturer = users.findById(member.userId())
                    .orElseThrow(() -> new IllegalArgumentException("Giảng viên không tồn tại."));
                TopicPolicy.staff(lecturer);
                council.getMembers().add(new CouncilMember(council, lecturer, member.role()));
            }
        }
        councils.save(council);
    }

    private void validateAssignments(Long councilId, CouncilService.CreateInput input) {
        if (defenses == null) return;
        for (Defense defense : defenses.findByCouncilId(councilId)) {
            var topic = defense.topic != null ? defense.topic : defense.group.getTopic();
            boolean hasAdvisor = input.members().stream().anyMatch(member ->
                (topic.getAdvisor1() != null && topic.getAdvisor1().getId().equals(member.userId()))
                || (topic.getAdvisor2() != null && topic.getAdvisor2().getId().equals(member.userId())));
            if (hasAdvisor)
                throw new ConflictException("Không thể thêm GVHD của nhóm đã phân công vào hội đồng.");
            if (input.members().stream().noneMatch(member -> member.userId().equals(defense.reviewer.getId())
                    && member.role() == CouncilRole.REVIEWER))
                throw new ConflictException("Phải giữ GVPB đã phân công với vai trò Phản biện.");
            var date = topic.getPeriod().getDefenseDate();
            if (date != null && !date.equals(input.defenseDate().toLocalDate()))
                throw new ConflictException("Ngày hội đồng phải khớp ngày bảo vệ của đợt đã phân công.");
        }
    }

    public void delete(Long id) {
        dean();
        Council council = councils.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Hội đồng không tồn tại."));
        if (defenses != null && defenses.existsByCouncilId(id)) {
            throw new ConflictException("Không thể xóa hội đồng khi đã có nhóm được phân công bảo vệ.");
        }
        councils.delete(council);
    }

    private void validateMembers(CouncilService.CreateInput input) {
        if (input.members() == null || input.members().size() < 3 || input.members().size() > 5)
            throw new IllegalArgumentException("Hội đồng phải có 3–5 thành viên.");
        if (input.members().stream().anyMatch(Objects::isNull))
            throw new IllegalArgumentException("Thành viên không được để trống.");
        if (input.members().stream().anyMatch(m -> m.userId() == null || m.role() == null))
            throw new IllegalArgumentException("Thông tin thành viên hội đồng không hợp lệ.");
        if (input.members().stream().map(CouncilService.MemberInput::userId).distinct().count() != input.members().size())
            throw new IllegalArgumentException("Thành viên không được trùng.");
        if (input.members().stream().filter(m -> m.role() == CouncilRole.CHAIRPERSON).count() != 1
                || input.members().stream().filter(m -> m.role() == CouncilRole.SECRETARY).count() != 1)
            throw new IllegalArgumentException("Cần đúng một Chủ tịch và một Thư ký.");
        if (input.members().stream().noneMatch(m -> m.role() == CouncilRole.REVIEWER))
            throw new IllegalArgumentException("Hội đồng cần ít nhất một Giảng viên phản biện.");
    }

    private User dean() {
        User user = accounts.user();
        TopicPolicy.staff(user);
        if (user.getRole() != Role.DEAN) throw new AccessDeniedException("Chỉ trưởng khoa được thực hiện.");
        return user;
    }
}
