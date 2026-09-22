package topicmanagement.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TopicRequest {
    private String topicCode;

    @NotBlank(message = "Tên đề tài không được để trống")
    private String title;

    private String description;
    private String requirements;

    @NotNull(message = "Số lượng sinh viên tối đa không được để trống")
    @Min(value = 1, message = "Số lượng sinh viên tối đa phải từ 1 trở lên")
    private Integer maxStudents;

    @NotBlank(message = "Loại đề tài không được để trống (COURSE, NCKH, TLCN, KLTN)")
    private String topicType;

    @NotNull(message = "Bộ môn không được để trống")
    private Long departmentId;

    @NotNull(message = "Đợt đăng ký không được để trống")
    private Long periodId;

    public TopicRequest() {}

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

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public Long getPeriodId() { return periodId; }
    public void setPeriodId(Long periodId) { this.periodId = periodId; }
}
