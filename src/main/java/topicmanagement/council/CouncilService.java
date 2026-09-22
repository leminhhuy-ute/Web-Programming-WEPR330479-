package topicmanagement.council;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import topicmanagement.entity.*;
import topicmanagement.enums.*;
import topicmanagement.repository.*;
import topicmanagement.security.CurrentAccount;
import topicmanagement.service.TopicPolicy;
import java.util.*;
import java.math.*;
import java.time.*;

@Service @Transactional(readOnly=true)
public class CouncilService {
    public record MemberInput(@NotNull Long userId,@NotNull CouncilRole role) {}
    public record CreateInput(@NotBlank @Size(max=30) String code,@NotBlank @Size(max=150) String name,
        @NotNull LocalDateTime defenseDate,@NotBlank @Size(max=80) String room,
        @NotNull @Size(min=3,max=5) List<@jakarta.validation.Valid MemberInput> members) {}
    private final CouncilRepository councils; private final DefenseRepository defenses; private final GradeRepository grades;
    private final UserRepository users; private final StudentGroupRepository groups; private final GroupMemberRepository members;
    private final CurrentAccount accounts; private final EntityManager em;
    public CouncilService(CouncilRepository c,DefenseRepository d,GradeRepository g,UserRepository u,
        StudentGroupRepository groups,GroupMemberRepository members,CurrentAccount a,EntityManager em) {
        councils=c;defenses=d;grades=g;users=u;this.groups=groups;this.members=members;accounts=a;this.em=em;
    }
    private User staff() {User u=accounts.user();TopicPolicy.staff(u);return u;}
    private User dean() {User u=staff();if(u.getRole()!=Role.DEAN)throw new AccessDeniedException("Chỉ trưởng khoa được thực hiện.");return u;}
    private boolean member(Council c,User u) {return c.getMembers().stream().anyMatch(m->m.getLecturer().getId().equals(u.getId()));}
    private boolean advisor(Topic t,Long id) {return (t.getAdvisor1()!=null&&t.getAdvisor1().getId().equals(id))||(t.getAdvisor2()!=null&&t.getAdvisor2().getId().equals(id));}
    private Defense locked(Long id) {var d=em.find(Defense.class,id,LockModeType.PESSIMISTIC_WRITE);if(d==null)throw new IllegalArgumentException("Không tìm thấy phân công.");return d;}
    private Map<String,Object> councilView(Council c) {
        return Map.of("id",c.getId(),"code",c.getCode(),"name",c.getName(),"date",c.getDefenseDate(),"room",c.getRoom(),
            "members",c.getMembers().stream().map(m->Map.of("id",m.getLecturer().getId(),"name",m.getLecturer().getFullName(),"role",m.getRole().name())).toList());
    }
    private Map<String,Object> defenseView(Defense d,User u) {
        Map<String,Object> v=new LinkedHashMap<>();
        v.put("id",d.id);v.put("groupName",d.group.getGroupName());v.put("topic",d.group.getTopic().getTitle());
        v.put("council",d.council.getName());v.put("reviewer",d.reviewer.getFullName());v.put("finalized",d.finalized);
        v.put("published",d.published);v.put("score",d.finalScore);
        v.put("canGrade",!d.finalized&&member(d.council,u)&&!advisor(d.group.getTopic(),u.getId()));
        v.put("canFinalize",!d.finalized&&d.council.getMembers().stream().anyMatch(m->m.getLecturer().getId().equals(u.getId())&&m.getRole()==CouncilRole.CHAIRPERSON));
        v.put("grades",grades.findByDefenseId(d.id).stream().map(g->Map.of("name",g.evaluator.getFullName(),"score",g.score,"comment",g.comment)).toList());
        return v;
    }
    public Map<String,Object> state() {
        User u=staff();
        var cs=councils.findAll().stream().filter(c->u.getRole()==Role.DEAN||member(c,u)).map(this::councilView).toList();
        var ds=defenses.findAll().stream().filter(d->u.getRole()==Role.DEAN||member(d.council,u)).map(d->defenseView(d,u)).toList();
        return Map.of("role",u.getRole().name(),"councils",cs,"defenses",ds,
            "lecturers",u.getRole()==Role.DEAN?users.findAll().stream().filter(x->x.getRole()!=Role.STUDENT&&x.getStatus()==UserStatus.ACTIVE)
                .map(x->Map.of("id",x.getId(),"name",x.getFullName())).toList():List.of(),
            "groups",u.getRole()==Role.DEAN?groups.findAll().stream().filter(g->g.getStatus()==GroupStatus.APPROVED&&defenses.findByGroupId(g.getId()).isEmpty())
                .map(g->Map.of("id",g.getId(),"name",g.getGroupName()+" — "+g.getTopic().getTitle())).toList():List.of());
    }
    @Transactional public void create(CreateInput in) {
        dean();
        if(in.members()==null||in.members().size()<3||in.members().size()>5)throw new IllegalArgumentException("Hội đồng phải có 3–5 thành viên.");
        if(in.members().stream().map(MemberInput::userId).distinct().count()!=in.members().size())throw new IllegalArgumentException("Thành viên không được trùng.");
        if(in.members().stream().filter(m->m.role()==CouncilRole.CHAIRPERSON).count()!=1
          ||in.members().stream().filter(m->m.role()==CouncilRole.SECRETARY).count()!=1)throw new IllegalArgumentException("Cần đúng một Chủ tịch và một Thư ký.");
        if(in.defenseDate().isBefore(LocalDateTime.now()))throw new IllegalArgumentException("Ngày hội đồng không được trong quá khứ.");
        var c=new Council(in.code().strip(),in.name().strip(),in.defenseDate(),in.room().strip());
        for(var m:in.members()) {
            User u=users.findById(m.userId()).orElseThrow(()->new IllegalArgumentException("Giảng viên không tồn tại."));
            TopicPolicy.staff(u);c.getMembers().add(new CouncilMember(c,u,m.role()));
        }
        c.setStatus(CouncilStatus.READY);councils.save(c);
    }
    @Transactional public void assign(Long groupId,Long councilId,Long reviewerId) {
        dean();var g=em.find(StudentGroup.class,groupId,LockModeType.PESSIMISTIC_WRITE);
        if(g==null||g.getStatus()!=GroupStatus.APPROVED)throw new IllegalArgumentException("Nhóm phải được giảng viên chấp thuận.");
        em.lock(g.getTopic(),LockModeType.PESSIMISTIC_WRITE);
        em.refresh(g.getTopic());
        if(defenses.findByGroupId(groupId).isPresent())throw new IllegalArgumentException("Nhóm đã được phân công.");
        var c=councils.findById(councilId).orElseThrow(()->new IllegalArgumentException("Không tìm thấy hội đồng."));
        if(c.getMembers().stream().noneMatch(m->m.getLecturer().getId().equals(reviewerId)))throw new IllegalArgumentException("GVPB phải thuộc hội đồng.");
        if(c.getMembers().stream().anyMatch(m->advisor(g.getTopic(),m.getLecturer().getId())))throw new IllegalArgumentException("Hội đồng này có GVHD của đề tài; hãy chọn hội đồng khác.");
        var period=g.getTopic().getPeriod();
        if(period.getDefenseDate()!=null&&!period.getDefenseDate().equals(c.getDefenseDate().toLocalDate()))
            throw new IllegalArgumentException("Ngày hội đồng phải khớp ngày báo cáo của đợt đăng ký.");
        Defense d=new Defense();d.group=g;d.council=c;d.reviewer=users.findById(reviewerId).orElseThrow();defenses.save(d);
    }
    @Transactional public void grade(Long id,BigDecimal score,String comment) {
        User u=staff();Defense d=locked(id);
        if(!member(d.council,u)||advisor(d.group.getTopic(),u.getId()))throw new AccessDeniedException("Không được chấm đề tài này.");
        if(d.finalized)throw new IllegalArgumentException("Điểm đã tổng hợp, không thể sửa.");
        var deadline=d.group.getTopic().getPeriod().getReviewDeadline();
        if(d.reviewer.getId().equals(u.getId())&&deadline!=null&&LocalDateTime.now().isAfter(deadline))
            throw new IllegalArgumentException("Đã hết hạn nộp điểm phản biện.");
        if(score==null||score.compareTo(BigDecimal.ZERO)<0||score.compareTo(BigDecimal.TEN)>0||score.scale()>2)
            throw new IllegalArgumentException("Điểm từ 0 đến 10, tối đa 2 chữ số thập phân.");
        if(comment==null||comment.isBlank()||comment.length()>2000)throw new IllegalArgumentException("Nhận xét cần từ 1 đến 2.000 ký tự.");
        Grade g=grades.findByDefenseIdAndEvaluatorId(id,u.getId()).orElseGet(Grade::new);
        g.defense=d;g.evaluator=u;g.score=score;g.comment=comment.strip();g.updatedAt=LocalDateTime.now();grades.save(g);
    }
    @Transactional public void finalizeScore(Long id) {
        User u=staff();Defense d=locked(id);
        if(d.council.getMembers().stream().noneMatch(m->m.getLecturer().getId().equals(u.getId())&&m.getRole()==CouncilRole.CHAIRPERSON))
            throw new AccessDeniedException("Chỉ Chủ tịch hội đồng tổng hợp điểm.");
        if(d.finalized)throw new IllegalArgumentException("Đã tổng hợp điểm.");
        var all=grades.findByDefenseId(id);
        if(all.size()!=d.council.getMembers().size())throw new IllegalArgumentException("Chưa đủ điểm của tất cả thành viên.");
        d.finalScore=all.stream().map(g->g.score).reduce(BigDecimal.ZERO,BigDecimal::add)
            .divide(BigDecimal.valueOf(all.size()),2,RoundingMode.HALF_UP);d.finalized=true;
    }
    @Transactional public void publish(Long id) {
        dean();Defense d=locked(id);if(!d.finalized)throw new IllegalArgumentException("Chủ tịch chưa tổng hợp điểm.");d.published=true;
    }
    public Object studentResult() {
        User u=accounts.user();if(u.getRole()!=Role.STUDENT)throw new AccessDeniedException("Chỉ sinh viên.");
        var membership=members.findByStudentId(u.getId()).stream().findFirst();
        if(membership.isEmpty())return Map.of("published",false);
        var d=defenses.findByGroupId(membership.get().getGroup().getId()).filter(x->x.published);
        if(d.isEmpty())return Map.of("published",false);
        var result=d.get();
        return Map.of("published",true,"score",result.finalScore,"topic",result.group.getTopic().getTitle(),
            "grades",grades.findByDefenseId(result.id).stream().map(g->Map.of("name",g.evaluator.getFullName(),"score",g.score,"comment",g.comment)).toList());
    }
}
