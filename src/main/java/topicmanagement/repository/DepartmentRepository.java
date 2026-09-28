package topicmanagement.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import topicmanagement.entity.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
  boolean existsByCode(String code);

  boolean existsByCodeAndIdNot(String code, Long id);

  @Query(
      "select d from Department d "
          + "where :keyword='' "
          + "or lower(d.name) like lower(concat('%',:keyword,'%')) "
          + "or lower(d.code) like lower(concat('%',:keyword,'%')) "
          + "order by d.id desc")
  List<Department> search(@Param("keyword") String keyword);
}
