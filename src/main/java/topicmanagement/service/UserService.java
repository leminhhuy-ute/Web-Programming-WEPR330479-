package topicmanagement.service;

import java.util.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import topicmanagement.dto.request.UserRequest;
import topicmanagement.dto.response.UserResponse;
import topicmanagement.dto.response.PageResponse;
import topicmanagement.entity.*;
import topicmanagement.enums.*;
import topicmanagement.exception.*;
import topicmanagement.repository.*;

@Service
@Transactional
public class UserService {
    private final UserRepository users;
    private final DepartmentRepository departments;
    private final PasswordEncoder passwords;

    public UserService(UserRepository users, DepartmentRepository departments, PasswordEncoder passwords) {
        this.users = users;
        this.departments = departments;
        this.passwords = passwords;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll(String keyword, Role role, Long departmentId) {
        return users.search(normalize(keyword), role, departmentId).stream().map(this::map).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> findPage(String keyword, Role role, Long departmentId, int page, int size) {
        var pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100),
            Sort.by(Sort.Direction.DESC, "id"));
        return PageResponse.from(users.search(normalize(keyword), role, departmentId, pageable).map(this::map));
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) { return map(entity(id)); }

    @Transactional(readOnly = true)
    public List<UserResponse> getLecturersByDepartment(Long departmentId) {
        List<User> lecturers = departmentId == null
            ? users.findByRoleIn(List.of(Role.LECTURER, Role.HEAD_OF_DEPT))
            : users.findByDepartmentIdAndRoleIn(departmentId, List.of(Role.LECTURER, Role.HEAD_OF_DEPT));
        return lecturers.stream().map(this::map).toList();
    }

    public UserResponse create(UserRequest request) {
        User user = new User();
        apply(user, request, true);
        return map(users.save(user));
    }

    public UserResponse update(Long id, UserRequest request) {
        User user = entity(id);
        ensureActiveDeanRemains(user, request.role(), request.status());
        apply(user, request, false);
        return map(users.save(user));
    }

    public UserResponse setStatus(Long id, UserStatus status) {
        if (status == null) throw new IllegalArgumentException("Trạng thái không hợp lệ.");
        User user = entity(id);
        ensureActiveDeanRemains(user, user.getRole(), status);
        user.setStatus(status);
        return map(users.save(user));
    }

    public void delete(Long id, Long currentUserId) {
        if (id.equals(currentUserId)) throw new IllegalArgumentException("Không thể xóa chính tài khoản đang đăng nhập.");
        User user = entity(id);
        ensureActiveDeanRemains(user, null, null);
        users.delete(user);
    }

    private void ensureActiveDeanRemains(User user, Role nextRole, UserStatus nextStatus) {
        if (user.getId() == null || user.getRole() != Role.DEAN || user.getStatus() != UserStatus.ACTIVE) return;
        boolean remainsActiveDean = nextRole == Role.DEAN && nextStatus == UserStatus.ACTIVE;
        if (!remainsActiveDean && users.countByRoleAndStatus(Role.DEAN, UserStatus.ACTIVE) <= 1)
            throw new ConflictException("Hệ thống phải còn ít nhất một tài khoản Trưởng khoa đang hoạt động.");
    }

    private void apply(User user, UserRequest request, boolean create) {
        String code = request.userCode().trim();
        String username = request.username().trim();
        String email = request.email().trim();
        if (duplicateCode(code, user.getId())) throw new ConflictException("Mã người dùng đã tồn tại.");
        if (duplicateUsername(username, user.getId())) throw new ConflictException("Tên đăng nhập đã tồn tại.");
        if (duplicateEmail(email, user.getId())) throw new ConflictException("Email đã được sử dụng.");
        if ((request.role() == Role.LECTURER || request.role() == Role.HEAD_OF_DEPT) && request.departmentId() == null) {
            throw new IllegalArgumentException("Giảng viên và Trưởng bộ môn bắt buộc phải thuộc một bộ môn.");
        }
        user.setUserCode(code);
        user.setUsername(username);
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setRole(request.role());
        user.setStatus(request.status());
        user.setDepartment(request.departmentId() == null ? null : departments.findById(request.departmentId())
            .orElseThrow(() -> new ResourceNotFoundException("Bộ môn đã chọn không tồn tại.")));
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwords.encode(request.password()));
            user.setPasswordChangedAt(java.time.LocalDateTime.now());
        } else if (create) {
            throw new IllegalArgumentException("Vui lòng nhập mật khẩu cho tài khoản mới.");
        }
    }

    private boolean duplicateCode(String value, Long id) {
        return id == null ? users.existsByUserCode(value) : users.existsByUserCodeAndIdNot(value, id);
    }
    private boolean duplicateUsername(String value, Long id) {
        return id == null ? users.existsByUsernameIgnoreCase(value) : users.existsByUsernameIgnoreCaseAndIdNot(value, id);
    }
    private boolean duplicateEmail(String value, Long id) {
        return id == null ? users.existsByEmailIgnoreCase(value) : users.existsByEmailIgnoreCaseAndIdNot(value, id);
    }
    private User entity(Long id) {
        return users.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tồn tại."));
    }
    private String normalize(String value) { return value == null ? "" : value.trim(); }

    public UserResponse map(User user) {
        Department department = user.getDepartment();
        return new UserResponse(user.getId(), user.getUserCode(), user.getUsername(), user.getFullName(),
            user.getEmail(), user.getRole().name(), department == null ? null : department.getId(),
            department == null ? null : department.getName(), user.getStatus().name(),
            user.getCreatedAt(), user.getUpdatedAt());
    }
}
