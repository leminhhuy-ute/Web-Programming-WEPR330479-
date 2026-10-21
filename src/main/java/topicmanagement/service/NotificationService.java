package topicmanagement.service;

import java.util.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import topicmanagement.entity.*;
import topicmanagement.enums.*;
import topicmanagement.exception.ResourceNotFoundException;
import topicmanagement.repository.*;

@Service
@Transactional(readOnly = true)
public class NotificationService {
  private final AnnouncementRepository announcements;
  private final AnnouncementReadRepository reads;

  public NotificationService(AnnouncementRepository announcements, AnnouncementReadRepository reads) {
    this.announcements = announcements;
    this.reads = reads;
  }

  public record Item(Long id, String title, String content, String sender,
      java.time.LocalDateTime createdAt, boolean read) {}
  public record Feed(long unreadCount, List<Item> items) {}

  private static AnnouncementAudience audienceOf(User recipient) {
    return switch (recipient.getRole()) {
      case STUDENT -> AnnouncementAudience.STUDENT;
      case LECTURER -> AnnouncementAudience.LECTURER;
      default -> null;
    };
  }

  private static Long departmentOf(User recipient) {
    return recipient.getDepartment() == null ? null : recipient.getDepartment().getId();
  }

  /** Every announcement the recipient may read, newest first; audience and department are filtered in the query. */
  public List<Announcement> visibleAnnouncements(User recipient) {
    return announcements.findVisible(AnnouncementAudience.ALL, audienceOf(recipient), departmentOf(recipient),
        Pageable.unpaged(Sort.by(Sort.Direction.DESC, "id"))).getContent();
  }

  public Feed feed(User recipient) {
    AnnouncementAudience audience = audienceOf(recipient);
    Long departmentId = departmentOf(recipient);
    List<Announcement> recent = announcements.findVisible(AnnouncementAudience.ALL, audience, departmentId,
        PageRequest.of(0, 30, Sort.by(Sort.Direction.DESC, "id"))).getContent();
    long unreadCount = reads.countVisibleUnread(recipient.getId(), AnnouncementAudience.ALL, audience, departmentId);
    if (recent.isEmpty()) return new Feed(unreadCount, List.of());
    Set<Long> readIds = new HashSet<>();
    reads.findByUserIdAndAnnouncementIdIn(recipient.getId(), recent.stream().map(Announcement::getId).toList())
        .forEach(read -> readIds.add(read.getAnnouncement().getId()));
    List<Item> items = recent.stream().map(a -> new Item(a.getId(), a.getTitle(), a.getContent(),
        a.getCreatedBy() == null ? "Khoa CNTT" : a.getCreatedBy().getFullName(), a.getCreatedAt(),
        readIds.contains(a.getId()))).toList();
    return new Feed(unreadCount, items);
  }

  @Transactional
  public void markRead(User recipient, Long id) {
    Announcement announcement = announcements.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Thông báo không tồn tại."));
    if (!visibleTo(announcement, recipient)) throw new AccessDeniedException("Không có quyền xem thông báo này.");
    if (reads.existsByAnnouncementIdAndUserId(id, recipient.getId())) return;
    AnnouncementRead read = new AnnouncementRead();
    read.setAnnouncement(announcement);
    read.setUser(recipient);
    reads.save(read);
  }

  public boolean visibleTo(Announcement a, User recipient) {
    Role role = recipient.getRole();
    boolean audience = switch (a.getAudience()) {
      case ALL -> true;
      case STUDENT -> role == Role.STUDENT;
      case LECTURER -> role == Role.LECTURER;
    };
    if (!audience) return false;
    return a.getDepartment() == null || (recipient.getDepartment() != null
        && a.getDepartment().getId().equals(recipient.getDepartment().getId()));
  }
}
