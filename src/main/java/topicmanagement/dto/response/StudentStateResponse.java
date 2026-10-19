package topicmanagement.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record StudentStateResponse(
        StudentPerson student,
        StudentGroupView group,
        List<GroupChoice> groups,
        StudentRegistrationView registration,
        List<TopicRegistrationResponse> registrationHistory,
        List<ReportSummaryResponse> reports,
        List<StudentInvitation> invitations,
        AdvisorGuidanceView guidance,
        DefenseScheduleView defense) {

    public record AdvisorGuidanceView(String tasks, String requirements, boolean completed) {}

    public record DefenseScheduleView(String reviewer, String council, LocalDateTime defenseDate, String room) {}

    public record StudentPerson(String id, String name, String className) {}

    public record StudentInvitation(Long id, Long groupId, String groupName, String studentId,
            String studentName, String status, LocalDateTime createdAt) {}

    public record StudentTopic(String id, String title, String description, String technologies,
            String supervisor, String department, String type, int capacity,
            LocalDateTime opensAt, LocalDateTime closesAt, LocalDateTime reportDueAt,
            Long periodId, String periodName) {}

    public record StudentGroupView(Long id, String name, String leaderId, int maxMembers,
            List<StudentPerson> members, List<StudentInvitation> invitations, Long periodId, String periodName) {}

    public record GroupChoice(Long id, String name, Long periodId, String periodName,
            String status, boolean leader) {}

    public record PeriodChoice(Long id, String name, String type, boolean open, boolean joined) {}

    public record StudentRegistrationView(Long id, String status, LocalDateTime submittedAt,
            String feedback, StudentTopic topic) {}

    public record CatalogItem(StudentTopic topic, long registeredGroups, boolean open) {}
}
