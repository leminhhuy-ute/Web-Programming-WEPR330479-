package topicmanagement.service;

import java.time.LocalDateTime;
import java.util.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import topicmanagement.dto.response.*;
import topicmanagement.entity.*;
import topicmanagement.enums.*;
import topicmanagement.exception.ConflictException;
import topicmanagement.exception.ResourceNotFoundException;
import topicmanagement.repository.*;
import topicmanagement.student.ReportRepository;
import topicmanagement.council.DefenseRepository;
import topicmanagement.council.Defense;

@Service
@Transactional
public class TopicRegistrationService {
    private final TopicRegistrationRepository registrations;
    private final RegistrationStatusHistoryRepository history;
    private final AdvisorQuotaService quotas;
    private final ReportRepository reports;
    private final DefenseRepository defenses;

    public TopicRegistrationService(TopicRegistrationRepository registrations,
            RegistrationStatusHistoryRepository history, AdvisorQuotaService quotas) {
        this(registrations, history, quotas, null, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public TopicRegistrationService(TopicRegistrationRepository registrations,
            RegistrationStatusHistoryRepository history, AdvisorQuotaService quotas,
            @org.springframework.beans.factory.annotation.Autowired(required = false) ReportRepository reports,
            @org.springframework.beans.factory.annotation.Autowired(required = false) DefenseRepository defenses) {
        this.registrations = registrations;
        this.history = history;
        this.quotas = quotas;
        this.reports = reports;
        this.defenses = defenses;
    }

    public TopicRegistration submit(StudentGroup group, Topic topic, User actor) {
        registrations.findFirstByGroupIdAndStatusInOrderByCreatedAtDesc(group.getId(),
            List.of(RegistrationStatus.DRAFT, RegistrationStatus.PENDING, RegistrationStatus.APPROVED))
            .ifPresent(r -> { throw new IllegalArgumentException("Nhóm đã có một đăng ký đang hoạt động."); });
        TopicRegistration registration = new TopicRegistration();
        registration.setGroup(group);
        registration.setTopic(topic);
        registration.setStatus(RegistrationStatus.DRAFT);
        registrations.save(registration);
        transition(registration, RegistrationStatus.PENDING, actor, "Sinh viên gửi đăng ký đề tài.");
        return registration;
    }

    public TopicRegistration decidePending(Long groupId, RegistrationStatus target, String note, User actor) {
        if (target != RegistrationStatus.APPROVED && target != RegistrationStatus.REJECTED)
            throw new IllegalArgumentException("Trạng thái duyệt không hợp lệ.");
        TopicRegistration registration = registrations
            .findFirstByGroupIdAndStatusInOrderByCreatedAtDesc(groupId, List.of(RegistrationStatus.PENDING))
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đăng ký đang chờ duyệt."));
        if (target == RegistrationStatus.APPROVED) {
            Topic topic = registration.getTopic();
            if (topic.getAdvisor1() != null) quotas.assertAvailable(topic.getPeriod(), topic.getAdvisor1(), 1);
            if (topic.getAdvisor2() != null) quotas.assertAvailable(topic.getPeriod(), topic.getAdvisor2(), 1);
        }
        transition(registration, target, actor, note);
        return registration;
    }

    public TopicRegistration cancelByStudent(StudentGroup group, User actor, String note) {
        if (!group.getLeader().getId().equals(actor.getId()))
            throw new AccessDeniedException("Chỉ nhóm trưởng được hủy đăng ký.");
        TopicRegistration registration = active(group.getId());
        if (LocalDateTime.now().isAfter(registration.getTopic().getPeriod().getStudentEndAt()))
            throw new IllegalArgumentException("Đã hết hạn tự hủy đăng ký.");
        assertCanCancel(registration);
        transition(registration, RegistrationStatus.CANCELLED, actor, normalize(note, "Nhóm sinh viên hủy đăng ký."));
        return registration;
    }

    public TopicRegistration cancelByDean(Long id, User actor, String note) {
        if (actor.getRole() != Role.DEAN) throw new AccessDeniedException("Chỉ Trưởng khoa được hủy đăng ký.");
        TopicRegistration registration = registrations.lockById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Đăng ký không tồn tại."));
        if (registration.getStatus() == RegistrationStatus.CANCELLED)
            throw new IllegalArgumentException("Đăng ký đã được hủy.");
        assertCanCancel(registration);
        transition(registration, RegistrationStatus.CANCELLED, actor, normalize(note, "Trưởng khoa hủy đăng ký."));
        syncLegacy(registration);
        return registration;
    }

    private void assertCanCancel(TopicRegistration registration) {
        if (reports != null && (reports.existsByRegistrationId(registration.getId())
                || reports.existsByGroupIdAndStage(registration.getGroup().getId(), "Đề cương")
                || reports.existsByGroupIdAndStage(registration.getGroup().getId(), "Giữa kỳ")
                || reports.existsByGroupIdAndStage(registration.getGroup().getId(), "Cuối kỳ"))) {
            throw new ConflictException("Không thể hủy đăng ký khi đã có báo cáo tiến độ được nộp.");
        }
        if (defenses != null) {
            var defenseOpt = defenses.findByRegistrationId(registration.getId())
                .or(() -> defenses.findByGroupId(registration.getGroup().getId()));
            if (defenseOpt.isPresent()) {
                var d = defenseOpt.get();
                if (d.published || d.finalized || d.finalScore != null) {
                    throw new ConflictException("Không thể hủy đăng ký khi đã có điểm hoặc công bố kết quả bảo vệ.");
                }
                throw new ConflictException("Không thể hủy đăng ký khi đã được xếp lịch bảo vệ.");
            }
        }
    }

    @Transactional(readOnly = true)
    public Optional<TopicRegistration> latest(Long groupId) {
        return registrations.findFirstByGroupIdOrderByCreatedAtDesc(groupId);
    }

    @Transactional(readOnly = true)
    public List<TopicRegistrationResponse> listForGroup(Long groupId) {
        return registrations.findByGroupIdOrderByCreatedAtDesc(groupId).stream().map(this::map).toList();
    }

    public TopicRegistration importLegacy(StudentGroup group, User actor) {
        var found = latest(group.getId());
        if (found.isPresent()) return found.get();
        if (group.getTopic() == null) throw new IllegalArgumentException("Nhóm chưa đăng ký đề tài.");
        TopicRegistration r = new TopicRegistration();
        r.setGroup(group);
        r.setTopic(group.getTopic());
        r.setCreatedAt(group.getRegisteredAt());
        r.setStatus(toRegistration(group.getStatus()));
        r.setReason(group.getNotes());
        if (r.getStatus() == RegistrationStatus.APPROVED || r.getStatus() == RegistrationStatus.REJECTED) {
            r.setDecidedBy(actor);
            r.setDecidedAt(LocalDateTime.now());
        }
        registrations.save(r);
        appendHistory(r, null, r.getStatus(), actor, "Chuyển dữ liệu đăng ký cũ.");
        return r;
    }

    private TopicRegistration active(Long groupId) {
        return registrations.findFirstByGroupIdAndStatusInOrderByCreatedAtDesc(groupId,
            List.of(RegistrationStatus.DRAFT, RegistrationStatus.PENDING, RegistrationStatus.APPROVED))
            .orElseThrow(() -> new IllegalArgumentException("Nhóm không có đăng ký đang hoạt động."));
    }

    private void transition(TopicRegistration registration, RegistrationStatus next, User actor, String note) {
        RegistrationStatus old = registration.getStatus();
        validateTransition(old, next);
        registration.setStatus(next);
        registration.setReason(note == null || note.isBlank() ? null : note.strip());
        if (next == RegistrationStatus.APPROVED || next == RegistrationStatus.REJECTED || next == RegistrationStatus.CANCELLED) {
            registration.setDecidedBy(actor);
            registration.setDecidedAt(LocalDateTime.now());
        }
        registrations.save(registration);
        appendHistory(registration, old, next, actor, note);
        syncLegacy(registration);
    }

    private void validateTransition(RegistrationStatus old, RegistrationStatus next) {
        boolean allowed = (old == RegistrationStatus.DRAFT && next == RegistrationStatus.PENDING)
            || (old == RegistrationStatus.PENDING && (next == RegistrationStatus.APPROVED
                || next == RegistrationStatus.REJECTED || next == RegistrationStatus.CANCELLED))
            || (old == RegistrationStatus.APPROVED && next == RegistrationStatus.CANCELLED);
        if (!allowed) throw new IllegalArgumentException("Không thể chuyển trạng thái từ " + old + " sang " + next + ".");
    }

    private void appendHistory(TopicRegistration registration, RegistrationStatus old, RegistrationStatus next,
            User actor, String note) {
        RegistrationStatusHistory h = new RegistrationStatusHistory();
        h.setRegistration(registration);
        h.setOldStatus(old);
        h.setNewStatus(next);
        h.setChangedBy(actor);
        h.setNote(note == null || note.isBlank() ? null : note.strip());
        history.save(h);
    }

    private void syncLegacy(TopicRegistration registration) {
        StudentGroup group = registration.getGroup();
        group.setTopic(registration.getStatus() == RegistrationStatus.CANCELLED ? null : registration.getTopic());
        group.setStatus(switch (registration.getStatus()) {
            case DRAFT -> GroupStatus.DRAFT;
            case PENDING -> GroupStatus.PENDING;
            case APPROVED -> GroupStatus.APPROVED;
            case REJECTED, CANCELLED -> GroupStatus.DRAFT;
        });
        group.setRegisteredAt(registration.getCreatedAt());
        group.setNotes(registration.getReason());
    }

    private RegistrationStatus toRegistration(GroupStatus status) {
        return switch (status) {
            case DRAFT -> RegistrationStatus.DRAFT;
            case PENDING -> RegistrationStatus.PENDING;
            case APPROVED -> RegistrationStatus.APPROVED;
            case REJECTED -> RegistrationStatus.REJECTED;
        };
    }

    private TopicRegistrationResponse map(TopicRegistration r) {
        List<RegistrationHistoryResponse> entries = history
            .findByRegistrationIdOrderByChangedAtDesc(r.getId()).stream()
            .map(h -> new RegistrationHistoryResponse(r.getId(), r.getTopic().getTopicCode(), r.getTopic().getTitle(),
                h.getOldStatus() == null ? null : h.getOldStatus().name(), h.getNewStatus().name(),
                h.getChangedBy().getFullName(), h.getChangedAt(), h.getNote())).toList();
        return new TopicRegistrationResponse(r.getId(), r.getGroup().getId(), r.getGroup().getGroupName(),
            r.getTopic().getId(), r.getTopic().getTopicCode(), r.getTopic().getTitle(), r.getStatus().name(),
            r.getCreatedAt(), r.getUpdatedAt(), r.getDecidedBy() == null ? null : r.getDecidedBy().getFullName(),
            r.getDecidedAt(), r.getReason(), entries);
    }

    private String normalize(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.strip();
    }
}
