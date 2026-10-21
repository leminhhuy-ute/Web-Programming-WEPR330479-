package topicmanagement;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import topicmanagement.controller.SharedAnnouncementController;
import topicmanagement.entity.*;
import topicmanagement.enums.*;
import topicmanagement.repository.*;
import topicmanagement.security.CurrentUser;
import static org.junit.jupiter.api.Assertions.*;

// Pins which announcements each role sees on the dashboard list (audience + department, newest first).
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:announcement-visibility-test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop","spring.profiles.active=test"})
class AnnouncementVisibilityTest {
    @Autowired SharedAnnouncementController controller;
    @Autowired AnnouncementRepository announcements;
    @Autowired UserRepository users;
    @Autowired DepartmentRepository departments;

    Department department(String code) {
        var d=new Department();d.setCode(code);d.setName(code);return departments.save(d);
    }
    User user(String name,Role role,Department d) {
        var u=new User();u.setUsername(name);u.setUserCode(name);u.setEmail(name+"@example.test");
        u.setPasswordHash("unused");u.setFullName(name);u.setRole(role);u.setStatus(UserStatus.ACTIVE);u.setDepartment(d);
        return users.save(u);
    }
    Long post(String title,AnnouncementAudience audience,Department d) {
        var a=new Announcement();a.setTitle(title);a.setContent(title);a.setAudience(audience);a.setDepartment(d);
        return announcements.save(a).getId();
    }
    List<Long> visibleFor(User u) {
        var p=CurrentUser.from(u);
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(p,null,p.getAuthorities()));
        return ((List<?>) controller.list()).stream()
            .map(item->((SharedAnnouncementController.AnnouncementItem) item).id()).toList();
    }
    @AfterEach void clearAuthentication() { SecurityContextHolder.clearContext(); }

    @Test void dashboardListShowsOnlyAnnouncementsForTheViewersRoleAndDepartment() {
        var own=department("OWN");var other=department("OTHER");
        var student=user("astudent",Role.STUDENT,own);var lecturer=user("alecturer",Role.LECTURER,own);
        var dean=user("adean",Role.DEAN,null);
        Long all=post("Toàn khoa",AnnouncementAudience.ALL,null);
        Long students=post("Mọi sinh viên",AnnouncementAudience.STUDENT,null);
        Long ownStudents=post("Sinh viên bộ môn mình",AnnouncementAudience.STUDENT,own);
        post("Sinh viên bộ môn khác",AnnouncementAudience.STUDENT,other);
        Long lecturers=post("Mọi giảng viên",AnnouncementAudience.LECTURER,null);
        post("Toàn bộ môn khác",AnnouncementAudience.ALL,other);

        assertEquals(List.of(ownStudents,students,all),visibleFor(student));
        assertEquals(List.of(lecturers,all),visibleFor(lecturer));
        assertEquals(List.of(all),visibleFor(dean));
    }
}
