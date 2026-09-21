package vn.edu.hcmute.topicmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.hcmute.topicmanagement.entity.Announcement;
import vn.edu.hcmute.topicmanagement.enums.AnnouncementAudience;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findByAudienceInOrderByCreatedAtDesc(List<AnnouncementAudience> audiences);
    List<Announcement> findAllByOrderByCreatedAtDesc();
}
