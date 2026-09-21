package topicmanagement.service;

import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import topicmanagement.dto.request.AnnouncementRequest;
import topicmanagement.dto.response.AnnouncementResponse;
import topicmanagement.entity.*;
import topicmanagement.enums.AnnouncementAudience;
import topicmanagement.exception.*;
import topicmanagement.repository.*;

@Service
@Transactional
public class AnnouncementServiceV2 {
  private final AnnouncementRepository announcements;
  private final UserRepository users;

  public AnnouncementServiceV2(AnnouncementRepository a, UserRepository u) {
    announcements = a;
    users = u;
  }

  public List<AnnouncementResponse> findAll(String q, AnnouncementAudience a) {
    return announcements.search(q == null ? "" : q.trim(), a).stream().map(this::map).toList();
  }

  public AnnouncementResponse findById(Long id) {
    return map(entity(id));
  }

  public AnnouncementResponse create(AnnouncementRequest r, Long author) {
    Announcement a = new Announcement();
    a.setCreatedBy(
        users
            .findById(author)
            .orElseThrow(() -> new ResourceNotFoundException("Người tạo không tồn tại.")));
    return save(a, r);
  }

  public AnnouncementResponse update(Long id, AnnouncementRequest r) {
    return save(entity(id), r);
  }

  private AnnouncementResponse save(Announcement a, AnnouncementRequest r) {
    a.setTitle(r.title().trim());
    a.setContent(r.content().trim());
    a.setAudience(r.audience());
    return map(announcements.save(a));
  }

  public void delete(Long id) {
    announcements.delete(entity(id));
  }

  private Announcement entity(Long id) {
    return announcements
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Thông báo không tồn tại."));
  }

  private AnnouncementResponse map(Announcement a) {
    return new AnnouncementResponse(
        a.getId(),
        a.getTitle(),
        a.getContent(),
        a.getAudience().name(),
        a.getCreatedBy() == null ? null : a.getCreatedBy().getId(),
        a.getCreatedAt());
  }
}
