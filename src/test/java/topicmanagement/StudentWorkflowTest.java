package topicmanagement;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import topicmanagement.entity.*;
import topicmanagement.enums.*;
import topicmanagement.repository.*;
import topicmanagement.student.StudentService;
import topicmanagement.service.StudentGroupService;
import topicmanagement.dto.request.GroupApprovalRequest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:student-test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop","spring.profiles.active=test"})
class StudentWorkflowTest {
    @Autowired topicmanagement.student.ReportRepository reports;
    @Autowired GroupMemberRepository members;
    @Autowired StudentService students;
    @Autowired StudentGroupService lecturerGroups;
    @Autowired UserRepository users;
    @Autowired DepartmentRepository departments;
    @Autowired RegistrationPeriodRepository periods;
    @Autowired TopicRepository topics;
    @Autowired StudentGroupRepository groups;
    @Autowired topicmanagement.student.InvitationRepository invitations;
    User advisor;
    @BeforeEach void seed() {
        reports.deleteAll();invitations.deleteAll();members.deleteAll();groups.deleteAll();
        topics.deleteAll();periods.deleteAll();users.deleteAll();departments.deleteAll();
        var d=new Department();d.setCode("TEST");d.setName("Bộ môn kiểm thử");departments.save(d);
        for(int i=1;i<=4;i++)user("sv"+i,Role.STUDENT,d);
        advisor=user("gv",Role.LECTURER,d);
        var p=new RegistrationPeriod();p.setName("Kiểm thử");p.setType(RegistrationPeriodType.COURSE);
        var now=LocalDateTime.now();p.setLecturerStartAt(now.minusDays(3));p.setLecturerEndAt(now.minusDays(2));
        p.setStudentStartAt(now.minusDays(1));p.setStudentEndAt(now.plusDays(1));periods.save(p);
        var t=new Topic();t.setTopicCode("TEST-TOPIC");t.setTitle("Kiểm thử");t.setDepartment(d);t.setPeriod(p);
        t.setCreatedBy(advisor);t.setAdvisor1(advisor);t.setTopicType(RegistrationPeriodType.COURSE);
        t.setMaxStudents(3);t.setStatus(TopicStatus.APPROVED);topics.save(t);
    }
    User user(String name,Role role,Department d) {
        var u=new User();u.setUsername(name);u.setUserCode(name);u.setEmail(name+"@example.test");
        u.setPasswordHash("unused");u.setFullName(name);u.setStatus(UserStatus.ACTIVE);u.setRole(role);u.setDepartment(d);
        return users.save(u);
    }
    void join(String id) {
        students.invite("sv1",id);
        var u=users.findByUsername(id).orElseThrow();
        students.respond(id,invitations.findByStudentIdAndStatusOrderByCreatedAtDesc(u.getId(),"PENDING").getFirst().id,true);
    }
    @Test void groupHasAtMostThreeMembersAndOneGroupPerStudent() {
        students.createGroup("sv1","Nhóm A");join("sv2");join("sv3");
        assertThrows(IllegalArgumentException.class,()->students.invite("sv1","sv4"));
        assertThrows(IllegalArgumentException.class,()->students.createGroup("sv2","Nhóm B"));
    }
    @Test void nonLeaderCannotRegisterOrTransfer() {
        students.createGroup("sv1","Nhóm A");join("sv2");
        assertThrows(AccessDeniedException.class,()->students.register("sv2","TEST-TOPIC"));
        assertThrows(AccessDeniedException.class,()->students.transfer("sv2","sv1"));
    }
    @Test void invitedStudentMustOwnInvitation() {
        students.createGroup("sv1","Nhóm A");students.invite("sv1","sv2");
        Long id=invitations.findAll().getFirst().id;
        assertThrows(AccessDeniedException.class,()->students.respond("sv3",id,true));
    }
    @Test void registrationLocksMembershipAndReservesTopic() {
        students.createGroup("sv1","Nhóm A");students.register("sv1","TEST-TOPIC");
        assertThrows(IllegalArgumentException.class,()->students.invite("sv1","sv2"));
        students.createGroup("sv3","Nhóm B");
        assertThrows(IllegalArgumentException.class,()->students.register("sv3","TEST-TOPIC"));
    }
    @Test void fullWorkflowUsesSameGroupInLecturerAndStudentModules() throws Exception {
        students.createGroup("sv1","Nhóm A");join("sv2");students.register("sv1","TEST-TOPIC");
        var g=groups.findAll().getFirst();
        assertEquals(1,lecturerGroups.getGroupsForLecturer(advisor).size());
        var decision=new GroupApprovalRequest();decision.setStatus("APPROVED");
        lecturerGroups.approveOrRejectGroup(g.getId(),decision,advisor);
        var pdf=new MockMultipartFile("file","report.pdf","application/pdf","%PDF-1.7\nqa".getBytes());
        assertThrows(AccessDeniedException.class,()->students.upload("sv2","Cuối kỳ","",pdf));
        students.upload("sv1","Cuối kỳ","Bản kiểm thử",pdf);
        var state=students.state("sv2");
        assertEquals("APPROVED",((Map<?,?>)state.get("registration")).get("status"));
        var reports=(java.util.List<?>)state.get("reports");
        assertEquals(1,reports.size());
        Long reportId=(Long)((Map<?,?>)reports.getFirst()).get("id");
        assertArrayEquals(pdf.getBytes(),students.download("sv2",reportId).content);
        assertThrows(AccessDeniedException.class,()->students.download("sv3",reportId));
    }
    @Test void fakePdfIsRejected() {
        students.createGroup("sv1","Nhóm A");students.register("sv1","TEST-TOPIC");
        var decision=new GroupApprovalRequest();decision.setStatus("APPROVED");
        lecturerGroups.approveOrRejectGroup(groups.findAll().getFirst().getId(),decision,advisor);
        var file=new MockMultipartFile("file","fake.pdf","application/pdf","<script>bad</script>".getBytes());
        assertThrows(IllegalArgumentException.class,()->students.upload("sv1","Cuối kỳ","",file));
    }
}
