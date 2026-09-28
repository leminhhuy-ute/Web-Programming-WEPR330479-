package topicmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "advisor_quotas", uniqueConstraints =
    @UniqueConstraint(name = "uk_quota_period_lecturer", columnNames = {"registration_period_id", "lecturer_id"}))
public class AdvisorQuota {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registration_period_id", nullable = false)
    private RegistrationPeriod registrationPeriod;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lecturer_id", nullable = false)
    private User lecturer;

    @Min(1)
    @Column(name = "max_groups", nullable = false)
    private Integer maxGroups;

    public Long getId() { return id; }
    public RegistrationPeriod getRegistrationPeriod() { return registrationPeriod; }
    public void setRegistrationPeriod(RegistrationPeriod registrationPeriod) { this.registrationPeriod = registrationPeriod; }
    public User getLecturer() { return lecturer; }
    public void setLecturer(User lecturer) { this.lecturer = lecturer; }
    public Integer getMaxGroups() { return maxGroups; }
    public void setMaxGroups(Integer maxGroups) { this.maxGroups = maxGroups; }
}
