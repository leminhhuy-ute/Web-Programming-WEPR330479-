package topicmanagement.service;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import topicmanagement.dto.request.GroupApprovalRequest;
import topicmanagement.dto.response.StudentGroupResponse;
import topicmanagement.dto.response.PageResponse;
import topicmanagement.entity.StudentGroup;
import topicmanagement.entity.User;
import topicmanagement.entity.Topic;
import org.springframework.security.access.AccessDeniedException;
import topicmanagement.enums.GroupStatus;
import topicmanagement.enums.RegistrationStatus;
import topicmanagement.exception.ResourceNotFoundException;
import topicmanagement.repository.StudentGroupRepository;
import topicmanagement.repository.GroupMemberRepository;

@Service
public class StudentGroupService {
    public record Guidance(String groupName, String tasks, String requirements, LocalDateTime completedAt, boolean canManage) {}

    private boolean assignedAdvisor(User actor, Topic topic) {
        return topic != null && ((topic.getAdvisor1() != null && Objects.equals(topic.getAdvisor1().getId(), actor.getId()))
            || (topic.getAdvisor2() != null && Objects.equals(topic.getAdvisor2().getId(), actor.getId())));
    }

    private StudentGroup guidanceGroup(Long id, User actor) {
        TopicPolicy.staff(actor);
        StudentGroup group = groupRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhóm sinh viên có ID: " + id));
        Topic topic = group.getTopic();
        boolean manager = actor.getRole() == topicmanagement.enums.Role.DEAN
            || (actor.getRole() == topicmanagement.enums.Role.HEAD_OF_DEPT && actor.getDepartment() != null
                && topic != null && topic.getDepartment() != null
                && Objects.equals(actor.getDepartment().getId(), topic.getDepartment().getId()));
        if (!assignedAdvisor(actor, topic) && !manager)
            throw new AccessDeniedException("Bạn không được xem thông tin hướng dẫn của nhóm này.");
        return group;
    }

    private Guidance guidanceView(StudentGroup group, User actor) {
        return new Guidance(group.getGroupName(), group.getAdvisorTasks(), group.getAdvisorRequirements(),
            group.getAdvisorCompletedAt(), assignedAdvisor(actor, group.getTopic()));
    }

    @Transactional(readOnly = true)
    public Guidance getGuidance(Long id, User actor) {
        return guidanceView(guidanceGroup(id, actor), actor);
    }

    @Transactional
    public Guidance updateGuidance(Long id, String tasks, String requirements, User actor) {
        StudentGroup group = guidanceGroup(id, actor);
        if (!assignedAdvisor(actor, group.getTopic()))
            throw new AccessDeniedException("Chỉ giảng viên hướng dẫn của đề tài được giao nhiệm vụ.");
        if (group.getStatus() != GroupStatus.APPROVED)
            throw new IllegalArgumentException("Cần duyệt nhóm trước khi giao nhiệm vụ.");
        group.setAdvisorTasks(tasks == null ? null : tasks.strip());
        group.setAdvisorRequirements(requirements == null ? null : requirements.strip());
        return guidanceView(group, actor);
    }

    @Transactional
    public Guidance confirmCompletion(Long id, User actor) {
        StudentGroup group = guidanceGroup(id, actor);
        if (!assignedAdvisor(actor, group.getTopic()))
            throw new AccessDeniedException("Chỉ giảng viên hướng dẫn của đề tài được xác nhận hoàn thành.");
        if (group.getStatus() != GroupStatus.APPROVED)
            throw new IllegalArgumentException("Chỉ xác nhận hoàn thành cho nhóm đã được duyệt.");
        if (group.getAdvisorCompletedAt() == null) {
            group.setAdvisorCompletedAt(LocalDateTime.now());
            audit.log(actor, "CONFIRM_TOPIC_COMPLETION", "StudentGroup", id, "IN_PROGRESS", "COMPLETED");
        }
        return guidanceView(group, actor);
    }
    private final StudentGroupRepository groupRepository;
    private final TopicRegistrationService registrations;
    private final AuditLogService audit;
    private final GroupMemberRepository members;

