package vn.edu.hcmute.topicmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.hcmute.topicmanagement.entity.GroupMember;
import vn.edu.hcmute.topicmanagement.entity.StudentGroup;
import vn.edu.hcmute.topicmanagement.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByGroup(StudentGroup group);
    Optional<GroupMember> findByStudent(User student);
    Optional<GroupMember> findByGroupAndStudent(StudentGroup group, User student);
    boolean existsByStudent(User student);
    void deleteByGroupAndStudent(StudentGroup group, User student);
}
