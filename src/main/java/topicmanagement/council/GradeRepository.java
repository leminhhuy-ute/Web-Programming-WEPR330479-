package topicmanagement.council;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface GradeRepository extends JpaRepository<Grade,Long> {
    List<Grade> findByDefenseId(Long id);
    Optional<Grade> findByDefenseIdAndEvaluatorId(Long defenseId,Long evaluatorId);
}
