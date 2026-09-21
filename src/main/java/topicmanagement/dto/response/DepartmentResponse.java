package topicmanagement.dto.response;

import topicmanagement.entity.Department;

public class DepartmentResponse {
    private Long id;
    private String code;
    private String name;
    private String description;

    public DepartmentResponse() {}

    public DepartmentResponse(Department d) {
        if (d != null) {
            this.id = d.getId();
            this.code = d.getCode();
            this.name = d.getName();
            this.description = d.getDescription();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
