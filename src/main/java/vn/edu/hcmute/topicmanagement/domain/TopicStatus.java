package vn.edu.hcmute.topicmanagement.domain;

public enum TopicStatus {
    WAITING_ASSIGNMENT("Chờ phân công"), ASSIGNED("Đã phân công"), GRADING("Đang chấm"), GRADED("Đã có kết quả");
    private final String label;
    TopicStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}
