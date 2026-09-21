package vn.edu.hcmute.topicmanagement.dto.response;

import vn.edu.hcmute.topicmanagement.entity.GroupInvitation;

import java.time.LocalDateTime;

public record InvitationResponse(
        Long id,
        Long groupId,
        String groupCode,
        String groupName,
        Long inviterId,
        String inviterName,
        Long inviteeId,
        String inviteeName,
        String inviteeCode,
        String message,
        String status,
        LocalDateTime createdAt
) {
    public static InvitationResponse fromEntity(GroupInvitation i) {
        return new InvitationResponse(
                i.getId(),
                i.getGroup().getId(),
                i.getGroup().getGroupCode(),
                i.getGroup().getGroupName(),
                i.getInviter().getId(),
                i.getInviter().getFullName(),
                i.getInvitee().getId(),
                i.getInvitee().getFullName(),
                i.getInvitee().getUserCode(),
                i.getMessage(),
                i.getStatus().name(),
                i.getCreatedAt()
        );
    }
}
