package topicmanagement.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import topicmanagement.dto.request.GroupApprovalRequest;
import topicmanagement.dto.response.StudentGroupResponse;
import topicmanagement.entity.StudentGroup;
import topicmanagement.entity.User;
import topicmanagement.enums.GroupStatus;
import topicmanagement.exception.ResourceNotFoundException;
import topicmanagement.repository.StudentGroupRepository;

@Service
public class StudentGroupService {
    private final StudentGroupRepository groupRepository;

    @Autowired
    public StudentGroupService(StudentGroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @Transactional(readOnly = true)
    public List<StudentGroupResponse> getGroupsForLecturer(User lecturer) {
        List<StudentGroup> groups = groupRepository.findGroupsForLecturer(lecturer.getId());
        if (lecturer.getRole() == topicmanagement.enums.Role.DEAN) groups = groupRepository.findAll().stream().filter(g -> g.getTopic() != null).toList();
        return groups.stream().map(StudentGroupResponse::new).collect(Collectors.toList());
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
        group.setStatus(newStatus);
        if (request.getNotes() != null && !request.getNotes().isBlank()) {
            group.setNotes(request.getNotes());
        }

        StudentGroup saved = groupRepository.save(group);
        return new StudentGroupResponse(saved);
    }
}
