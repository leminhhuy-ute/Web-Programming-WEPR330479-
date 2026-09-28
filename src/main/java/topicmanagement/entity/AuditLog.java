package topicmanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_entity", columnList = "entity_name,entity_id"),
    @Index(name = "idx_audit_user_created", columnList = "user_id,created_at")
})
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private User user;

    @Column(nullable = false, length = 80)
    private String action;

    @Column(name = "entity_name", nullable = false, length = 80)
    private String entity;

    @Column(name = "entity_id", length = 80)
    private String entityId;

    @Lob @Column(name = "old_value")
    private String oldValue;

    @Lob @Column(name = "new_value")
    private String newValue;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    public Long getId() { return id; }
    public void setUser(User user) { this.user = user; }
    public void setAction(String action) { this.action = action; }
    public void setEntity(String entity) { this.entity = entity; }
    public void setEntityId(String entityId) { this.entityId = entityId; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
}
