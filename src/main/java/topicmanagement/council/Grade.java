package topicmanagement.council;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import topicmanagement.entity.User;
@Entity @Table(name="defense_grades",uniqueConstraints=@UniqueConstraint(columnNames={"defense_id","evaluator_id"}))
public class Grade {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional=false) @JoinColumn(name="defense_id") public Defense defense;
    @ManyToOne(optional=false) @JoinColumn(name="evaluator_id") public User evaluator;
    @Column(nullable=false,precision=4,scale=2) public BigDecimal score;
    @Column(nullable=false,length=2000) public String comment;
    public LocalDateTime updatedAt=LocalDateTime.now();
}
