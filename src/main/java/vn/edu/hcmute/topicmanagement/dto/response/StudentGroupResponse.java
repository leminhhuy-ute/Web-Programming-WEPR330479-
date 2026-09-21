package vn.edu.hcmute.topicmanagement.dto.response;

import vn.edu.hcmute.topicmanagement.entity.StudentGroup;

import java.time.LocalDateTime;
import java.util.List;

public record StudentGroupResponse(
        Long id,
        String groupCode,
        String groupName,
        Long leaderId,
        String leaderName,
        String leaderCode,
        int memberCount,
        int maxMembers,
        String status,
        String notes,
        Long topicId,
        String topicCode,
        String topicTitle,
        List<GroupMemberResponse> members,
        LocalDateTime createdAt
) {
    public static StudentGroupResponse fromEntity(StudentGroup g, List<GroupMemberResponse> members) {
        return new StudentGroupResponse(
                g.getId(),
                g.getGroupCode(),
                g.getGroupName(),
                g.getLeader() != null ? g.getLeader().getId() : null,
                g.getLeader() != null ? g.getLeader().getFullName() : null,
                g.getLeader() != null ? g.getLeader().getUserCode() : null,
                g.getMemberCount(),
                g.getMaxMembers(),
                g.getStatus().name(),
                g.getNotes(),
                g.getTopic() != null ? g.getTopic().getId() : null,
                g.getTopic() != null ? g.getTopic().getTopicCode() : null,
                g.getTopic() != null ? g.getTopic().getTitle() : null,
                members,
                g.getCreatedAt()
        );
    }
}
