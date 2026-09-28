package topicmanagement.council;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import topicmanagement.dto.response.CouncilSummaryResponse;
public interface CouncilRepository extends JpaRepository<Council,Long> {
    @EntityGraph(attributePaths = {"members", "members.lecturer"})
    List<Council> findAllByOrderByDefenseDateDesc();

    @Query(value = "select new topicmanagement.dto.response.CouncilSummaryResponse(c.id,c.code,c.name,c.defenseDate,c.room,c.status,size(c.members)) "
        + "from Council c where :dean=true or exists (select m.id from CouncilMember m where m.council=c and m.lecturer.id=:userId)",
        countQuery = "select count(c) from Council c where :dean=true or exists (select m.id from CouncilMember m where m.council=c and m.lecturer.id=:userId)")
    Page<CouncilSummaryResponse> findVisibleSummaries(@Param("userId") Long userId,
        @Param("dean") boolean dean, Pageable pageable);

    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, Long id);
}
