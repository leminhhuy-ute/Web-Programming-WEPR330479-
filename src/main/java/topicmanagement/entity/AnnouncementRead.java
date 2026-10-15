package topicmanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "announcement_reads", uniqueConstraints = @UniqueConstraint(columnNames = {"announcement_id", "user_id"}))
public class AnnouncementRead {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "announcement_id")
  private Announcement announcement;
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;
  @Column(name = "read_at", nullable = false)
  private LocalDateTime readAt = LocalDateTime.now();

  public Long getId() { return id; }
  public Announcement getAnnouncement() { return announcement; }
  public void setAnnouncement(Announcement value) { announcement = value; }
  public User getUser() { return user; }
  public void setUser(User value) { user = value; }
  public LocalDateTime getReadAt() { return readAt; }
}
