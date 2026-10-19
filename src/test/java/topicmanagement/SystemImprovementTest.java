package topicmanagement;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

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
import topicmanagement.dto.request.AnnouncementRequest;
import topicmanagement.dto.request.TopicRequest;
import topicmanagement.exception.ConflictException;
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
    @Autowired NotificationService notificationService;
    @Autowired AnnouncementServiceV2 announcementService;
    @Autowired TopicService topicService;
    @Autowired DepartmentServiceV2 departmentService;
    @Autowired RegistrationPeriodServiceV2 registrationPeriodService;

    @Test
    void duplicateTopicTitleGetsSpecificMessageIgnoringCaseAndSurroundingSpaces() {
        Fixture fixture = fixture(false);
        TopicRequest request = new TopicRequest();
        request.setTopicCode("NEW-" + suffix());
        request.setTitle("  " + fixture.topic.getTitle().toUpperCase() + "  ");
        request.setDescription("Mô tả đủ dài để kiểm thử đề tài.");
        request.setMaxStudents(3);
        request.setTopicType("COURSE");
        request.setDepartmentId(fixture.department.getId());
        request.setPeriodId(fixture.period.getId());
        var error = assertThrows(ConflictException.class,
            () -> topicService.createTopic(request, fixture.advisor));
        assertEquals("Tên đề tài đã tồn tại. Vui lòng nhập tên khác.", error.getMessage());
    }

    @Test
    void departmentAnnouncementReachesOnlyItsStudentsAndReadStateIsPerUser() {
        Fixture fixture = fixture(false);
        User dean = user("deanNotice", Role.DEAN, fixture.department);
        User studentTwo = user("studentTwo", Role.STUDENT, fixture.department);
        Department otherDepartment = new Department();
        otherDepartment.setCode("OTHER" + suffix().substring(0, 6));
        otherDepartment.setName("Bộ môn khác " + suffix());
        departments.save(otherDepartment);
        User outsider = user("outside", Role.STUDENT, otherDepartment);
        var notice = announcementService.createForDepartment(
            new AnnouncementRequest("Lịch họp", "Sinh viên xem lịch họp.", AnnouncementAudience.STUDENT), dean);

        assertTrue(notificationService.feed(fixture.student).items().stream().anyMatch(item -> item.id().equals(notice.id())));
        assertTrue(notificationService.feed(studentTwo).items().stream().anyMatch(item -> item.id().equals(notice.id())));
        assertFalse(notificationService.feed(outsider).items().stream().anyMatch(item -> item.id().equals(notice.id())));
        assertThrows(AccessDeniedException.class, () -> notificationService.markRead(outsider, notice.id()));
        notificationService.markRead(fixture.student, notice.id());
        notificationService.markRead(fixture.student, notice.id());
        assertTrue(notificationService.feed(fixture.student).items().stream()
            .filter(item -> item.id().equals(notice.id())).findFirst().orElseThrow().read());
        assertFalse(notificationService.feed(studentTwo).items().stream()
            .filter(item -> item.id().equals(notice.id())).findFirst().orElseThrow().read());
        assertThrows(AccessDeniedException.class, () -> announcementService.createForDepartment(
            new AnnouncementRequest("Sai quyền", "Không hợp lệ", AnnouncementAudience.LECTURER), dean));
        announcementService.delete(notice.id());
        assertFalse(notificationService.feed(fixture.student).items().stream()
            .anyMatch(item -> item.id().equals(notice.id())));
    }

    @Test
    void advisorFeedbackIsVisibleToStudentsButCannotBeEditedByAnotherLecturer() throws Exception {
        Fixture fixture = fixture(true);
        Report report = upload(fixture);
        assertNull(reportService.listForStudent(fixture.student, fixture.group).getFirst().advisorFeedback());
        var updated = reportService.saveAdvisorFeedback(fixture.advisor, report.id, "Cần bổ sung phần kết luận.");
        assertEquals("Cần bổ sung phần kết luận.", updated.advisorFeedback());
        assertEquals(fixture.advisor.getFullName(), updated.feedbackBy());
        assertEquals(updated.advisorFeedback(),
            reportService.listForStudent(fixture.student, fixture.group).getFirst().advisorFeedback());
        assertThrows(AccessDeniedException.class,
            () -> reportService.saveAdvisorFeedback(fixture.otherLecturer, report.id, "Sửa trái phép"));
    }

    @Test
    void studentReportEditsKeepPreviousFileAndAdvisorCanReviewStatus() throws Exception {
        Fixture fixture = fixture(true);
        Report report = upload(fixture);
        byte[] originalBytes = report.content.clone();
        var replacement = new MockMultipartFile("file", "report-v2.pdf", "application/pdf",
            "%PDF-1.5\n2 0 obj\n<<>>\nendobj\ntrailer\n<<>>\n%%EOF".getBytes());

        var edited = reportService.updateStudentReport(fixture.student, report.id,
            "Đã bổ sung phần phương pháp.", replacement);
        assertEquals("report-v2.pdf", edited.filename());
        assertEquals("Đã bổ sung phần phương pháp.", edited.note());
        assertEquals(ReportStatus.PENDING, edited.status());
        assertEquals(1, edited.editCount());

        var historyItems = reportService.editHistoryForAdvisor(fixture.advisor, report.id);
        assertEquals(1, historyItems.size());
        assertEquals("report.pdf", historyItems.getFirst().filename());
        var previousVersion = reportService.editVersionDownloadForAdvisor(
            fixture.advisor, report.id, historyItems.getFirst().id());
        assertArrayEquals(originalBytes, previousVersion.content);

        var rejected = reportService.reviewReport(fixture.advisor, report.id,
            ReportStatus.REJECTED, "Bổ sung thêm kết quả thử nghiệm.");
        assertEquals(ReportStatus.REJECTED, rejected.status());
        assertEquals("Bổ sung thêm kết quả thử nghiệm.", rejected.advisorFeedback());

        var resubmitted = reportService.updateStudentReport(fixture.student, report.id,
            "Đã bổ sung kết quả thử nghiệm.", null);
        assertEquals(ReportStatus.PENDING, resubmitted.status());
        assertEquals("report-v2.pdf", resubmitted.filename(), "Bỏ trống tệp mới thì giữ tệp hiện tại.");
        assertEquals(2, reportService.editHistoryForAdvisor(fixture.advisor, report.id).size());

        reportService.reviewReport(fixture.advisor, report.id, ReportStatus.APPROVED, "Đạt yêu cầu.");
        assertThrows(IllegalArgumentException.class,
            () -> reportService.updateStudentReport(fixture.student, report.id, "Sửa sau duyệt", null));
        assertThrows(IllegalArgumentException.class,
            () -> reportService.reviewReport(fixture.advisor, report.id, ReportStatus.REJECTED, " "));
        assertThrows(AccessDeniedException.class,
            () -> reportService.reviewReport(fixture.otherLecturer, report.id, ReportStatus.APPROVED, "Duyệt trái phép"));
        assertThrows(AccessDeniedException.class,
            () -> reportService.editHistoryForAdvisor(fixture.otherLecturer, report.id));
    }

    @Test
    void studentCannotEditReportAfterSubmissionDeadline() throws Exception {
        Fixture fixture = fixture(true);
        Report report = upload(fixture);
        fixture.period.setStudentEndAt(LocalDateTime.now().minusSeconds(1));
        periods.saveAndFlush(fixture.period);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
            () -> reportService.updateStudentReport(fixture.student, report.id, "Sửa quá hạn", null));
        assertTrue(error.getMessage().contains("quá hạn"));
    }

    @Test
    void studentEditAndAdvisorReviewHttpEndpointsWorkWithRoleChecks() throws Exception {
        Fixture fixture = fixture(true);
        Report report = upload(fixture);
        var editRequest = multipart("/api/student/reports/{id}", report.id)
            .param("note", "Nội dung đã cập nhật qua API.")
            .with(authentication(auth(fixture.student)))
            .with(csrf())
            .with(request -> { request.setMethod("PUT"); return request; });
        mvc.perform(editRequest).andExpect(status().isOk());
        assertEquals("Nội dung đã cập nhật qua API.",
            reportRepository.findById(report.id).orElseThrow().note);

        mvc.perform(get("/api/lecturer/reports/{id}/history", report.id)
                .with(authentication(auth(fixture.advisor))))
            .andExpect(status().isOk());
        mvc.perform(put("/api/lecturer/reports/{id}/review", report.id)
                .with(authentication(auth(fixture.advisor)))
                .with(csrf())
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"status\":\"APPROVED\",\"feedback\":\"Đạt yêu cầu.\"}"))
            .andExpect(status().isOk());
        mvc.perform(get("/api/lecturer/reports/{id}/history", report.id)
                .with(authentication(auth(fixture.otherLecturer))))
            .andExpect(status().isForbidden());
        mvc.perform(put("/api/lecturer/reports/{id}/review", report.id)
                .with(authentication(auth(fixture.otherLecturer)))
                .with(csrf())
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"status\":\"APPROVED\",\"feedback\":\"Không được duyệt.\"}"))
            .andExpect(status().isForbidden());
    }

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
        assertEquals(1, departmentService.findPage(fixture.department.getCode(), 0, 1).content().size());
        assertEquals(1, registrationPeriodService.findPage(fixture.period.getName(), null, 0, 1).content().size());
        assertTrue(announcementService.findPage("", null, 0, 1).content().size() <= 1);
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
