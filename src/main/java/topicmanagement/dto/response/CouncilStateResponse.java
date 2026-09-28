package topicmanagement.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CouncilStateResponse(
        String role,
        List<CouncilView> councils,
        List<DefenseView> defenses,
        List<SelectOption> lecturers,
        List<SelectOption> groups) {
    public record CouncilMemberView(Long id, String name, String role) {}
    public record CouncilView(Long id, String code, String name, LocalDateTime date, String room,
            List<CouncilMemberView> members) {}
    public record GradeView(String name, BigDecimal score, String comment) {}
    public record DefenseView(Long id, String groupName, String topic, String council, String reviewer,
            boolean finalized, boolean published, BigDecimal score, boolean canGrade, boolean canFinalize,
            List<GradeView> grades, List<ReportSummaryResponse> reports) {}
    public record SelectOption(Long id, String name) {}
}
