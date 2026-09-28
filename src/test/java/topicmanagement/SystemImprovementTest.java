package topicmanagement;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import topicmanagement.council.*;
import topicmanagement.dto.request.AdvisorQuotaRequest;
import topicmanagement.entity.*;
import topicmanagement.enums.*;
import topicmanagement.repository.*;
import topicmanagement.security.CurrentUser;
import topicmanagement.service.*;
import topicmanagement.student.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:improvement-test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.generate_statistics=true",
    "spring.profiles.active=test"
})
@AutoConfigureMockMvc
@Transactional
class SystemImprovementTest {
    @Autowired DepartmentRepository departments;
    @Autowired RegistrationPeriodRepository periods;
    @Autowired UserRepository users;
    @Autowired TopicRepository topics;
    @Autowired StudentGroupRepository groups;
    @Autowired GroupMemberRepository members;
    @Autowired TopicRegistrationRepository registrations;
    @Autowired RegistrationStatusHistoryRepository history;
    @Autowired CouncilRepository councils;
    @Autowired DefenseRepository defenses;
    @Autowired ReportRepository reportRepository;
    @Autowired TopicRegistrationService registrationService;
    @Autowired AdvisorQuotaService quotaService;
    @Autowired ReportService reportService;
    @Autowired StudentService studentService;
    @Autowired UserService userService;
    @Autowired EntityManager entityManager;
    @Autowired MockMvc mvc;

    @Test
    void advisorCanViewAndDownloadButAnotherLecturerGets403() throws Exception {
        Fixture fixture = fixture(true);
        Report report = upload(fixture);

        mvc.perform(get("/api/lecturer/reports/{id}", report.id).with(authentication(auth(fixture.advisor))))
            .andExpect(status().isOk());
        mvc.perform(get("/api/lecturer/reports/{id}/download", report.id).with(authentication(auth(fixture.advisor))))
            .andExpect(status().isOk());
        mvc.perform(get("/api/lecturer/reports/{id}", report.id).with(authentication(auth(fixture.otherLecturer))))
            .andExpect(status().isForbidden());
        assertThrows(AccessDeniedException.class,
            () -> reportService.downloadForAdvisor(fixture.otherLecturer, report.id));
    }

    @Test
    void councilMemberCanViewOnlyAssignedDefenseReport() throws Exception {
        Fixture fixture = fixture(true);
        Report report = upload(fixture);
        Council council = new Council("HD-" + suffix(), "Hội đồng kiểm thử", LocalDateTime.now().plusDays(1), "A1");
        council.getMembers().add(new CouncilMember(council, fixture.otherLecturer, CouncilRole.REVIEWER));
        councils.save(council);
        Defense defense = new Defense();
        defense.group = fixture.group;
        defense.council = council;
        defense.reviewer = fixture.otherLecturer;
        defenses.save(defense);
        entityManager.flush();

        mvc.perform(get("/api/council/reports/{id}", report.id)
                .with(authentication(auth(fixture.otherLecturer))))
            .andExpect(status().isOk());
        User outsider = user("outside", Role.LECTURER, fixture.department);
        mvc.perform(get("/api/council/reports/{id}", report.id).with(authentication(auth(outsider))))
            .andExpect(status().isForbidden());
    }

    @Test
    void cancellingRegistrationKeepsStatusHistoryAndAllowsAnotherTopic() {
        Fixture fixture = fixture(false);
        var first = registrationService.submit(fixture.group, fixture.topic, fixture.student);
        registrationService.cancelByStudent(fixture.group, fixture.student, "Đổi hướng nghiên cứu");
        Topic secondTopic = topic(fixture, "T2-" + suffix());
        registrationService.submit(fixture.group, secondTopic, fixture.student);

        var all = registrationService.listForGroup(fixture.group.getId());
        assertEquals(2, all.size());
        assertEquals(RegistrationStatus.CANCELLED,
            registrations.findById(first.getId()).orElseThrow().getStatus());
        assertTrue(history.findByRegistrationIdOrderByChangedAtDesc(first.getId()).stream()
            .anyMatch(item -> item.getNewStatus() == RegistrationStatus.CANCELLED));
    }

    @Test
    void advisorQuotaIsCheckedInsideRegistrationApproval() {
        Fixture fixture = fixture(false);
        quotaService.save(new AdvisorQuotaRequest(fixture.period.getId(), fixture.advisor.getId(), 1));
        registrationService.submit(fixture.group, fixture.topic, fixture.student);
        registrationService.decidePending(fixture.group.getId(), RegistrationStatus.APPROVED, "Đồng ý", fixture.advisor);

        User secondStudent = user("student2", Role.STUDENT, fixture.department);
        StudentGroup secondGroup = group(secondStudent, "G2-" + suffix());
        registrationService.submit(secondGroup, fixture.topic, secondStudent);
        assertThrows(IllegalArgumentException.class, () -> registrationService.decidePending(
            secondGroup.getId(), RegistrationStatus.APPROVED, "Vượt quota", fixture.advisor));
    }

