package topicmanagement.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import topicmanagement.entity.Announcement;
import topicmanagement.enums.AnnouncementAudience;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
  @Query(
      "select a from Announcement a "
          + "where (:keyword='' or lower(a.title) like lower(concat('%',:keyword,'%'))) "
          + "and (:audience is null or a.audience=:audience) "
          + "order by a.id desc")
  List<Announcement> search(
      @Param("keyword") String keyword, @Param("audience") AnnouncementAudience audience);
}
