package vn.edu.hcmute.topicmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.hcmute.topicmanagement.entity.Department;
import vn.edu.hcmute.topicmanagement.entity.User;
import vn.edu.hcmute.topicmanagement.enums.Role;
import vn.edu.hcmute.topicmanagement.enums.UserStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByUserCode(String userCode);

    @Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:login) OR LOWER(u.email) = LOWER(:login)")
    Optional<User> findByUsernameOrEmail(@Param("login") String login);

    @Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:login) OR LOWER(u.email) = LOWER(:login) OR LOWER(u.userCode) = LOWER(:login)")
    Optional<User> findByLoginIdentifier(@Param("login") String login);

    List<User> findByRole(Role role);
    List<User> findByRoleAndStatus(Role role, UserStatus status);
    List<User> findByDepartment(Department department);
    List<User> findByDepartmentAndRole(Department department, Role role);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.department WHERE (:keyword = '' OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.userCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:departmentId IS NULL OR u.department.id = :departmentId) " +
           "ORDER BY u.id DESC")
    List<User> search(@Param("keyword") String keyword,
                      @Param("role") Role role,
                      @Param("departmentId") Long departmentId);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUserCode(String userCode);

    boolean existsByUsernameAndIdNot(String username, Long id);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByUserCodeAndIdNot(String userCode, Long id);

    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUserCodeIgnoreCase(String userCode);

    long countByRoleAndStatus(Role role, UserStatus status);

    @Query("SELECT u FROM User u WHERE u.role = 'STUDENT' AND " +
           "NOT EXISTS (SELECT gm FROM GroupMember gm WHERE gm.student = u) AND " +
           "(LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(u.userCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<User> searchAvailableStudents(@Param("keyword") String keyword);
}
