package topicmanagement.dto.response;

import topicmanagement.entity.Department;

public record DepartmentResponse(Long id, String code, String name, String description) {
    public DepartmentResponse(Department d) {
        this(d.getId(), d.getCode(), d.getName(), d.getDescription());
    }
}
