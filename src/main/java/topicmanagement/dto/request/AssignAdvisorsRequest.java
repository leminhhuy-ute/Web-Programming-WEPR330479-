package topicmanagement.dto.request;

import jakarta.validation.constraints.NotNull;

public class AssignAdvisorsRequest {
    @NotNull(message = "Giảng viên hướng dẫn 1 không được để trống")
    private Long advisor1Id;

    private Long advisor2Id;

    public AssignAdvisorsRequest() {}

    public Long getAdvisor1Id() { return advisor1Id; }
    public void setAdvisor1Id(Long advisor1Id) { this.advisor1Id = advisor1Id; }

    public Long getAdvisor2Id() { return advisor2Id; }
    public void setAdvisor2Id(Long advisor2Id) { this.advisor2Id = advisor2Id; }
}
