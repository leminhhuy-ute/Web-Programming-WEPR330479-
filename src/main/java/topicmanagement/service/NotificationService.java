package topicmanagement.service;

import java.util.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

  public Feed feed(User recipient) {
    List<Announcement> visible = announcements.search("", null).stream()
        .filter(a -> visibleTo(a, recipient)).toList();
    if (visible.isEmpty()) return new Feed(0, List.of());
    List<Announcement> recent = visible.stream().limit(30).toList();
    Set<Long> readIds = new HashSet<>();
    reads.findByUserIdAndAnnouncementIdIn(recipient.getId(), visible.stream().map(Announcement::getId).toList())
        .forEach(read -> readIds.add(read.getAnnouncement().getId()));
    List<Item> items = recent.stream().map(a -> new Item(a.getId(), a.getTitle(), a.getContent(),
        a.getCreatedBy() == null ? "Khoa CNTT" : a.getCreatedBy().getFullName(), a.getCreatedAt(),
        readIds.contains(a.getId()))).toList();
    return new Feed(visible.stream().filter(a -> !readIds.contains(a.getId())).count(), items);
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
      case HEAD_OF_DEPT -> role == Role.HEAD_OF_DEPT;
    };
    if (!audience) return false;
    return a.getDepartment() == null || (recipient.getDepartment() != null
        && a.getDepartment().getId().equals(recipient.getDepartment().getId()));
  }
}
