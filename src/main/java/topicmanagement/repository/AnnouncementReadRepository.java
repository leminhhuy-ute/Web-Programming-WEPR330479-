package topicmanagement.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import topicmanagement.entity.AnnouncementRead;

public interface AnnouncementReadRepository extends JpaRepository<AnnouncementRead, Long> {
  boolean existsByAnnouncementIdAndUserId(Long announcementId, Long userId);
  List<AnnouncementRead> findByUserIdAndAnnouncementIdIn(Long userId, Collection<Long> announcementIds);
  void deleteByAnnouncementId(Long announcementId);
}
