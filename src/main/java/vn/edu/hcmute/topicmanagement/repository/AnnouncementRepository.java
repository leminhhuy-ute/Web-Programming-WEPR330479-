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

    @org.springframework.data.jpa.repository.Query(
            "SELECT a FROM Announcement a WHERE (:keyword = '' OR LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:audience IS NULL OR a.audience = :audience) ORDER BY a.id DESC")
    List<Announcement> search(@org.springframework.data.repository.query.Param("keyword") String keyword,
                              @org.springframework.data.repository.query.Param("audience") AnnouncementAudience audience);
}
