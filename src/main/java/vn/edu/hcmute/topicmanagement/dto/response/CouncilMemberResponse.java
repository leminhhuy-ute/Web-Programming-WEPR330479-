package vn.edu.hcmute.topicmanagement.dto.response;

import vn.edu.hcmute.topicmanagement.entity.CouncilMember;

import java.time.LocalDateTime;

public record CouncilMemberResponse(
        Long id,
        Long councilId,
        Long lecturerId,
        String lecturerCode,
        String fullName,
        String email,
        String role,
        LocalDateTime joinedAt
) {
    public static CouncilMemberResponse fromEntity(CouncilMember cm) {
        return new CouncilMemberResponse(
                cm.getId(),
                cm.getCouncil().getId(),
                cm.getLecturer().getId(),
                cm.getLecturer().getUserCode(),
                cm.getLecturer().getFullName(),
                cm.getLecturer().getEmail(),
                cm.getMemberRole().name(),
                cm.getJoinedAt()
        );
    }
}
