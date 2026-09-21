package vn.edu.hcmute.topicmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.hcmute.topicmanagement.entity.Council;
import vn.edu.hcmute.topicmanagement.entity.CouncilMember;
import vn.edu.hcmute.topicmanagement.entity.User;
import vn.edu.hcmute.topicmanagement.enums.CouncilRole;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouncilMemberRepository extends JpaRepository<CouncilMember, Long> {
    List<CouncilMember> findByCouncil(Council council);
    List<CouncilMember> findByLecturer(User lecturer);
    Optional<CouncilMember> findByCouncilAndLecturer(Council council, User lecturer);
    List<CouncilMember> findByCouncilAndMemberRole(Council council, CouncilRole memberRole);
}
