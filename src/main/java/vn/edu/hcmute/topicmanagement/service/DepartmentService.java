package vn.edu.hcmute.topicmanagement.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmute.topicmanagement.dto.request.DepartmentRequest;
import vn.edu.hcmute.topicmanagement.dto.response.DepartmentResponse;
import vn.edu.hcmute.topicmanagement.entity.Department;
import vn.edu.hcmute.topicmanagement.exception.ConflictException;
import vn.edu.hcmute.topicmanagement.exception.ResourceNotFoundException;
import vn.edu.hcmute.topicmanagement.repository.DepartmentRepository;
import vn.edu.hcmute.topicmanagement.repository.UserRepository;

import java.util.List;

@Service
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public DepartmentService(DepartmentRepository departmentRepository, UserRepository userRepository) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    public List<DepartmentResponse> findAll(String keyword) {
        return departmentRepository.search(keyword == null ? "" : keyword.trim())
                .stream()
                .map(this::map)
                .toList();
    }

    public DepartmentResponse findById(Long id) {
        return map(getEntity(id));
    }

    public DepartmentResponse create(DepartmentRequest r) {
        return save(new Department(), r);
    }

    public DepartmentResponse update(Long id, DepartmentRequest r) {
        return save(getEntity(id), r);
    }

    private DepartmentResponse save(Department department, DepartmentRequest r) {
        String code = r.code().trim();
        boolean exists = department.getId() == null
                ? departmentRepository.existsByCode(code)
                : departmentRepository.existsByCodeAndIdNot(code, department.getId());

        if (exists) {
            throw new ConflictException("Mã bộ môn đã tồn tại.");
        }

        department.setCode(code);
        department.setName(r.name().trim());
        department.setDescription(r.description());
        return map(departmentRepository.save(department));
    }

    public void delete(Long id) {
        if (!userRepository.search("", null, id).isEmpty()) {
            throw new ConflictException("Không thể xóa bộ môn đang được gán cho nhân sự hoặc sinh viên.");
        }
        departmentRepository.delete(getEntity(id));
    }

    private Department getEntity(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bộ môn không tồn tại."));
    }

    public DepartmentResponse map(Department d) {
        return new DepartmentResponse(d.getId(), d.getCode(), d.getName(), d.getDescription());
    }
}
