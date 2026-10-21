package topicmanagement;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import topicmanagement.council.*;
import topicmanagement.dto.request.GroupApprovalRequest;
import topicmanagement.dto.response.CouncilStateResponse;
import topicmanagement.entity.*;
import topicmanagement.enums.*;
import topicmanagement.repository.*;
import topicmanagement.security.CurrentUser;
import topicmanagement.service.StudentGroupService;
import topicmanagement.student.StudentService;
import static org.junit.jupiter.api.Assertions.*;

// The council screen must only load the councils and defenses a lecturer sits on; the filtering
// happens in the repository queries, so this pins the visible result for every role.
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:council-visibility-test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop","spring.profiles.active=test"})
class CouncilVisibilityTest {
    @Autowired CouncilService service;
    @Autowired CouncilRepository councils;
    @Autowired UserRepository users;
    @Autowired DepartmentRepository departments;
    @Autowired RegistrationPeriodRepository periods;
    @Autowired TopicRepository topics;
    @Autowired StudentGroupRepository groups;
    @Autowired StudentService students;
    @Autowired StudentGroupService lecturerGroups;
    @Autowired topicmanagement.student.InvitationRepository invitations;

    User user(String name,Role role,Department d) {
        var u=new User();u.setUsername(name);u.setUserCode(name);u.setEmail(name+"@example.test");
        u.setPasswordHash("unused");u.setFullName(name);u.setRole(role);u.setStatus(UserStatus.ACTIVE);u.setDepartment(d);
        return users.save(u);
    }
    void as(User u) {
        var p=CurrentUser.from(u);
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(p,null,p.getAuthorities()));
    }
    List<String> codes(CouncilStateResponse state) { return state.councils().stream().map(CouncilStateResponse.CouncilView::code).toList(); }
    List<Long> ids(List<CouncilStateResponse.SelectOption> options) { return options.stream().map(CouncilStateResponse.SelectOption::id).toList(); }
    @AfterEach void clearAuthentication() { SecurityContextHolder.clearContext(); }

    @Test void councilScreenShowsOnlyCouncilsAndDefensesTheLecturerSitsOn() {
        var department=new Department();department.setCode("VIS");department.setName("Hiển thị");departments.save(department);
        var dean=user("vdean",Role.DEAN,department);var advisor=user("vadvisor",Role.LECTURER,department);
        var chair=user("vchair",Role.LECTURER,department);var secretary=user("vsecretary",Role.LECTURER,department);
        var reviewer=user("vreviewer",Role.LECTURER,department);var otherReviewer=user("vother",Role.LECTURER,department);
        var retired=user("vretired",Role.LECTURER,department);retired.setStatus(UserStatus.INACTIVE);users.save(retired);
        var leader=user("vstudent",Role.STUDENT,department);
        var second=user("vstudent2",Role.STUDENT,department);var third=user("vstudent3",Role.STUDENT,department);

        var now=LocalDateTime.now();var p=new RegistrationPeriod();p.setName("Hiển thị");p.setType(RegistrationPeriodType.COURSE);
        p.setLecturerStartAt(now.minusDays(4));p.setLecturerEndAt(now.minusDays(3));
        p.setStudentStartAt(now.minusDays(1));p.setStudentEndAt(now.plusDays(2));periods.save(p);
        var t=new Topic();t.setTopicCode("VIS");t.setTitle("Đề tài hiển thị");t.setDepartment(department);t.setPeriod(p);
        t.setCreatedBy(advisor);t.setAdvisor1(advisor);t.setMaxStudents(3);t.setTopicType(RegistrationPeriodType.COURSE);
        t.setStatus(TopicStatus.APPROVED);topics.save(t);
        students.createGroup("vstudent","Nhóm hiển thị");
        students.invite("vstudent","vstudent2");
        students.respond("vstudent2", invitations.findByStudentIdAndStatusOrderByCreatedAtDesc(second.getId(),"PENDING").getFirst().id, true);
        students.invite("vstudent","vstudent3");
        students.respond("vstudent3", invitations.findByStudentIdAndStatusOrderByCreatedAtDesc(third.getId(),"PENDING").getFirst().id, true);
        students.register("vstudent","VIS");
        var g=groups.findAll().getFirst();var approval=new GroupApprovalRequest();approval.setStatus("APPROVED");
        lecturerGroups.approveOrRejectGroup(g.getId(),approval,advisor);
        lecturerGroups.confirmCompletion(g.getId(),advisor);

        as(dean);
        service.create(new CouncilService.CreateInput("VIS-A","Hội đồng A",now.plusDays(1),"A1",List.of(
            new CouncilService.MemberInput(chair.getId(),CouncilRole.CHAIRPERSON),
            new CouncilService.MemberInput(secretary.getId(),CouncilRole.SECRETARY),
            new CouncilService.MemberInput(reviewer.getId(),CouncilRole.REVIEWER))));
        service.create(new CouncilService.CreateInput("VIS-B","Hội đồng B",now.plusDays(2),"B1",List.of(
            new CouncilService.MemberInput(dean.getId(),CouncilRole.CHAIRPERSON),
            new CouncilService.MemberInput(secretary.getId(),CouncilRole.SECRETARY),
            new CouncilService.MemberInput(otherReviewer.getId(),CouncilRole.REVIEWER))));
        var before=service.state();
        assertTrue(ids(before.groups()).contains(g.getId()), "Nhóm chưa phân công phải có trong danh sách chọn");
        assertTrue(ids(before.lecturers()).containsAll(List.of(dean.getId(),advisor.getId(),chair.getId())));
        assertFalse(ids(before.lecturers()).contains(retired.getId()), "Giảng viên ngừng hoạt động không được chọn");
        assertFalse(ids(before.lecturers()).contains(leader.getId()), "Sinh viên không được chọn vào hội đồng");

        Long councilA=councils.findAll().stream().filter(c->c.getCode().equals("VIS-A")).findFirst().orElseThrow().getId();
        service.assign(g.getId(),councilA,reviewer.getId());

        var deanView=service.state();
        assertEquals(List.of("VIS-B","VIS-A"),codes(deanView), "Trưởng khoa thấy mọi hội đồng, mới nhất trước");
        assertEquals(1,deanView.defenses().size());
        assertFalse(ids(deanView.groups()).contains(g.getId()), "Nhóm đã phân công không còn trong danh sách chọn");

        as(chair);
        var chairView=service.state();
        assertEquals(List.of("VIS-A"),codes(chairView));
        assertEquals(1,chairView.defenses().size());
        assertTrue(chairView.defenses().getFirst().canFinalize());
        assertTrue(chairView.lecturers().isEmpty());
        assertTrue(chairView.groups().isEmpty());

        as(secretary);
        assertEquals(List.of("VIS-B","VIS-A"),codes(service.state()), "Thành viên của hai hội đồng thấy cả hai");

        as(otherReviewer);
        var otherView=service.state();
        assertEquals(List.of("VIS-B"),codes(otherView));
        assertTrue(otherView.defenses().isEmpty(), "Không thấy buổi bảo vệ của hội đồng khác");

        as(advisor);
        var advisorView=service.state();
        assertTrue(advisorView.councils().isEmpty());
        assertTrue(advisorView.defenses().isEmpty());
    }
}
