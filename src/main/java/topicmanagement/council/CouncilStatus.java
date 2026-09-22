package topicmanagement.council;
import topicmanagement.entity.User;

public enum CouncilStatus {
    DRAFT("Bản nháp"), READY("Sẵn sàng"), COMPLETED("Đã hoàn tất");
    private final String label;
    CouncilStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}
