package topicmanagement.dto.response;

public record AdminDashboardResponse(long users, long activePeriods, long pendingTopics,
        long approvedTopics, long approvedGroups, long councils, long publishedResults) {}
