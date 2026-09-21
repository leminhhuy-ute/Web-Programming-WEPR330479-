package topicmanagement.service;

import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import topicmanagement.dto.request.DepartmentRequest;
import topicmanagement.dto.response.DepartmentResponse;
import topicmanagement.entity.Department;
import topicmanagement.exception.*;
import topicmanagement.repository.*;

@Service
@Transactional
public class DepartmentServiceV2 {
  private final DepartmentRepository departments;
  private final UserRepository users;

  public DepartmentServiceV2(DepartmentRepository d, UserRepository u) {
    departments = d;
    users = u;
  }

  public List<DepartmentResponse> findAll(String q) {
    return departments.search(q == null ? "" : q.trim()).stream().map(this::map).toList();
  }

  public DepartmentResponse findById(Long id) {
    return map(entity(id));
  }

  public DepartmentResponse create(DepartmentRequest r) {
    return save(new Department(), r);
  }

  public DepartmentResponse update(Long id, DepartmentRequest r) {
    return save(entity(id), r);
  }

  private DepartmentResponse save(Department d, DepartmentRequest r) {
    String code = r.code().trim();
    boolean exists =
        d.getId() == null
            ? departments.existsByCode(code)
            : departments.existsByCodeAndIdNot(code, d.getId());
    if (exists) throw new ConflictException("Mã bộ môn đã tồn tại.");
    d.setCode(code);
    d.setName(r.name().trim());
    d.setDescription(r.description());
    return map(departments.save(d));
  }

  public void delete(Long id) {
    if (users.search("", null, id).size() > 0)
      throw new ConflictException("Không thể xóa bộ môn đang được sử dụng.");
    departments.delete(entity(id));
  }

  private Department entity(Long id) {
    return departments
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Bộ môn không tồn tại."));
  }

  private DepartmentResponse map(Department d) {
    return new DepartmentResponse(d.getId(), d.getCode(), d.getName(), d.getDescription());
  }
}
