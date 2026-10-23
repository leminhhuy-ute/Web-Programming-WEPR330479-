package topicmanagement;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import topicmanagement.dto.request.AdvisorQuotaRequest;
import topicmanagement.entity.*;
import topicmanagement.enums.*;
import topicmanagement.repository.*;
import topicmanagement.service.AdvisorQuotaService;
import topicmanagement.service.TopicRegistrationService;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:advisor-quota-concurrency;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.profiles.active=test"
})
class AdvisorQuotaConcurrencyTest {
    @Autowired DepartmentRepository departments;
    @Autowired UserRepository users;
    @Autowired RegistrationPeriodRepository periods;
    @Autowired TopicRepository topics;
    @Autowired StudentGroupRepository groups;
    @Autowired GroupMemberRepository members;
    @Autowired TopicRegistrationRepository registrations;
    @Autowired AdvisorQuotaService quotas;
    @Autowired TopicRegistrationService registrationService;

    @Test
    void simultaneousApprovalsCannotExceedAdvisorQuotaForOneTopic() throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        Department department = new Department();
        department.setCode("Q" + suffix.substring(0, 8));
        department.setName("Quota concurrency");
        departments.save(department);

        User advisor = user("advisor", suffix, Role.LECTURER, department);
        User studentOne = user("student1", suffix, Role.STUDENT, department);
        User studentTwo = user("student2", suffix, Role.STUDENT, department);
        LocalDateTime now = LocalDateTime.now();
        RegistrationPeriod period = new RegistrationPeriod();
        period.setName("Quota test " + suffix);
        period.setType(RegistrationPeriodType.COURSE);
        period.setLecturerStartAt(now.minusDays(3));
        period.setLecturerEndAt(now.minusDays(2));
        period.setStudentStartAt(now.minusDays(1));
        period.setStudentEndAt(now.plusDays(2));
        periods.save(period);

        Topic topic = new Topic();
        topic.setTopicCode("QT-" + suffix);
        topic.setTitle("Đề tài quota concurrency");
        topic.setDescription("Mô tả đề tài dùng để kiểm tra giới hạn nhóm.");
        topic.setTopicType(RegistrationPeriodType.COURSE);
        topic.setStatus(TopicStatus.APPROVED);
        topic.setDepartment(department);
        topic.setPeriod(period);
        topic.setCreatedBy(advisor);
        topic.setAdvisor1(advisor);
        topics.save(topic);

        StudentGroup groupOne = group("G1-" + suffix, studentOne, period);
        StudentGroup groupTwo = group("G2-" + suffix, studentTwo, period);
        quotas.save(new AdvisorQuotaRequest(period.getId(), advisor.getId(), 1));
        registrationService.submit(groupOne, topic, studentOne);
        registrationService.submit(groupTwo, topic, studentTwo);

        List<Boolean> outcomes = approveConcurrently(List.of(
            new Approval(groupOne.getId(), advisor),
            new Approval(groupTwo.getId(), advisor)));

        assertEquals(1, outcomes.stream().filter(Boolean::booleanValue).count());
        assertEquals(1, registrations.countByTopicAndStatuses(topic.getId(), List.of(RegistrationStatus.APPROVED)));
    }

    private StudentGroup group(String code, User leader, RegistrationPeriod period) {
        StudentGroup group = new StudentGroup();
        group.setGroupCode(code);
        group.setGroupName(code);
        group.setLeader(leader);
        group.setPeriod(period);
        groups.save(group);
        members.save(new GroupMember(group, leader, "LEADER", period));
        return group;
    }

    private User user(String prefix, String suffix, Role role, Department department) {
        User user = new User();
        user.setUserCode(prefix + suffix);
        user.setUsername(prefix + suffix);
        user.setPasswordHash("unused-test-hash");
        user.setFullName(prefix + " " + suffix);
        user.setEmail(prefix + suffix + "@example.test");
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.setDepartment(department);
        return users.save(user);
    }

    private List<Boolean> approveConcurrently(List<Approval> approvals) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(approvals.size());
        CountDownLatch ready = new CountDownLatch(approvals.size());
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<Boolean>> results = approvals.stream().map(approval -> executor.submit(() -> {
                ready.countDown();
                if (!start.await(10, TimeUnit.SECONDS))
                    throw new IllegalStateException("Concurrent approval start timed out.");
                try {
                    registrationService.decidePending(approval.groupId(), RegistrationStatus.APPROVED,
                        "Đồng ý", approval.advisor());
                    return true;
                } catch (IllegalArgumentException expected) {
                    return false;
                }
            })).toList();
            assertTrue(ready.await(10, TimeUnit.SECONDS), "All approvals should reach the start barrier.");
            start.countDown();
            return List.of(results.get(0).get(20, TimeUnit.SECONDS), results.get(1).get(20, TimeUnit.SECONDS));
        } finally {
            start.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS), "Approval workers should terminate.");
        }
    }

    private record Approval(Long groupId, User advisor) {}
}
