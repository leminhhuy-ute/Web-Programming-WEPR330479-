package vn.edu.hcmute.topicmanagement.dto.response;

import vn.edu.hcmute.topicmanagement.entity.Council;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record CouncilResponse(
        Long id,
        String code,
        String name,
        Long departmentId,
        String departmentName,
        Long periodId,
        String periodName,
        String status,
        LocalDate defenseDate,
        String defenseTime,
        String room,
        List<CouncilMemberResponse> members,
        LocalDateTime createdAt
) {
    public static CouncilResponse fromEntity(Council c, List<CouncilMemberResponse> members) {
        return new CouncilResponse(
                c.getId(),
                c.getCode(),
                c.getName(),
                c.getDepartment() != null ? c.getDepartment().getId() : null,
                c.getDepartment() != null ? c.getDepartment().getName() : null,
                c.getPeriod() != null ? c.getPeriod().getId() : null,
                c.getPeriod() != null ? c.getPeriod().getName() : null,
                c.getStatus().name(),
                c.getDefenseDate(),
                c.getDefenseTime(),
                c.getRoom(),
                members,
                c.getCreatedAt()
        );
    }
}
