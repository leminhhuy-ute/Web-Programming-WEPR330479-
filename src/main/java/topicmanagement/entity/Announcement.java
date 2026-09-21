package topicmanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import topicmanagement.enums.AnnouncementAudience;

@Entity
@Table(name = "announcements")
public class Announcement {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private AnnouncementAudience audience;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by")
  private User createdBy;

  @Column(name = "created_at", insertable = false, updatable = false)
  private LocalDateTime createdAt;

  public Long getId() {
    return id;
  }

  public void setId(Long v) {
    id = v;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String v) {
    title = v;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String v) {
    content = v;
  }

  public AnnouncementAudience getAudience() {
    return audience;
  }

  public void setAudience(AnnouncementAudience v) {
    audience = v;
  }

  public User getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(User v) {
    createdBy = v;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
