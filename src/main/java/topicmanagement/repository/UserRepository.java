package topicmanagement.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import topicmanagement.entity.User;
import topicmanagement.enums.Role;
import topicmanagement.enums.UserStatus;

public interface UserRepository extends JpaRepository<User, Long> {
  @Override
  @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "department")
  Optional<User> findById(Long id);

  Optional<User> findByUsername(String username);

  Optional<User> findByUsernameIgnoreCase(String username);

  Optional<User> findByEmailIgnoreCase(String email);

  Optional<User> findByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);

  @Query(
      "select u from User u left join fetch u.department "
          + "where (:keyword='' "
          + "or lower(u.fullName) like lower(concat('%',:keyword,'%')) "
          + "or lower(u.username) like lower(concat('%',:keyword,'%')) "
          + "or lower(u.email) like lower(concat('%',:keyword,'%')) "
          + "or lower(u.userCode) like lower(concat('%',:keyword,'%'))) "
          + "and (:role is null or u.role=:role) "
          + "and (:departmentId is null or u.department.id=:departmentId) "
          + "order by u.id desc")
  List<User> search(
      @Param("keyword") String keyword,
      @Param("role") Role role,
      @Param("departmentId") Long departmentId);

  @EntityGraph(attributePaths = "department")
  @Query(
      "select u from User u "
          + "where (:keyword='' "
          + "or lower(u.fullName) like lower(concat('%',:keyword,'%')) "
          + "or lower(u.username) like lower(concat('%',:keyword,'%')) "
          + "or lower(u.email) like lower(concat('%',:keyword,'%')) "
          + "or lower(u.userCode) like lower(concat('%',:keyword,'%'))) "
          + "and (:role is null or u.role=:role) "
          + "and (:departmentId is null or u.department.id=:departmentId)")
  Page<User> search(
      @Param("keyword") String keyword,
      @Param("role") Role role,
      @Param("departmentId") Long departmentId,
      Pageable pageable);

  boolean existsByUsername(String username);

  boolean existsByUsernameIgnoreCase(String username);

  boolean existsByEmail(String email);

  boolean existsByEmailIgnoreCase(String email);

  boolean existsByUserCode(String userCode);

  boolean existsByUsernameAndIdNot(String username, Long id);

  boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);

  boolean existsByEmailAndIdNot(String email, Long id);

  boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

  boolean existsByUserCodeAndIdNot(String userCode, Long id);

  long countByRoleAndStatus(Role role, UserStatus status);

  List<User> findByRole(Role role);

  List<User> findByRoleIn(List<Role> roles);

  List<User> findByDepartmentIdAndRoleIn(Long departmentId, List<Role> roles);
}
