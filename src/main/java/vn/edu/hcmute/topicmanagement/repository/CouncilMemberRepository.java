package vn.edu.hcmute.topicmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import vn.edu.hcmute.topicmanagement.domain.CouncilMember;
import vn.edu.hcmute.topicmanagement.domain.CouncilRole;
import java.util.List;

public interface CouncilMemberRepository extends JpaRepository<CouncilMember, Long> {
    long countByCouncilId(Long councilId);
    boolean existsByCouncilIdAndLecturerId(Long councilId, Long lecturerId);
    boolean existsByCouncilIdAndRole(Long councilId, CouncilRole role);
    @EntityGraph(attributePaths = "lecturer")
    List<CouncilMember> findByCouncilId(Long councilId);
}
