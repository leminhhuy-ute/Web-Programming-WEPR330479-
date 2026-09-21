package vn.edu.hcmute.topicmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.hcmute.topicmanagement.entity.Department;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByCode(String code);
    boolean existsByCode(String code);
    boolean existsByCodeIgnoreCase(String code);
    boolean existsByCodeAndIdNot(String code, Long id);

    @org.springframework.data.jpa.repository.Query(
            "SELECT d FROM Department d WHERE :keyword = '' OR " +
            "LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.code) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY d.id DESC")
    List<Department> search(@org.springframework.data.repository.query.Param("keyword") String keyword);
}
