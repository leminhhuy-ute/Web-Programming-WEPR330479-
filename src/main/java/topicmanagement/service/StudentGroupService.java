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
        if (groups.isEmpty() && lecturer.getDepartment() != null) {
            groups = groupRepository.findGroupsByDepartment(lecturer.getDepartment().getId());
        }
        return groups.stream().map(StudentGroupResponse::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StudentGroupResponse> getGroupsByTopic(Long topicId) {
        List<StudentGroup> groups = groupRepository.findByTopicId(topicId);
        return groups.stream().map(StudentGroupResponse::new).collect(Collectors.toList());
    }

    @Transactional
    public StudentGroupResponse approveOrRejectGroup(Long groupId, GroupApprovalRequest request, User currentUser) {
        StudentGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhóm sinh viên có ID: " + groupId));

        GroupStatus newStatus;
        try {
            newStatus = GroupStatus.valueOf(request.getStatus());
        } catch (Exception e) {
            throw new IllegalArgumentException("Trạng thái nhóm không hợp lệ (Phải là APPROVED hoặc REJECTED).");
        }

        group.setStatus(newStatus);
        if (request.getNotes() != null && !request.getNotes().isBlank()) {
            group.setNotes(request.getNotes());
        }

        StudentGroup saved = groupRepository.save(group);
        return new StudentGroupResponse(saved);
    }
}
