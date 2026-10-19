package topicmanagement.service;

import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import topicmanagement.dto.request.AnnouncementRequest;
import topicmanagement.dto.response.AnnouncementResponse;
import topicmanagement.dto.response.PageResponse;
import topicmanagement.entity.*;
import topicmanagement.enums.AnnouncementAudience;
import topicmanagement.exception.*;
import topicmanagement.repository.*;

@Service
@Transactional
public class AnnouncementServiceV2 {
  private final AnnouncementRepository announcements;
  private final UserRepository users;
  private final AnnouncementReadRepository reads;

  public AnnouncementServiceV2(AnnouncementRepository a, UserRepository u, AnnouncementReadRepository reads) {
    announcements = a;
    users = u;
    this.reads = reads;
  }

  public List<AnnouncementResponse> findAll(String q, AnnouncementAudience a) {
    return announcements.search(q == null ? "" : q.trim(), a).stream().map(this::map).toList();
  }

  public PageResponse<AnnouncementResponse> findPage(String q, AnnouncementAudience audience, int page, int size) {
    var pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100),
        Sort.by(Sort.Direction.DESC, "id"));
    return PageResponse.from(announcements.search(q == null ? "" : q.trim(), audience, pageable).map(this::map));
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
    a.setDepartment(null);
    return save(a, r);
  }

  public AnnouncementResponse createForDepartment(AnnouncementRequest r, topicmanagement.entity.User author) {
    if (author.getRole() != topicmanagement.enums.Role.DEAN || r.audience() != AnnouncementAudience.STUDENT)
      throw new org.springframework.security.access.AccessDeniedException("Chỉ Trưởng khoa được gửi thông báo cho sinh viên thuộc bộ môn.");
    Announcement a = new Announcement();
    a.setCreatedBy(author);
    a.setDepartment(author.getDepartment());
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
    reads.deleteByAnnouncementId(id);
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
