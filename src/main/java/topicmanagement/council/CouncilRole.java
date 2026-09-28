package topicmanagement.council;
import topicmanagement.entity.User;

public enum CouncilRole {
    CHAIRPERSON("Chủ tịch"), SECRETARY("Thư ký"), REVIEWER("Ủy viên phản biện"), MEMBER("Ủy viên");
    private final String label;
    CouncilRole(String label) { this.label = label; }
    public String getLabel() { return label; }
}
