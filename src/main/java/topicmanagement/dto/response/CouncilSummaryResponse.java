package topicmanagement.dto.response;

import java.time.LocalDateTime;
import topicmanagement.council.CouncilStatus;

public record CouncilSummaryResponse(Long id, String code, String name, LocalDateTime defenseDate,
        String room, CouncilStatus status, long memberCount) {}
