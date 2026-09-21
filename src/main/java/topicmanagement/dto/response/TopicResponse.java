package topicmanagement.dto.response;

import java.time.LocalDateTime;
import topicmanagement.entity.Topic;

public class TopicResponse {
    private Long id;
    private String topicCode;
    private String title;
    private String description;
    private String requirements;
    private Integer maxStudents;
    private String topicType;
    private String status;
    private String rejectionReason;
    private DepartmentResponse department;
    private RegistrationPeriodResponse period;
    private UserResponse createdBy;
    private UserResponse advisor1;
    private UserResponse advisor2;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TopicResponse() {}

    public TopicResponse(Topic topic) {
        if (topic != null) {
            this.id = topic.getId();
            this.topicCode = topic.getTopicCode();
            this.title = topic.getTitle();
            this.description = topic.getDescription();
            this.requirements = topic.getRequirements();
            this.maxStudents = topic.getMaxStudents();
            this.topicType = topic.getTopicType() != null ? topic.getTopicType().name() : null;
            this.status = topic.getStatus() != null ? topic.getStatus().name() : null;
            this.rejectionReason = topic.getRejectionReason();
            if (topic.getDepartment() != null) this.department = new DepartmentResponse(topic.getDepartment());
            if (topic.getPeriod() != null) this.period = new RegistrationPeriodResponse(topic.getPeriod());
            if (topic.getCreatedBy() != null) this.createdBy = new UserResponse(topic.getCreatedBy());
            if (topic.getAdvisor1() != null) this.advisor1 = new UserResponse(topic.getAdvisor1());
            if (topic.getAdvisor2() != null) this.advisor2 = new UserResponse(topic.getAdvisor2());
            this.createdAt = topic.getCreatedAt();
            this.updatedAt = topic.getUpdatedAt();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTopicCode() { return topicCode; }
    public void setTopicCode(String topicCode) { this.topicCode = topicCode; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }

    public Integer getMaxStudents() { return maxStudents; }
    public void setMaxStudents(Integer maxStudents) { this.maxStudents = maxStudents; }

    public String getTopicType() { return topicType; }
    public void setTopicType(String topicType) { this.topicType = topicType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public DepartmentResponse getDepartment() { return department; }
    public void setDepartment(DepartmentResponse department) { this.department = department; }

    public RegistrationPeriodResponse getPeriod() { return period; }
    public void setPeriod(RegistrationPeriodResponse period) { this.period = period; }

    public UserResponse getCreatedBy() { return createdBy; }
    public void setCreatedBy(UserResponse createdBy) { this.createdBy = createdBy; }

    public UserResponse getAdvisor1() { return advisor1; }
    public void setAdvisor1(UserResponse advisor1) { this.advisor1 = advisor1; }

    public UserResponse getAdvisor2() { return advisor2; }
    public void setAdvisor2(UserResponse advisor2) { this.advisor2 = advisor2; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
