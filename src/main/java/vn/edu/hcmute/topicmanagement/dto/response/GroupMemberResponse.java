package vn.edu.hcmute.topicmanagement.dto.response;

import vn.edu.hcmute.topicmanagement.entity.GroupMember;

import java.time.LocalDateTime;

public record GroupMemberResponse(
        Long id,
        Long studentId,
        String studentCode,
        String fullName,
        String email,
        String memberRole,
        LocalDateTime joinedAt
) {
    public static GroupMemberResponse fromEntity(GroupMember gm) {
        return new GroupMemberResponse(
                gm.getId(),
                gm.getStudent().getId(),
                gm.getStudent().getUserCode(),
                gm.getStudent().getFullName(),
                gm.getStudent().getEmail(),
                gm.getRole().name(),
                gm.getJoinedAt()
        );
    }
}