    @Test
    void paginatedListsReturnBoundedContentAndTotals() throws Exception {
        Fixture fixture = fixture(false);
        for (int i = 0; i < 5; i++) user("page" + i, Role.STUDENT, fixture.department);
        var page = userService.findPage("", Role.STUDENT, fixture.department.getId(), 0, 2);
        assertEquals(2, page.content().size());
        assertTrue(page.totalElements() >= 6);
        assertTrue(page.totalPages() >= 3);
        var catalogPage = studentService.catalogPage("Đề tài", "", "", 0, 1);
        assertEquals(1, catalogPage.content().size());
        assertTrue(catalogPage.totalElements() >= 1);
        mvc.perform(get("/api/councils/page?page=0&size=2").with(authentication(auth(fixture.advisor))))
            .andExpect(status().isOk());
    }

    @Test
    void reportListUsesMetadataProjectionWithoutLoadingBlobEntity() throws Exception {
        Fixture fixture = fixture(true);
        Report report = upload(fixture);
        entityManager.flush();
        entityManager.clear();
        var statistics = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        var summaries = reportRepository.findSummariesByGroupId(fixture.group.getId(), PageRequest.of(0, 10));
        assertEquals(1, summaries.getTotalElements());
        assertEquals(report.reportSize.longValue(), summaries.getContent().getFirst().size());
        assertNotNull(summaries.getContent().getFirst().checksum());
        assertEquals(0, statistics.getEntityLoadCount(), "Projection danh sách không được hydrate entity chứa BLOB.");
    }

    private Report upload(Fixture fixture) throws Exception {
        byte[] bytes = "%PDF-1.4\n1 0 obj\n<<>>\nendobj\ntrailer\n<<>>\n%%EOF".getBytes();
        reportService.upload(fixture.student, fixture.group, "Đề cương", "Bản kiểm thử",
            new MockMultipartFile("file", "report.pdf", "application/pdf", bytes));
        entityManager.flush();
        return reportRepository.findAll().stream().filter(item -> item.group.getId().equals(fixture.group.getId()))
            .findFirst().orElseThrow();
    }

    private Fixture fixture(boolean approvedGroup) {
        String suffix = suffix();
        Department department = new Department();
        department.setCode("D" + suffix.substring(0, 6));
        department.setName("Bộ môn " + suffix);
        departments.save(department);
        User advisor = user("advisor" + suffix, Role.LECTURER, department);
        User other = user("other" + suffix, Role.LECTURER, department);
        User student = user("student" + suffix, Role.STUDENT, department);
        RegistrationPeriod period = period(suffix);
        Topic topic = topic(department, period, advisor, "T-" + suffix);
        StudentGroup group = group(student, "G-" + suffix);
        group.setTopic(topic);
        group.setStatus(approvedGroup ? GroupStatus.APPROVED : GroupStatus.DRAFT);
        groups.save(group);
        return new Fixture(department, period, advisor, other, student, topic, group);
    }

    private RegistrationPeriod period(String suffix) {
        LocalDateTime now = LocalDateTime.now();
        RegistrationPeriod period = new RegistrationPeriod();
        period.setName("Đợt " + suffix);
        period.setType(RegistrationPeriodType.COURSE);
        period.setLecturerStartAt(now.minusDays(2));
        period.setLecturerEndAt(now.plusDays(2));
        period.setStudentStartAt(now.minusDays(1));
        period.setStudentEndAt(now.plusDays(5));
        return periods.save(period);
    }

    private Topic topic(Fixture fixture, String code) {
        return topic(fixture.department, fixture.period, fixture.advisor, code);
    }

    private Topic topic(Department department, RegistrationPeriod period, User advisor, String code) {
        Topic topic = new Topic();
        topic.setTopicCode(code);
        topic.setTitle("Đề tài " + code);
        topic.setDescription("Mô tả kiểm thử");
        topic.setTopicType(RegistrationPeriodType.COURSE);
        topic.setStatus(TopicStatus.APPROVED);
        topic.setDepartment(department);
        topic.setPeriod(period);
        topic.setCreatedBy(advisor);
        topic.setAdvisor1(advisor);
        return topics.save(topic);
    }

    private StudentGroup group(User leader, String code) {
        StudentGroup group = new StudentGroup();
        group.setGroupCode(code);
        group.setGroupName("Nhóm " + code);
        group.setLeader(leader);
        groups.save(group);
        GroupMember membership = members.save(new GroupMember(group, leader, "LEADER"));
        group.getMembers().add(membership);
        return group;
    }

    private User user(String prefix, Role role, Department department) {
        String suffix = suffix();
        User user = new User();
        user.setUserCode(prefix + suffix);
        user.setUsername(prefix + suffix);
        user.setPasswordHash("unused");
        user.setFullName(prefix + " " + suffix);
        user.setEmail(prefix + suffix + "@example.test");
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.setDepartment(department);
        return users.save(user);
    }

    private UsernamePasswordAuthenticationToken auth(User user) {
        CurrentUser principal = CurrentUser.from(user);
        return UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());
    }

    private String suffix() { return UUID.randomUUID().toString().replace("-", "").substring(0, 10); }

    private record Fixture(Department department, RegistrationPeriod period, User advisor,
            User otherLecturer, User student, Topic topic, StudentGroup group) {}
}
