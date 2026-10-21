package topicmanagement.council;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
public interface DefenseRepository extends JpaRepository<Defense,Long> {
    @EntityGraph(attributePaths = {"group", "group.topic", "group.topic.period", "group.topic.advisor1",
        "group.topic.advisor2", "reviewer"})
    List<Defense> findByCouncilId(Long councilId);
    @EntityGraph(attributePaths={"group","group.topic","group.topic.period","topic","topic.period","council"})
    @Query("select d from Defense d join d.group g left join d.registration r left join d.topic t left join g.topic gt where d.published=true and d.finalized=true and d.finalScore is not null and (r is null or r.status=topicmanagement.enums.RegistrationStatus.APPROVED) and (:periodId is null or coalesce(t.period.id,gt.period.id)=:periodId) order by d.id")
    List<Defense> findPublished(Long periodId);
    Optional<Defense> findByGroupId(Long groupId);
    Optional<Defense> findByRegistrationId(Long registrationId);
    boolean existsByRegistrationId(Long registrationId);
    boolean existsByCouncilId(Long councilId);
    boolean existsByGroupTopicId(Long topicId);
    long countByPublishedTrue();
    @EntityGraph(attributePaths = {"group", "group.topic", "group.topic.advisor1", "group.topic.advisor2",
        "council", "council.members", "council.members.lecturer", "reviewer"})
    List<Defense> findAllByOrderByIdDesc();
    @EntityGraph(attributePaths = {"group", "group.topic", "group.topic.advisor1", "group.topic.advisor2",
        "council", "council.members", "council.members.lecturer", "reviewer"})
    @Query("select d from Defense d where exists (select m.id from CouncilMember m "
        + "where m.council = d.council and m.lecturer.id = :userId) order by d.id desc")
    List<Defense> findForCouncilMemberOrderByIdDesc(@org.springframework.data.repository.query.Param("userId") Long userId);
}
