package vn.edu.hcmute.topicmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.hcmute.topicmanagement.domain.Lecturer;
import java.util.List;
import java.util.Optional;

public interface LecturerRepository extends JpaRepository<Lecturer, Long> {
    List<Lecturer> findAllByOrderByFullNameAsc();
    List<Lecturer> findByActiveTrueOrderByFullNameAsc();
    Optional<Lecturer> findByCode(String code);
    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
