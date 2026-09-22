package topicmanagement.config;

import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import topicmanagement.entity.*;
import topicmanagement.enums.*;
import topicmanagement.repository.*;

/** Local demo only. Never seeds or overwrites accounts in the mysql profile. */
@Component
@Profile("demo")
public class DemoData implements CommandLineRunner {
    private final UserRepository users;
    private final DepartmentRepository departments;
    private final RegistrationPeriodRepository periods;
    private final TopicRepository topics;
    private final PasswordEncoder passwords;
    public DemoData(UserRepository users, DepartmentRepository departments,
            RegistrationPeriodRepository periods, TopicRepository topics, PasswordEncoder passwords) {
        this.users=users; this.departments=departments; this.periods=periods;
        this.topics=topics; this.passwords=passwords;
    }
    @Override @Transactional public void run(String... args) {
        if (users.count() > 0) return;
        var d = new Department(); d.setCode("CNPM"); d.setName("Công nghệ phần mềm");
        departments.save(d);
        account("dean01", "Trưởng khoa (demo)", Role.DEAN,d);
        account("hod01", "Trưởng bộ môn (demo)", Role.HEAD_OF_DEPT,d);
        var lecturer = account("lecturer01", "Nguyễn Minh Anh (demo)", Role.LECTURER,d);
        account("lecturer02", "Trần Thanh Bình (demo)", Role.LECTURER,d);
        account("lecturer03", "Lê Hoàng Phúc (demo)", Role.LECTURER,d);
        account("lecturer04", "Phạm Ngọc Lan (demo)", Role.LECTURER,d);
        account("lecturer05", "Võ Thảo Nguyên (demo)", Role.LECTURER,d);
        for (int i=1;i<=6;i++) {
            var student=account("student0"+i,"Sinh viên "+i+" (demo)",Role.STUDENT,d);
            student.setUserCode("2211000"+i);
        }
        var now = LocalDateTime.now();
        var p = new RegistrationPeriod(); p.setName("Đợt đề xuất môn học — demo");
        p.setType(RegistrationPeriodType.COURSE);
        p.setLecturerStartAt(now.minusDays(7)); p.setLecturerEndAt(now.plusDays(7));
        p.setStudentStartAt(now.plusDays(8)); p.setStudentEndAt(now.plusDays(21));
        periods.save(p);
        var t = new Topic(); t.setTopicCode("DEMO-WEB-01");
        t.setTitle("Hệ thống quản lý đề tài và nhóm sinh viên");
        t.setDescription("Xây dựng ứng dụng Spring MVC quản lý vòng đời đề tài trong khoa.");
        t.setRequirements("Java, Spring Boot, Spring Data JPA, HTML, CSS, JavaScript");
        t.setMaxStudents(3); t.setTopicType(RegistrationPeriodType.COURSE);
        t.setDepartment(d); t.setPeriod(p); t.setCreatedBy(lecturer);
        t.setStatus(TopicStatus.PENDING); topics.save(t);
        var studentPeriod = new RegistrationPeriod();
        studentPeriod.setName("Đợt đăng ký sinh viên — demo");studentPeriod.setType(RegistrationPeriodType.COURSE);
        studentPeriod.setLecturerStartAt(now.minusDays(21));studentPeriod.setLecturerEndAt(now.minusDays(8));
        studentPeriod.setStudentStartAt(now.minusDays(7));studentPeriod.setStudentEndAt(now.plusDays(14));
        periods.save(studentPeriod);
        var published=new Topic();published.setTopicCode("DEMO-SV-01");
        published.setTitle("Cổng thông tin hoạt động sinh viên");
        published.setDescription("Thiết kế website quản lý hoạt động và đăng ký tham gia cho sinh viên.");
        published.setRequirements("Spring Boot · HTML · CSS · JavaScript");published.setMaxStudents(3);
        published.setTopicType(RegistrationPeriodType.COURSE);published.setDepartment(d);published.setPeriod(studentPeriod);
        published.setCreatedBy(lecturer);published.setAdvisor1(lecturer);published.setStatus(TopicStatus.APPROVED);
        topics.save(published);
    }
    private User account(String username, String name, Role role, Department department) {
        var u = new User(); u.setUserCode(username.toUpperCase()); u.setUsername(username);
        u.setEmail(username+"@example.test"); u.setFullName(name); u.setRole(role);
        u.setStatus(UserStatus.ACTIVE); u.setDepartment(department);
        u.setPasswordHash(passwords.encode("Demo@12345")); return users.save(u);
    }
}
