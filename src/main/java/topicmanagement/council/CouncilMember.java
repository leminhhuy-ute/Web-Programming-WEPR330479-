package topicmanagement.council;
import topicmanagement.entity.User;

import jakarta.persistence.*;

@Entity
@Table(name = "council_members", uniqueConstraints = {
        @UniqueConstraint(name = "uk_council_lecturer", columnNames = {"council_id", "lecturer_id"})
})
public class CouncilMember {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "council_id", nullable = false)
    private Council council;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "lecturer_id", nullable = false)
    private User lecturer;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private CouncilRole role;

    public CouncilMember() {}
    public CouncilMember(Council council, User lecturer, CouncilRole role) {
        this.council = council; this.lecturer = lecturer; this.role = role;
    }
    public Long getId() { return id; }
    public Council getCouncil() { return council; }
    public User getLecturer() { return lecturer; }
    public CouncilRole getRole() { return role; }
    public void setRole(CouncilRole role) { this.role = role; }
}
