package topicmanagement.service;

import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import topicmanagement.council.DefenseRepository;
import topicmanagement.council.GradeRepository;
import topicmanagement.dto.response.CouncilStateResponse.GradeView;
import topicmanagement.dto.response.StudentResultResponse;
import topicmanagement.enums.Role;
import topicmanagement.repository.GroupMemberRepository;
import topicmanagement.security.CurrentAccount;

@Service
@Transactional(readOnly = true)
public class StudentResultService {
    private final CurrentAccount accounts;
    private final GroupMemberRepository members;
    private final DefenseRepository defenses;
    private final GradeRepository grades;

    public StudentResultService(CurrentAccount accounts, GroupMemberRepository members,
            DefenseRepository defenses, GradeRepository grades) {
        this.accounts = accounts;
        this.members = members;
        this.defenses = defenses;
        this.grades = grades;
    }

    public StudentResultResponse current() {
        return current(null);
    }

    public StudentResultResponse current(Long periodId) {
        var user = accounts.user();
        if (user.getRole() != Role.STUDENT) throw new AccessDeniedException("Chỉ sinh viên.");

        List<topicmanagement.entity.GroupMember> allMemberships = members.findByStudentId(user.getId());
        if (allMemberships.isEmpty()) return StudentResultResponse.unpublished();

        topicmanagement.entity.GroupMember targetMembership;
        if (periodId != null) {
            targetMembership = allMemberships.stream()
                .filter(m -> matchesPeriod(m, periodId))
                .findFirst()
                .orElse(null);
            if (targetMembership == null) {
                return StudentResultResponse.unpublished();
            }
        } else {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            targetMembership = allMemberships.stream()
                .sorted(java.util.Comparator
                    // 1. Prioritize active registration period
                    .comparing((topicmanagement.entity.GroupMember m) -> isActivePeriod(resolvePeriod(m), now)).reversed()
                    // 2. Prioritize period end/defense date (latest first)
                    .thenComparing(this::periodEndDate, java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder()))
                    // 3. Prioritize higher period ID (latest period first)
                    .thenComparing(this::periodId, java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder()))
                    // 4. Prioritize higher group ID (latest group first, never earliest random group)
                    .thenComparing((topicmanagement.entity.GroupMember m) -> m.getGroup() != null ? m.getGroup().getId() : 0L, java.util.Comparator.reverseOrder())
                    // 5. Prioritize higher member ID
                    .thenComparing(topicmanagement.entity.GroupMember::getId, java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder()))
                )
                .findFirst()
                .orElse(null);
        }

        if (targetMembership == null || targetMembership.getGroup() == null) {
            return StudentResultResponse.unpublished();
        }

        var defenseOpt = defenses.findByGroupId(targetMembership.getGroup().getId()).filter(item -> item.published);
        if (defenseOpt.isEmpty()) return StudentResultResponse.unpublished();

        var result = defenseOpt.get();
        if (result.registration != null && result.registration.getStatus() != topicmanagement.enums.RegistrationStatus.APPROVED) {
            return StudentResultResponse.unpublished();
        }

        String topicTitle = result.topic != null ? result.topic.getTitle()
            : (result.registration != null ? result.registration.getTopic().getTitle()
            : (result.group.getTopic() != null ? result.group.getTopic().getTitle() : ""));

        return new StudentResultResponse(true, result.finalScore, topicTitle,
            grades.findByDefenseId(result.id).stream()
                .map(grade -> new GradeView(grade.evaluator.getFullName(), grade.score, grade.comment)).toList());
    }

    private topicmanagement.entity.RegistrationPeriod resolvePeriod(topicmanagement.entity.GroupMember m) {
        if (m == null) return null;
        if (m.getRegistrationPeriod() != null) return m.getRegistrationPeriod();
        if (m.getGroup() != null) return m.getGroup().getPeriod();
        return null;
    }

    private boolean matchesPeriod(topicmanagement.entity.GroupMember m, Long periodId) {
        topicmanagement.entity.RegistrationPeriod p = resolvePeriod(m);
        return p != null && p.getId().equals(periodId);
    }

    private boolean isActivePeriod(topicmanagement.entity.RegistrationPeriod p, java.time.LocalDateTime now) {
        if (p == null) return false;
        java.time.LocalDateTime start = p.getStudentStartAt() != null ? p.getStudentStartAt() : p.getLecturerStartAt();
        if (start == null || now.isBefore(start)) return false;

        if (p.getDefenseDate() != null) {
            return !now.toLocalDate().isAfter(p.getDefenseDate());
        }
        if (p.getStudentEndAt() != null) {
            return !now.isAfter(p.getStudentEndAt());
        }
        return false;
    }

    private java.time.LocalDateTime periodEndDate(topicmanagement.entity.GroupMember m) {
        topicmanagement.entity.RegistrationPeriod p = resolvePeriod(m);
        if (p == null) return null;
        if (p.getDefenseDate() != null) {
            return p.getDefenseDate().atTime(23, 59, 59);
        }
        if (p.getStudentEndAt() != null) {
            return p.getStudentEndAt();
        }
        return p.getLecturerEndAt();
    }

    private Long periodId(topicmanagement.entity.GroupMember m) {
        topicmanagement.entity.RegistrationPeriod p = resolvePeriod(m);
        return p != null ? p.getId() : null;
    }
}
