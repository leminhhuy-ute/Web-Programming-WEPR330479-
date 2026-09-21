package vn.edu.hcmute.topicmanagement.dto.response;

import vn.edu.hcmute.topicmanagement.entity.GroupMember;
import vn.edu.hcmute.topicmanagement.entity.StudentGroup;
import vn.edu.hcmute.topicmanagement.entity.Topic;
import vn.edu.hcmute.topicmanagement.entity.TopicRegistration;

import java.time.LocalDateTime;
import java.util.List;

public record TopicRegistrationResponse(
        Long id,
        Long groupId,
        String groupCode,
        String groupName,
        String leaderName,
        int memberCount,
        Long topicId,
        String topicCode,
        String topicTitle,
        String status,
        String proposalNote,
        String supervisorFeedback,
        LocalDateTime registeredAt,
        LocalDateTime reviewedAt
) {
    public static TopicRegistrationResponse fromEntity(TopicRegistration reg) {
        StudentGroup g = reg.getGroup();
        Topic t = reg.getTopic();

        return new TopicRegistrationResponse(
                reg.getId(),
                g != null ? g.getId() : null,
                g != null ? g.getGroupCode() : null,
                g != null ? g.getGroupName() : null,
                g != null && g.getLeader() != null ? g.getLeader().getFullName() : null,
                g != null ? g.getMemberCount() : 0,
                t != null ? t.getId() : null,
                t != null ? t.getTopicCode() : null,
                t != null ? t.getTitle() : null,
                reg.getStatus().name(),
                reg.getProposalNote(),
                reg.getSupervisorFeedback(),
                reg.getRegisteredAt(),
                reg.getReviewedAt()
        );
    }
}
