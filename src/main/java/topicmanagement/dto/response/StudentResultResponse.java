package topicmanagement.dto.response;

import java.math.BigDecimal;
import java.util.List;
import topicmanagement.dto.response.CouncilStateResponse.GradeView;

public record StudentResultResponse(boolean published, BigDecimal score, String topic, List<GradeView> grades) {
    public static StudentResultResponse unpublished() {
        return new StudentResultResponse(false, null, null, List.of());
    }
}
