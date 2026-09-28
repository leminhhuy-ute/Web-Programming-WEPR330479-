package topicmanagement.council;
import jakarta.persistence.*;
import java.math.BigDecimal;
import topicmanagement.entity.*;
@Entity @Table(name="defenses")
public class Defense {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Version private Long version;
    @OneToOne(optional=false) @JoinColumn(unique=true) public StudentGroup group;
    @ManyToOne(optional=false) public Council council;
    @ManyToOne(optional=false) public User reviewer;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="registration_id") public TopicRegistration registration;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="topic_id") public Topic topic;
    public boolean finalized;
    public boolean published;
    @Column(precision=4,scale=2) public BigDecimal finalScore;
}
