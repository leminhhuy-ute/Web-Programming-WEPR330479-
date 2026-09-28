package topicmanagement.service;

import java.util.List;
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
import topicmanagement.enums.GroupStatus;
import topicmanagement.enums.RegistrationStatus;
import topicmanagement.exception.ResourceNotFoundException;
import topicmanagement.repository.StudentGroupRepository;
import topicmanagement.repository.GroupMemberRepository;

@Service
public class StudentGroupService {
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
