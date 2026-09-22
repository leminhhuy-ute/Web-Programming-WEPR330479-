package topicmanagement.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import topicmanagement.entity.*;
import topicmanagement.enums.*;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class TopicPolicyTest {
    User user(Role role) {
        var u = new User(); u.setId(1L); u.setRole(role); u.setStatus(UserStatus.ACTIVE);
        var d = new Department(); d.setId(10L); u.setDepartment(d); return u;
    }
    @Test void lecturerCannotApproveDepartmentTopics() {
        assertThrows(AccessDeniedException.class, () -> TopicPolicy.departmentManager(user(Role.LECTURER),10L));
    }
    @Test void headCannotApproveAnotherDepartment() {
        assertThrows(AccessDeniedException.class, () -> TopicPolicy.departmentManager(user(Role.HEAD_OF_DEPT),11L));
        assertDoesNotThrow(() -> TopicPolicy.departmentManager(user(Role.HEAD_OF_DEPT),10L));
    }
    @Test void creatorWithoutAdvisorshipCannotReadGroup() {
        var t = new Topic(); var u = user(Role.LECTURER); t.setCreatedBy(u);
        assertThrows(AccessDeniedException.class, () -> TopicPolicy.advisor(u,t));
        t.setAdvisor1(u); assertDoesNotThrow(() -> TopicPolicy.advisor(u,t));
    }
    @Test void studentAndLockedAccountCannotActAsStaff() {
        assertThrows(AccessDeniedException.class, () -> TopicPolicy.staff(user(Role.STUDENT)));
        var u = user(Role.DEAN); u.setStatus(UserStatus.LOCKED);
        assertThrows(AccessDeniedException.class, () -> TopicPolicy.staff(u));
    }
    @Test void registrationChecksTypeAndSeparateWindows() {
        var p = new RegistrationPeriod(); p.setType(RegistrationPeriodType.COURSE);
        var now = LocalDateTime.now();
        p.setLecturerStartAt(now.minusDays(2)); p.setLecturerEndAt(now.minusDays(1));
        p.setStudentStartAt(now.minusHours(1)); p.setStudentEndAt(now.plusDays(1));
        assertThrows(IllegalArgumentException.class, () -> TopicPolicy.registration(p,RegistrationPeriodType.COURSE,false));
        assertThrows(IllegalArgumentException.class, () -> TopicPolicy.registration(p,RegistrationPeriodType.KLTN,true));
        assertDoesNotThrow(() -> TopicPolicy.registration(p,RegistrationPeriodType.COURSE,true));
    }
}