    @Autowired
    public StudentGroupService(StudentGroupRepository groupRepository, TopicRegistrationService registrations,
            AuditLogService audit, GroupMemberRepository members) {
        this.groupRepository = groupRepository;
        this.registrations = registrations;
        this.audit = audit;
        this.members = members;
    }

    @Transactional(readOnly = true)
    public List<StudentGroupResponse> getGroupsForLecturer(User lecturer) {
        List<StudentGroup> groups = groupRepository.findGroupsForLecturer(lecturer.getId());
        if (lecturer.getRole() == topicmanagement.enums.Role.DEAN) {
            groups = groupRepository.findByTopicIsNotNull();
        } else if (lecturer.getRole() == topicmanagement.enums.Role.HEAD_OF_DEPT && lecturer.getDepartment() != null) {
            groups = groupRepository.findGroupsByDepartment(lecturer.getDepartment().getId());
        }
        return groups.stream().map(StudentGroupResponse::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<StudentGroupResponse> getGroupsForLecturer(User lecturer, int page, int size) {
        var pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100),
            Sort.by(Sort.Direction.DESC, "registeredAt"));
        var result = switch (lecturer.getRole()) {
            case DEAN -> groupRepository.findByTopicIsNotNull(pageable);
            case HEAD_OF_DEPT -> lecturer.getDepartment() == null
                ? groupRepository.findGroupsForLecturer(lecturer.getId(), pageable)
                : groupRepository.findGroupsByDepartment(lecturer.getDepartment().getId(), pageable);
            default -> groupRepository.findGroupsForLecturer(lecturer.getId(), pageable);
        };
        return PageResponse.from(result.map(StudentGroupResponse::new));
    }

    @Transactional(readOnly = true)
    public List<StudentGroupResponse> getGroupsByTopic(Long topicId, User currentUser) {
        List<StudentGroup> groups = groupRepository.findByTopicId(topicId);
        for (StudentGroup group : groups) TopicPolicy.advisor(currentUser, group.getTopic());
        return groups.stream().map(StudentGroupResponse::new).collect(Collectors.toList());
    }

    @Transactional
    public StudentGroupResponse approveOrRejectGroup(Long groupId, GroupApprovalRequest request, User currentUser) {
        StudentGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhóm sinh viên có ID: " + groupId));

        if (group.getTopic() == null) throw new IllegalArgumentException("Nhóm chưa đăng ký đề tài.");
        if (!members.existsByGroupIdAndStudentId(group.getId(), group.getLeader().getId()))
            throw new IllegalStateException("Dữ liệu nhóm không hợp lệ: nhóm trưởng chưa thuộc danh sách thành viên.");
        TopicPolicy.advisor(currentUser, group.getTopic());
        if (group.getStatus() != GroupStatus.PENDING) throw new IllegalArgumentException("Chỉ duyệt nhóm đang chờ.");
        GroupStatus newStatus;
        try {
            newStatus = GroupStatus.valueOf(request.getStatus());
        } catch (Exception e) {
            throw new IllegalArgumentException("Trạng thái nhóm không hợp lệ (Phải là APPROVED hoặc REJECTED).");
        }

        if (newStatus != GroupStatus.APPROVED && newStatus != GroupStatus.REJECTED)
            throw new IllegalArgumentException("Trạng thái duyệt không hợp lệ.");
        if (newStatus == GroupStatus.REJECTED && (request.getNotes() == null || request.getNotes().isBlank()))
            throw new IllegalArgumentException("Vui lòng nhập lý do từ chối nhóm.");
        if (registrations.latest(group.getId()).isEmpty()) registrations.importLegacy(group, currentUser);
        registrations.decidePending(group.getId(),
            newStatus == GroupStatus.APPROVED ? RegistrationStatus.APPROVED : RegistrationStatus.REJECTED,
            request.getNotes(), currentUser);

        StudentGroup saved = groupRepository.save(group);
        audit.log(currentUser, newStatus == GroupStatus.APPROVED ? "APPROVE_GROUP" : "REJECT_GROUP",
            "StudentGroup", groupId, "PENDING", newStatus.name());
        return new StudentGroupResponse(saved);
    }
}
