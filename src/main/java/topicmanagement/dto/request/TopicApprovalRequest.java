package topicmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;

public class TopicApprovalRequest {
    @NotBlank(message = "Trạng thái không được để trống (APPROVED hoặc REJECTED)")
    private String status;

    private String rejectionReason;

    public TopicApprovalRequest() {}

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}
