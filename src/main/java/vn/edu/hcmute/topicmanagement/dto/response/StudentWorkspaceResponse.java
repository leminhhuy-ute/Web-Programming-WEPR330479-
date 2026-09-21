package vn.edu.hcmute.topicmanagement.dto.response;

import java.util.List;

public record StudentWorkspaceResponse(
        UserResponse student,
        StudentGroupResponse group,
        TopicRegistrationResponse registration,
        List<InvitationResponse> pendingInvitations,
        List<ProgressReportResponse> reports
) {}
