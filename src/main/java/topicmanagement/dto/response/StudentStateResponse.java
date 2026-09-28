package topicmanagement.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record StudentStateResponse(
        StudentPerson student,
        StudentGroupView group,
        StudentRegistrationView registration,
        List<TopicRegistrationResponse> registrationHistory,
        List<ReportSummaryResponse> reports,
        List<StudentInvitation> invitations) {

    public record StudentPerson(String id, String name, String className) {}

    public record StudentInvitation(Long id, Long groupId, String groupName, String studentId,
            String studentName, String status, LocalDateTime createdAt) {}

    public record StudentTopic(String id, String title, String description, String technologies,
            String supervisor, String department, String type, int capacity,
            LocalDateTime opensAt, LocalDateTime closesAt, LocalDateTime reportDueAt) {}

    public record StudentGroupView(Long id, String name, String leaderId, int maxMembers,
            List<StudentPerson> members, List<StudentInvitation> invitations) {}

    public record StudentRegistrationView(Long id, String status, LocalDateTime submittedAt,
            String feedback, StudentTopic topic) {}

    public record CatalogItem(StudentTopic topic, long registeredGroups, boolean open) {}
}
