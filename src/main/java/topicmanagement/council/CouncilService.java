package topicmanagement.council;
import jakarta.validation.constraints.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import topicmanagement.entity.*;
import topicmanagement.enums.*;
import topicmanagement.repository.*;
import topicmanagement.security.CurrentAccount;
import topicmanagement.service.TopicPolicy;
import topicmanagement.student.ReportService;
import topicmanagement.dto.response.CouncilStateResponse;
import topicmanagement.dto.response.CouncilStateResponse.*;
import topicmanagement.dto.response.StudentResultResponse;
import topicmanagement.dto.response.ReportSummaryResponse;
import topicmanagement.service.StudentResultService;
import topicmanagement.dto.response.PageResponse;
import topicmanagement.dto.response.CouncilSummaryResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.*;
import java.math.*;
import java.time.*;

@Service @Transactional(readOnly=true)
public class CouncilService {
    public record MemberInput(@NotNull Long userId,@NotNull CouncilRole role) {}
    public record CreateInput(@NotBlank @Size(max=30) String code,@NotBlank @Size(max=150) String name,
        @NotNull LocalDateTime defenseDate,@NotBlank @Size(max=80) String room,
        @NotNull @Size(min=3,max=5) List<@NotNull @jakarta.validation.Valid MemberInput> members) {}
    private final CouncilRepository councils; private final DefenseRepository defenses; private final GradeRepository grades;
    private final UserRepository users; private final StudentGroupRepository groups;
    private final CurrentAccount accounts; private final ReportService reports;
    private final CouncilManagementService councilManagement;
    private final DefenseService defenseService;
    private final GradeService gradeService;
    private final StudentResultService studentResults;
    public CouncilService(CouncilRepository c,DefenseRepository d,GradeRepository g,UserRepository u,
        StudentGroupRepository groups,CurrentAccount a,ReportService reports, CouncilManagementService councilManagement,
        DefenseService defenseService, GradeService gradeService, StudentResultService studentResults) {
        councils=c;defenses=d;grades=g;users=u;this.groups=groups;accounts=a;this.reports=reports;
        this.councilManagement=councilManagement;this.defenseService=defenseService;
        this.gradeService=gradeService;this.studentResults=studentResults;
    }
    private User staff() {User u=accounts.user();TopicPolicy.staff(u);return u;}
    private boolean member(Council c,User u) {return c.getMembers().stream().anyMatch(m->m.getLecturer().getId().equals(u.getId()));}
    private boolean advisor(Topic t,Long id) {return (t.getAdvisor1()!=null&&t.getAdvisor1().getId().equals(id))||(t.getAdvisor2()!=null&&t.getAdvisor2().getId().equals(id));}
    private CouncilView councilView(Council c) {
        return new CouncilView(c.getId(),c.getCode(),c.getName(),c.getDefenseDate(),c.getRoom(),
            c.getMembers().stream().map(m->new CouncilMemberView(m.getLecturer().getId(),m.getLecturer().getFullName(),m.getRole().name())).toList());
    }
    private DefenseView defenseView(Defense d,User u,List<Grade> defenseGrades,List<ReportSummaryResponse> defenseReports) {
        return new DefenseView(d.id,d.group.getGroupName(),d.group.getTopic().getTitle(),d.council.getName(),
            d.reviewer.getFullName(),d.finalized,d.published,d.finalScore,
            !d.finalized&&member(d.council,u)&&!advisor(d.group.getTopic(),u.getId()),
            !d.finalized&&d.council.getMembers().stream().anyMatch(m->m.getLecturer().getId().equals(u.getId())&&m.getRole()==CouncilRole.CHAIRPERSON),
            defenseGrades.stream().map(g->new GradeView(g.evaluator.getFullName(),g.score,g.comment)).toList(),defenseReports);
    }
    public CouncilStateResponse state() {
        User u=staff();
        var visible=councils.findAllByOrderByDefenseDateDesc().stream().filter(c->u.getRole()==Role.DEAN||member(c,u)).toList();
        var defenseEntities=defenses.findAllByOrderByIdDesc().stream().filter(d->u.getRole()==Role.DEAN||member(d.council,u)).toList();
        var gradeMap=grades.findByDefenseIdIn(defenseEntities.stream().map(d->d.id).toList()).stream()
            .collect(java.util.stream.Collectors.groupingBy(g->g.defense.id));
        var reportMap=reports.listForCouncil(u,defenseEntities);
        var ds=defenseEntities.stream().map(d->defenseView(d,u,gradeMap.getOrDefault(d.id,List.of()),
            reportMap.getOrDefault(d.group.getId(),List.of()))).toList();
        var lecturerOptions=u.getRole()==Role.DEAN?users.findByRoleIn(List.of(Role.DEAN,Role.HEAD_OF_DEPT,Role.LECTURER)).stream()
            .filter(x->x.getStatus()==UserStatus.ACTIVE).map(x->new SelectOption(x.getId(),x.getFullName())).toList():List.<SelectOption>of();
        var assignedGroupIds=new HashSet<>(defenses.findAssignedGroupIds());
        var groupOptions=u.getRole()==Role.DEAN?groups.findByStatus(GroupStatus.APPROVED).stream().filter(g->!assignedGroupIds.contains(g.getId()))
            .map(g->new SelectOption(g.getId(),g.getGroupName()+" — "+g.getTopic().getTitle())).toList():List.<SelectOption>of();
        return new CouncilStateResponse(u.getRole().name(),visible.stream().map(this::councilView).toList(),ds,lecturerOptions,groupOptions);
    }

    public PageResponse<CouncilSummaryResponse> page(int page, int size) {
        User user=staff();
        var pageable=PageRequest.of(Math.max(0,page),Math.min(Math.max(1,size),100),
            Sort.by(Sort.Direction.DESC,"defenseDate"));
        return PageResponse.from(councils.findVisibleSummaries(user.getId(),user.getRole()==Role.DEAN,pageable));
    }
    @Transactional public void create(CreateInput in) {
        councilManagement.create(in);
    }
    @Transactional public void update(Long id, CreateInput in) {
        councilManagement.update(id, in);
    }
    @Transactional public void delete(Long id) {
        councilManagement.delete(id);
    }
    @Transactional public void assign(Long groupId,Long councilId,Long reviewerId) {
        defenseService.assign(groupId,councilId,reviewerId);
    }
    @Transactional public void grade(Long id,BigDecimal score,String comment) {
        gradeService.grade(id,score,comment);
    }
    @Transactional public void finalizeScore(Long id) {
        gradeService.finalizeScore(id);
    }
    @Transactional public void publish(Long id) {
        gradeService.publish(id);
    }
    public StudentResultResponse studentResult() {
        return studentResults.current();
    }
    public StudentResultResponse studentResult(Long periodId) {
        return studentResults.current(periodId);
    }
}
