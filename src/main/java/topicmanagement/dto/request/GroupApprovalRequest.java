package topicmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;

public class GroupApprovalRequest {
    @NotBlank(message = "Trạng thái phê duyệt không được để trống (APPROVED hoặc REJECTED)")
    private String status;

    private String notes;

    public GroupApprovalRequest() {}

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
