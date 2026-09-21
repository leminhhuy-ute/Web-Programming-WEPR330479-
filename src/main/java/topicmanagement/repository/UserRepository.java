package topicmanagement.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import topicmanagement.entity.User;
import topicmanagement.enums.Role;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmailIgnoreCase(String email);
    
    @Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:identifier) OR LOWER(u.email) = LOWER(:identifier)")
    Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);
    
    List<User> findByRole(Role role);
    List<User> findByDepartmentIdAndRoleIn(Long departmentId, List<Role> roles);
    
    boolean existsByUsername(String username);
    boolean existsByUserCode(String userCode);
    boolean existsByEmail(String email);
}
