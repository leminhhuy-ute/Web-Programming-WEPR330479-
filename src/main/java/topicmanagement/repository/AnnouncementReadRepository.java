package topicmanagement.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import topicmanagement.entity.AnnouncementRead;
import topicmanagement.enums.AnnouncementAudience;

public interface AnnouncementReadRepository extends JpaRepository<AnnouncementRead, Long> {
  boolean existsByAnnouncementIdAndUserId(Long announcementId, Long userId);
  List<AnnouncementRead> findByUserIdAndAnnouncementIdIn(Long userId, Collection<Long> announcementIds);
  void deleteByAnnouncementId(Long announcementId);

  @Query("select count(a) from Announcement a where "
      + "(a.audience=:allAudience or a.audience=:audience) and "
      + "(a.department is null or a.department.id=:departmentId) and "
      + "not exists (select r.id from AnnouncementRead r where r.announcement=a and r.user.id=:userId)")
  long countVisibleUnread(@Param("userId") Long userId,
      @Param("allAudience") AnnouncementAudience allAudience,
      @Param("audience") AnnouncementAudience audience,
      @Param("departmentId") Long departmentId);
}
