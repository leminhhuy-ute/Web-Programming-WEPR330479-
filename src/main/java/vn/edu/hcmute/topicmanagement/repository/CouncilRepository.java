package vn.edu.hcmute.topicmanagement.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.hcmute.topicmanagement.domain.Council;
import java.util.List;
import java.util.Optional;

public interface CouncilRepository extends JpaRepository<Council, Long> {
    @EntityGraph(attributePaths = {"members", "members.lecturer"})
    List<Council> findAllByOrderByDefenseDateDesc();
    @EntityGraph(attributePaths = {"members", "members.lecturer"})
    @Query("select c from Council c where c.id = :id")
    Optional<Council> findWithMembersById(Long id);
}
