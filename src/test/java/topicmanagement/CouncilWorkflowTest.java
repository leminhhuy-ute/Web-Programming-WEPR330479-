package topicmanagement;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import topicmanagement.entity.*;
import topicmanagement.enums.*;
import topicmanagement.repository.*;
import topicmanagement.security.CurrentUser;
import topicmanagement.student.StudentService;
import topicmanagement.council.*;
import topicmanagement.service.StudentGroupService;
import topicmanagement.dto.request.GroupApprovalRequest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:council-test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop","spring.profiles.active=test"})
class CouncilWorkflowTest {
    @Autowired CouncilService service;
    @Autowired CouncilRepository councils;
    @Autowired DefenseRepository defenses;
    @Autowired UserRepository users;
    @Autowired DepartmentRepository departments;
    @Autowired RegistrationPeriodRepository periods;
    @Autowired TopicRepository topics;
    @Autowired StudentGroupRepository groups;
    @Autowired StudentService students;
    @Autowired StudentGroupService lecturerGroups;
    User user(String name,Role role,Department d) {
        var u=new User();u.setUsername(name);u.setUserCode(name);u.setEmail(name+"@example.test");
        u.setPasswordHash("unused");u.setFullName(name);u.setRole(role);u.setStatus(UserStatus.ACTIVE);u.setDepartment(d);
        return users.save(u);
    }
    void as(User u) {
        var p=CurrentUser.from(u);
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(p,null,p.getAuthorities()));
    }
    @Test void gradesNeedEligibleMembersChairFinalizationAndDeanPublication() {
        try {
            var department=new Department();department.setCode("QA");department.setName("Kiểm thử");departments.save(department);
            var dean=user("dean",Role.DEAN,department);var advisor=user("advisor",Role.LECTURER,department);
            var chair=user("chair",Role.LECTURER,department);var secretary=user("secretary",Role.LECTURER,department);
            var member=user("member",Role.LECTURER,department);var student=user("student",Role.STUDENT,department);
            var outsider=user("outsider",Role.STUDENT,department);
            var now=LocalDateTime.now();var p=new RegistrationPeriod();p.setName("Test");p.setType(RegistrationPeriodType.COURSE);
            p.setLecturerStartAt(now.minusDays(4));p.setLecturerEndAt(now.minusDays(3));
            p.setStudentStartAt(now.minusDays(1));p.setStudentEndAt(now.plusDays(2));periods.save(p);
            var t=new Topic();t.setTopicCode("QA");t.setTitle("QA");t.setDepartment(department);t.setPeriod(p);
            t.setCreatedBy(advisor);t.setAdvisor1(advisor);t.setMaxStudents(3);t.setTopicType(RegistrationPeriodType.COURSE);
            t.setStatus(TopicStatus.APPROVED);topics.save(t);
            students.createGroup("student","Nhóm QA");students.register("student","QA");
            var g=groups.findAll().getFirst();var approval=new GroupApprovalRequest();approval.setStatus("APPROVED");
            lecturerGroups.approveOrRejectGroup(g.getId(),approval,advisor);
            var roster=List.of(new CouncilService.MemberInput(chair.getId(),CouncilRole.CHAIRPERSON),
                new CouncilService.MemberInput(secretary.getId(),CouncilRole.SECRETARY),
                new CouncilService.MemberInput(member.getId(),CouncilRole.MEMBER));
            as(advisor);
            assertThrows(AccessDeniedException.class,()->service.create(new CouncilService.CreateInput("C","Hội đồng",now.plusDays(1),"A1",roster)));
            as(dean);
            assertThrows(IllegalArgumentException.class,()->service.create(new CouncilService.CreateInput("C","Hội đồng",now.plusDays(1),"A1",roster.subList(0,2))));
            service.create(new CouncilService.CreateInput("C","Hội đồng",now.plusDays(1),"A1",roster));
            Long councilId=councils.findAll().getFirst().getId();
            assertThrows(IllegalArgumentException.class,()->service.assign(g.getId(),councilId,advisor.getId()));
            service.assign(g.getId(),councilId,member.getId());
            Long defenseId=defenses.findAll().getFirst().id;
            assertThrows(IllegalArgumentException.class,()->service.publish(defenseId));
            as(advisor);assertThrows(AccessDeniedException.class,()->service.grade(defenseId,BigDecimal.TEN,"Sai quyền"));
            as(chair);assertThrows(IllegalArgumentException.class,()->service.finalizeScore(defenseId));
            service.grade(defenseId,new BigDecimal("8.00"),"Tốt");
            as(secretary);assertThrows(IllegalArgumentException.class,()->service.grade(defenseId,new BigDecimal("11"),"Sai điểm"));
            service.grade(defenseId,new BigDecimal("9.00"),"Tốt");
            assertThrows(AccessDeniedException.class,()->service.finalizeScore(defenseId));
            as(member);service.grade(defenseId,new BigDecimal("7.00"),"Đạt");
            as(chair);service.finalizeScore(defenseId);
            assertThrows(IllegalArgumentException.class,()->service.grade(defenseId,BigDecimal.TEN,"Đã khóa"));
            as(student);assertEquals(false,((Map<?,?>)service.studentResult()).get("published"));
            as(dean);service.publish(defenseId);
            as(student);var result=(Map<?,?>)service.studentResult();
            assertEquals(true,result.get("published"));assertEquals(new BigDecimal("8.00"),result.get("score"));
            as(outsider);assertEquals(false,((Map<?,?>)service.studentResult()).get("published"));
        } finally {SecurityContextHolder.clearContext();}
    }
}
