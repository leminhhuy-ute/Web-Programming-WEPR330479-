package topicmanagement.student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ReportRepository extends JpaRepository<Report,Long> {
    List<Report> findByGroupIdOrderBySubmittedAtDesc(Long id);
}
