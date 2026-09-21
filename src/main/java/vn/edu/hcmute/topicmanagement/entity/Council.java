package vn.edu.hcmute.topicmanagement.entity;

import jakarta.persistence.*;
import vn.edu.hcmute.topicmanagement.enums.CouncilStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "councils")
public class Council {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "period_id")
    private RegistrationPeriod period;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CouncilStatus status = CouncilStatus.ACTIVE;

    @Column(name = "defense_date")
    private LocalDate defenseDate;

    @Column(name = "defense_time", length = 20)
    private String defenseTime;

    @Column(length = 100)
    private String room;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "council", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CouncilMember> members = new ArrayList<>();

    public Council() {}

    public Council(String code, String name, Department department, RegistrationPeriod period, LocalDate defenseDate, String defenseTime, String room) {
        this.code = code;
        this.name = name;
        this.department = department;
        this.period = period;
        this.defenseDate = defenseDate;
        this.defenseTime = defenseTime;
        this.room = room;
        this.status = CouncilStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public RegistrationPeriod getPeriod() { return period; }
    public void setPeriod(RegistrationPeriod period) { this.period = period; }
    public CouncilStatus getStatus() { return status; }
    public void setStatus(CouncilStatus status) { this.status = status; }
    public LocalDate getDefenseDate() { return defenseDate; }
    public void setDefenseDate(LocalDate defenseDate) { this.defenseDate = defenseDate; }
    public String getDefenseTime() { return defenseTime; }
    public void setDefenseTime(String defenseTime) { this.defenseTime = defenseTime; }
    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<CouncilMember> getMembers() { return members; }
    public void setMembers(List<CouncilMember> members) { this.members = members; }
}
