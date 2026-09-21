package vn.edu.hcmute.topicmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.hcmute.topicmanagement.entity.GroupInvitation;
import vn.edu.hcmute.topicmanagement.entity.StudentGroup;
import vn.edu.hcmute.topicmanagement.entity.User;
import vn.edu.hcmute.topicmanagement.enums.InvitationStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupInvitationRepository extends JpaRepository<GroupInvitation, Long> {
    List<GroupInvitation> findByInviteeAndStatus(User invitee, InvitationStatus status);
    List<GroupInvitation> findByGroupOrderByCreatedAtDesc(StudentGroup group);
    List<GroupInvitation> findByInviteeOrderByCreatedAtDesc(User invitee);
    Optional<GroupInvitation> findByGroupAndInviteeAndStatus(StudentGroup group, User invitee, InvitationStatus status);
}
