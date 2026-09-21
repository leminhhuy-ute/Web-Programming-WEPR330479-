package vn.edu.hcmute.topicmanagement.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmute.topicmanagement.dto.request.UserRequest;
import vn.edu.hcmute.topicmanagement.dto.response.UserResponse;
import vn.edu.hcmute.topicmanagement.entity.Department;
import vn.edu.hcmute.topicmanagement.entity.User;
import vn.edu.hcmute.topicmanagement.enums.Role;
import vn.edu.hcmute.topicmanagement.enums.UserStatus;
import vn.edu.hcmute.topicmanagement.exception.ConflictException;
import vn.edu.hcmute.topicmanagement.exception.ResourceNotFoundException;
import vn.edu.hcmute.topicmanagement.repository.DepartmentRepository;
import vn.edu.hcmute.topicmanagement.repository.UserRepository;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       DepartmentRepository departmentRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> findAll(String keyword, Role role, Long departmentId) {
        return userRepository.search(keyword == null ? "" : keyword.trim(), role, departmentId)
                .stream()
                .map(this::map)
                .toList();
    }

    public UserResponse findById(Long id) {
        return map(getEntity(id));
    }

    public UserResponse create(UserRequest r) {
        User user = new User();
        apply(user, r, true);
        return map(userRepository.save(user));
    }

    public UserResponse update(Long id, UserRequest r) {
        User user = getEntity(id);
        ensureActiveDeanRemains(user, r.role(), r.status());
        apply(user, r, false);
        return map(userRepository.save(user));
    }

    public UserResponse setStatus(Long id, UserStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ.");
        }
        User user = getEntity(id);
        ensureActiveDeanRemains(user, user.getRole(), status);
        user.setStatus(status);
        return map(userRepository.save(user));
    }

    public void delete(Long id, Long currentUserId) {
        if (id.equals(currentUserId)) {
            throw new IllegalArgumentException("Không thể xóa chính tài khoản đang đăng nhập.");
        }
        User user = getEntity(id);
        ensureActiveDeanRemains(user, null, null);
        userRepository.delete(user);
    }

    private void ensureActiveDeanRemains(User user, Role nextRole, UserStatus nextStatus) {
        if (user.getId() == null || user.getRole() != Role.DEAN || user.getStatus() != UserStatus.ACTIVE) {
            return;
        }
        boolean remainsActiveDean = nextRole == Role.DEAN && nextStatus == UserStatus.ACTIVE;
        if (!remainsActiveDean && userRepository.countByRoleAndStatus(Role.DEAN, UserStatus.ACTIVE) <= 1) {
            throw new ConflictException("Hệ thống phải còn ít nhất một tài khoản Trưởng khoa đang hoạt động.");
        }
    }

    private void apply(User user, UserRequest r, boolean create) {
        String code = r.userCode().trim();
        String username = r.username().trim();
        String email = r.email().trim();

        if (duplicateCode(code, user.getId())) {
            throw new ConflictException("Mã người dùng đã tồn tại.");
        }
        if (duplicateUsername(username, user.getId())) {
            throw new ConflictException("Tên đăng nhập đã tồn tại.");
        }
        if (duplicateEmail(email, user.getId())) {
            throw new ConflictException("Email đã được sử dụng.");
        }

        user.setUserCode(code);
        user.setUsername(username);
        user.setFullName(r.fullName().trim());
        user.setEmail(email);
        user.setRole(r.role());
        user.setStatus(r.status() != null ? r.status() : UserStatus.ACTIVE);

        if (r.departmentId() != null) {
            Department dept = departmentRepository.findById(r.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bộ môn đã chọn không tồn tại."));
            user.setDepartment(dept);
        } else {
            user.setDepartment(null);
        }

        if (r.password() != null && !r.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(r.password()));
        } else if (create) {
            throw new IllegalArgumentException("Vui lòng nhập mật khẩu cho tài khoản mới.");
        }
    }

    private boolean duplicateCode(String code, Long id) {
        return id == null ? userRepository.existsByUserCode(code) : userRepository.existsByUserCodeAndIdNot(code, id);
    }

    private boolean duplicateUsername(String username, Long id) {
        return id == null ? userRepository.existsByUsername(username) : userRepository.existsByUsernameAndIdNot(username, id);
    }

    private boolean duplicateEmail(String email, Long id) {
        return id == null ? userRepository.existsByEmail(email) : userRepository.existsByEmailAndIdNot(email, id);
    }

    private User getEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tồn tại."));
    }

    public UserResponse map(User user) {
        Department dept = user.getDepartment();
        return new UserResponse(
                user.getId(),
                user.getUserCode(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name(),
                dept == null ? null : dept.getId(),
                dept == null ? null : dept.getName(),
                user.getStatus().name(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
