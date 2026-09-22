package topicmanagement.council;
import topicmanagement.entity.User;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "councils", uniqueConstraints = @UniqueConstraint(name = "uk_council_code", columnNames = "code"))
public class Council {
    @Version private Long version;
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank @Column(nullable = false, length = 30)
    private String code;
    @NotBlank @Column(nullable = false, length = 150)
    private String name;
    @NotNull @Column(nullable = false)
    private LocalDateTime defenseDate;
    @NotBlank @Column(nullable = false, length = 80)
    private String room;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private CouncilStatus status = CouncilStatus.DRAFT;
    @OneToMany(mappedBy = "council", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CouncilMember> members = new ArrayList<>();

    public Council() {}
    public Council(String code, String name, LocalDateTime defenseDate, String room) {
        this.code = code; this.name = name; this.defenseDate = defenseDate; this.room = room;
    }
    public Long getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDateTime getDefenseDate() { return defenseDate; }
    public void setDefenseDate(LocalDateTime defenseDate) { this.defenseDate = defenseDate; }
    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }
    public CouncilStatus getStatus() { return status; }
    public void setStatus(CouncilStatus status) { this.status = status; }
    public List<CouncilMember> getMembers() { return members; }
}
