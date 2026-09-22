package topicmanagement.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import topicmanagement.entity.StudentGroup;

public class StudentGroupResponse {
    private Long id;
    private String groupCode;
    private String groupName;
    private TopicResponse topic;
    private UserResponse leader;
    private Integer memberCount;
    private String status;
    private LocalDateTime registeredAt;
    private String notes;
    private List<UserResponse> members = new ArrayList<>();

    public StudentGroupResponse() {}

    public StudentGroupResponse(StudentGroup g) {
        if (g != null) {
            this.id = g.getId();
            this.groupCode = g.getGroupCode();
            this.groupName = g.getGroupName();
            if (g.getTopic() != null) this.topic = new TopicResponse(g.getTopic());
            if (g.getLeader() != null) this.leader = new UserResponse(g.getLeader());
            this.memberCount = g.getMemberCount();
            this.status = g.getStatus() != null ? g.getStatus().name() : null;
            this.registeredAt = g.getRegisteredAt();
            this.notes = g.getNotes();
            if (g.getMembers() != null) {
                g.getMembers().forEach(m -> {
                    if (m.getStudent() != null) {
                        this.members.add(new UserResponse(m.getStudent()));
                    }
                });
            }
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGroupCode() { return groupCode; }
    public void setGroupCode(String groupCode) { this.groupCode = groupCode; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public TopicResponse getTopic() { return topic; }
    public void setTopic(TopicResponse topic) { this.topic = topic; }

    public UserResponse getLeader() { return leader; }
    public void setLeader(UserResponse leader) { this.leader = leader; }

    public Integer getMemberCount() { return memberCount; }
    public void setMemberCount(Integer memberCount) { this.memberCount = memberCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<UserResponse> getMembers() { return members; }
    public void setMembers(List<UserResponse> members) { this.members = members; }
}
