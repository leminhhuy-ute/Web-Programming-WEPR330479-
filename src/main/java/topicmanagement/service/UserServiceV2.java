package topicmanagement.service;

import java.util.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import topicmanagement.dto.request.UserRequest;
import topicmanagement.dto.response.UserResponse;
import topicmanagement.entity.*;
import topicmanagement.enums.*;
import topicmanagement.exception.*;
import topicmanagement.repository.*;

@Service
@Transactional
public class UserServiceV2 {
  private final UserRepository users;
  private final DepartmentRepository departments;
  private final PasswordEncoder passwords;

  public UserServiceV2(UserRepository u, DepartmentRepository d, PasswordEncoder p) {
    users = u;
    departments = d;
    passwords = p;
  }

  public List<UserResponse> findAll(String keyword, Role role, Long departmentId) {
    return users.search(n(keyword), role, departmentId).stream().map(this::map).toList();
  }

  public UserResponse findById(Long id) {
    return map(entity(id));
  }

  public UserResponse create(UserRequest r) {
    User u = new User();
    apply(u, r, true);
    return map(users.save(u));
  }

  public UserResponse update(Long id, UserRequest r) {
    User u = entity(id);
    ensureActiveDeanRemains(u, r.role(), r.status());
    apply(u, r, false);
    return map(users.save(u));
  }

  public UserResponse setStatus(Long id, UserStatus status) {
    if (status == null) throw new IllegalArgumentException("Trạng thái không hợp lệ.");
    User u = entity(id);
    ensureActiveDeanRemains(u, u.getRole(), status);
    u.setStatus(status);
    return map(users.save(u));
  }

  public void delete(Long id, Long currentUserId) {
    if (id.equals(currentUserId)) {
      throw new IllegalArgumentException("Không thể xóa chính tài khoản đang đăng nhập.");
    }
    User user = entity(id);
    ensureActiveDeanRemains(user, null, null);
    users.delete(user);
  }

  private void ensureActiveDeanRemains(User user, Role nextRole, UserStatus nextStatus) {
    if (user.getId() == null || user.getRole() != Role.DEAN || user.getStatus() != UserStatus.ACTIVE) {
      return;
    }
    boolean remainsActiveDean = nextRole == Role.DEAN && nextStatus == UserStatus.ACTIVE;
    if (!remainsActiveDean && users.countByRoleAndStatus(Role.DEAN, UserStatus.ACTIVE) <= 1) {
      throw new ConflictException("Hệ thống phải còn ít nhất một tài khoản Trưởng khoa đang hoạt động.");
    }
  }

  private void apply(User u, UserRequest r, boolean create) {
    String code = r.userCode().trim(), username = r.username().trim(), email = r.email().trim();
    if (duplicateCode(code, u.getId())) throw new ConflictException("Mã người dùng đã tồn tại.");
    if (duplicateUsername(username, u.getId()))
      throw new ConflictException("Tên đăng nhập đã tồn tại.");
    if (duplicateEmail(email, u.getId())) throw new ConflictException("Email đã được sử dụng.");
    u.setUserCode(code);
    u.setUsername(username);
    u.setFullName(r.fullName().trim());
    u.setEmail(email);
    u.setRole(r.role());
    u.setStatus(r.status());
    u.setDepartment(
        r.departmentId() == null
            ? null
            : departments
                .findById(r.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Bộ môn đã chọn không tồn tại.")));
    if (r.password() != null && !r.password().isBlank())
      u.setPasswordHash(passwords.encode(r.password()));
    else if (create)
      throw new IllegalArgumentException("Vui lòng nhập mật khẩu cho tài khoản mới.");
  }

  private boolean duplicateCode(String v, Long id) {
    return id == null ? users.existsByUserCode(v) : users.existsByUserCodeAndIdNot(v, id);
  }

  private boolean duplicateUsername(String v, Long id) {
    return id == null ? users.existsByUsername(v) : users.existsByUsernameAndIdNot(v, id);
  }

  private boolean duplicateEmail(String v, Long id) {
    return id == null ? users.existsByEmail(v) : users.existsByEmailAndIdNot(v, id);
  }

  private User entity(Long id) {
    return users
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tồn tại."));
  }

  private String n(String v) {
    return v == null ? "" : v.trim();
  }

  public UserResponse map(User u) {
    Department d = u.getDepartment();
    return new UserResponse(
        u.getId(),
        u.getUserCode(),
        u.getUsername(),
        u.getFullName(),
        u.getEmail(),
        u.getRole().name(),
        d == null ? null : d.getId(),
        d == null ? null : d.getName(),
        u.getStatus().name(),
        u.getCreatedAt(),
        u.getUpdatedAt());
  }
}
