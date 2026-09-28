package topicmanagement;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import topicmanagement.council.*;
import topicmanagement.dto.request.*;
import topicmanagement.dto.response.*;
import topicmanagement.entity.*;
import topicmanagement.enums.*;
import topicmanagement.exception.ConflictException;
import topicmanagement.repository.*;
import topicmanagement.security.AccountRefreshFilter;
import topicmanagement.security.CurrentUser;
import topicmanagement.service.*;
import topicmanagement.student.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:regression-test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.profiles.active=test"
})
@AutoConfigureMockMvc
@Transactional
public class AuditFixesRegressionTest {

    @Autowired private DepartmentRepository departments;
    @Autowired private RegistrationPeriodRepository periods;
    @Autowired private UserRepository users;
    @Autowired private TopicRepository topics;
    @Autowired private StudentGroupRepository groups;
    @Autowired private GroupMemberRepository members;
    @Autowired private TopicRegistrationRepository registrations;
    @Autowired private CouncilRepository councils;
    @Autowired private DefenseRepository defenses;
    @Autowired private ReportRepository reportRepository;
    @Autowired private InvitationRepository invitations;

    @Autowired private TopicService topicService;
    @Autowired private DepartmentTopicService departmentTopicService;
    @Autowired private TopicRegistrationService registrationService;
    @Autowired private ReportService reportService;
    @Autowired private StudentService studentService;
    @Autowired private UserService userService;
    @Autowired private RegistrationPeriodServiceV2 registrationPeriodService;
    @Autowired private CouncilService councilService;
    @Autowired private DefenseService defenseService;
    @Autowired private StudentResultService studentResultService;
    @Autowired private SecurityContextRepository securityContextRepository;

    @Autowired private EntityManager entityManager;
    @Autowired private MockMvc mvc;

    private static final byte[] VALID_PDF =
        "%PDF-1.4\n1 0 obj\n<<>>\nendobj\ntrailer\n<<>>\n%%EOF".getBytes();

    // -------------------------------------------------------------------------
    // Scenario 1: Registration cancellation lifecycle
    // -------------------------------------------------------------------------
    @Test
    void testScenario1_RegistrationCancellationLifecycle_BlockedAfterReportOrDefense() throws Exception {
        Department dept = createDept("D1");
        RegistrationPeriod period = createPeriod(RegistrationPeriodType.COURSE, null, null);
        User advisor = createUser("adv1", Role.LECTURER, dept);
        User student1 = createUser("stu1", Role.STUDENT, dept);
        User dean = createUser("dean1", Role.DEAN, dept);

        Topic topic1 = createTopic(dept, period, advisor, "T1", TopicStatus.APPROVED);
        StudentGroup group1 = createGroup(student1, "G1", period, topic1, GroupStatus.APPROVED);

        registrationService.submit(group1, topic1, student1);
        registrationService.decidePending(group1.getId(), RegistrationStatus.APPROVED, "Duyệt", advisor);

        // Upload a report
        MockMultipartFile file = new MockMultipartFile("file", "report.pdf", "application/pdf", VALID_PDF);
        reportService.upload(student1, group1, "Đề cương", "Báo cáo đề cương", file);
        entityManager.flush();

        // Cancellation must be blocked because report exists
        ConflictException exReport = assertThrows(ConflictException.class, () ->
            registrationService.cancelByStudent(group1, student1, "Muốn đổi đề tài")
        );
        assertTrue(exReport.getMessage().contains("báo cáo tiến độ"));

        // Setup second group with defense scheduled
        User student2 = createUser("stu2", Role.STUDENT, dept);
        Topic topic2 = createTopic(dept, period, advisor, "T2", TopicStatus.APPROVED);
        StudentGroup group2 = createGroup(student2, "G2", period, topic2, GroupStatus.APPROVED);
        registrationService.submit(group2, topic2, student2);
        registrationService.decidePending(group2.getId(), RegistrationStatus.APPROVED, "Duyệt", advisor);

        User chair = createUser("chair1", Role.LECTURER, dept);
        User secretary = createUser("sec1", Role.LECTURER, dept);
        User reviewer = createUser("rev1", Role.LECTURER, dept);
        Council council = createCouncil("HD01", chair, secretary, reviewer);

        // Assign defense as DEAN
        SecurityContextHolder.getContext().setAuthentication(auth(dean));
        defenseService.assign(group2.getId(), council.getId(), reviewer.getId());
        entityManager.flush();

        // Cancellation must be blocked because defense is scheduled
        ConflictException exDefense = assertThrows(ConflictException.class, () ->
            registrationService.cancelByStudent(group2, student2, "Muốn hủy")
        );
        assertTrue(exDefense.getMessage().contains("lịch bảo vệ") || exDefense.getMessage().contains("điểm"));
    }

    // -------------------------------------------------------------------------
    // Scenario 2: Defense/report linkage to topic registration
    // -------------------------------------------------------------------------
    @Test
    void testScenario2_DefenseReportLinkage_CancelledRegistrationDoesNotLeak() {
        Department dept = createDept("D2");
        RegistrationPeriod period = createPeriod(RegistrationPeriodType.COURSE, null, null);
        User advisor = createUser("adv2", Role.LECTURER, dept);
        User student = createUser("stu2b", Role.STUDENT, dept);
        User dean = createUser("dean2", Role.DEAN, dept);

        Topic topic = createTopic(dept, period, advisor, "T2B", TopicStatus.APPROVED);
        StudentGroup group = createGroup(student, "G2B", period, topic, GroupStatus.APPROVED);

        var reg = registrationService.submit(group, topic, student);
        registrationService.decidePending(group.getId(), RegistrationStatus.APPROVED, "Duyệt", advisor);

        User chair = createUser("chair2", Role.LECTURER, dept);
        User secretary = createUser("sec2", Role.LECTURER, dept);
        User reviewer = createUser("rev2", Role.LECTURER, dept);
        Council council = createCouncil("HD02", chair, secretary, reviewer);

        SecurityContextHolder.getContext().setAuthentication(auth(dean));
        defenseService.assign(group.getId(), council.getId(), reviewer.getId());
        entityManager.flush();

        Defense defense = defenses.findByGroupId(group.getId()).orElseThrow();
        assertNotNull(defense.registration);
        assertNotNull(defense.topic);
        assertEquals(reg.getId(), defense.registration.getId());
        assertEquals(topic.getId(), defense.topic.getId());

        // Finalize & publish score
        defense.finalScore = new BigDecimal("8.5");
        defense.finalized = true;
        defense.published = true;
        defenses.save(defense);

        // If the registration is active/approved, student can view result
        SecurityContextHolder.getContext().setAuthentication(auth(student));
        StudentResultResponse resultActive = studentResultService.current();
        assertTrue(resultActive.published());
        assertEquals(new BigDecimal("8.5"), resultActive.score());
        assertEquals(topic.getTitle(), resultActive.topic());

        // If the registration is cancelled, results are suppressed (never leaks)
        defense.registration.setStatus(RegistrationStatus.CANCELLED);
        registrations.save(defense.registration);

        StudentResultResponse resultCancelled = studentResultService.current();
        assertFalse(resultCancelled.published());
    }

    // -------------------------------------------------------------------------
    // Scenario 3: Group membership scoped per registration period
    // -------------------------------------------------------------------------
    @Test
    void testScenario3_GroupMembershipScopedPerRegistrationPeriod() {
        Department dept = createDept("D3");
        RegistrationPeriod period1 = createPeriod(RegistrationPeriodType.COURSE, null, null);
        RegistrationPeriod period2 = createPeriod(RegistrationPeriodType.COURSE, null, null);
        User student = createUser("stu3", Role.STUDENT, dept);

        // Period 1 group
        StudentGroup group1 = new StudentGroup();
        group1.setGroupCode("G3-P1");
        group1.setGroupName("Group P1");
        group1.setLeader(student);
        group1.setPeriod(period1);
        groups.save(group1);
        members.save(new GroupMember(group1, student, "LEADER", period1));

        // Period 2 group: student joins another group in a DIFFERENT period
        StudentGroup group2 = new StudentGroup();
        group2.setGroupCode("G3-P2");
        group2.setGroupName("Group P2");
        group2.setLeader(student);
        group2.setPeriod(period2);
        groups.save(group2);

        // Saving membership in period 2 does NOT throw unique constraint violation!
        assertDoesNotThrow(() -> {
            members.save(new GroupMember(group2, student, "LEADER", period2));
            entityManager.flush();
        });

        // But in period 1, member check prevents joining another group in same period
        assertTrue(members.existsByRegistrationPeriodIdAndStudentId(period1.getId(), student.getId()));
    }

    // -------------------------------------------------------------------------
    // Scenario 4: Topic approval rejects inactive creator / invalid supervisor
    // -------------------------------------------------------------------------
    @Test
    void testScenario4_TopicApprovalRejectsInactiveCreatorOrInvalidSupervisor() {
        Department deptA = createDept("D4A");
        Department deptB = createDept("D4B");
        RegistrationPeriod period = createLecturerPeriod(RegistrationPeriodType.COURSE);
        User hod = createUser("hod4", Role.HEAD_OF_DEPT, deptA);

        // Inactive creator
        User inactiveLecturer = createUser("inactive4", Role.LECTURER, deptA);
        inactiveLecturer.setStatus(UserStatus.INACTIVE);
        users.save(inactiveLecturer);

        Topic topic1 = new Topic();
        topic1.setTopicCode("T4A");
        topic1.setTitle("De tai inactive");
        topic1.setDescription("Mo ta de tai du dai");
        topic1.setTopicType(RegistrationPeriodType.COURSE);
        topic1.setStatus(TopicStatus.PENDING);
        topic1.setDepartment(deptA);
        topic1.setPeriod(period);
        topic1.setCreatedBy(inactiveLecturer);
        topics.save(topic1);

        TopicApprovalRequest approveReq = new TopicApprovalRequest();
        approveReq.setStatus("APPROVED");

        // Approval fails because creator is inactive
        IllegalArgumentException exInactive = assertThrows(IllegalArgumentException.class, () ->
            departmentTopicService.approveOrRejectTopic(topic1.getId(), approveReq, hod)
        );
        assertTrue(exInactive.getMessage().contains("không hoạt động"));

        // Supervisor from different department
        User lecturerDeptB = createUser("lect4b", Role.LECTURER, deptB);
        Topic topic2 = new Topic();
        topic2.setTopicCode("T4B");
        topic2.setTitle("De tai wrong dept");
        topic2.setDescription("Mo ta de tai du dai");
        topic2.setTopicType(RegistrationPeriodType.COURSE);
        topic2.setStatus(TopicStatus.PENDING);
        topic2.setDepartment(deptA);
        topic2.setPeriod(period);
        topic2.setCreatedBy(hod);
        topic2.setAdvisor1(lecturerDeptB); // Dept B advisor for Dept A topic
        topics.save(topic2);

        IllegalArgumentException exDept = assertThrows(IllegalArgumentException.class, () ->
            departmentTopicService.approveOrRejectTopic(topic2.getId(), approveReq, hod)
        );
        assertTrue(exDept.getMessage().contains("không thuộc bộ môn"));
    }

    // -------------------------------------------------------------------------
    // Scenario 5: Case-insensitive username/email uniqueness prevents collision
    // -------------------------------------------------------------------------
    @Test
    void testScenario5_CaseInsensitiveUsernameAndEmailUniqueness() {
        Department dept = createDept("D5");

        // Create first user
        userService.create(new UserRequest(
            "U5A", "student_unique", "Student Unique",
            "student_unique@hcmute.edu.vn", Role.STUDENT, null,
            UserStatus.ACTIVE, "Demo@12345"
        ));

        // Duplicate username in uppercase
        ConflictException exUser = assertThrows(ConflictException.class, () ->
            userService.create(new UserRequest(
                "U5B", "STUDENT_UNIQUE", "Student Duplicate",
                "other@hcmute.edu.vn", Role.STUDENT, null,
                UserStatus.ACTIVE, "Demo@12345"
            ))
        );
        assertTrue(exUser.getMessage().contains("Tên đăng nhập"));

        // Duplicate email in uppercase
        ConflictException exEmail = assertThrows(ConflictException.class, () ->
            userService.create(new UserRequest(
                "U5C", "student_other", "Student Other",
                "STUDENT_UNIQUE@HCMUTE.EDU.VN", Role.STUDENT, null,
                UserStatus.ACTIVE, "Demo@12345"
            ))
        );
        assertTrue(exEmail.getMessage().contains("Email đã được sử dụng"));

        // Lecturer without departmentId must be rejected
        IllegalArgumentException exDept = assertThrows(IllegalArgumentException.class, () ->
            userService.create(new UserRequest(
                "U5D", "lecturer_nodept", "Lecturer NoDept",
                "nodept@hcmute.edu.vn", Role.LECTURER, null,
                UserStatus.ACTIVE, "Demo@12345"
            ))
        );
        assertTrue(exDept.getMessage().contains("bộ môn"));
    }

    // -------------------------------------------------------------------------
    // Scenario 6: Password change invalidates active user sessions
    // -------------------------------------------------------------------------
    @Test
    void testScenario6_PasswordChangeInvalidatesSessionViaAccountRefreshFilter() throws Exception {
        Department dept = createDept("D6");
        User user = createUser("u6", Role.STUDENT, dept);

        // User authenticated 5 minutes ago
        LocalDateTime authTime = LocalDateTime.now().minusMinutes(5);
        CurrentUser principal = CurrentUser.from(user, authTime);
        var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Update password now -> updates passwordChangedAt to now
        userService.update(user.getId(), new UserRequest(
            user.getUserCode(), user.getUsername(), user.getFullName(),
            user.getEmail(), user.getRole(), user.getDepartment() != null ? user.getDepartment().getId() : null,
            user.getStatus(), "NewPassword123!"
        ));
        entityManager.flush();

        // Run AccountRefreshFilter
        AccountRefreshFilter filter = new AccountRefreshFilter(users, securityContextRepository);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);

        filter.doFilter(request, response, new MockFilterChain());

        // Session must be invalidated and security context cleared
        assertTrue(session.isInvalid());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    // -------------------------------------------------------------------------
    // Scenario 7: Report submission enforces stage progression and PDF structure
    // -------------------------------------------------------------------------
    @Test
    void testScenario7_ReportSubmissionEnforcesStageProgressionAndPdfStructure() throws Exception {
        Department dept = createDept("D7");
        RegistrationPeriod period = createPeriod(RegistrationPeriodType.COURSE, null, null);
        User advisor = createUser("adv7", Role.LECTURER, dept);
        User student = createUser("stu7", Role.STUDENT, dept);

        Topic topic = createTopic(dept, period, advisor, "T7", TopicStatus.APPROVED);
        StudentGroup group = createGroup(student, "G7", period, topic, GroupStatus.APPROVED);
        registrationService.submit(group, topic, student);
        registrationService.decidePending(group.getId(), RegistrationStatus.APPROVED, "Duyệt", advisor);

        MockMultipartFile validFile = new MockMultipartFile("file", "rep.pdf", "application/pdf", VALID_PDF);
        MockMultipartFile invalidPdf = new MockMultipartFile("file", "fake.pdf", "application/pdf", "%PDF-broken".getBytes());

        // 1. Stage progression check: cannot upload "Giữa kỳ" before "Đề cương"
        IllegalArgumentException exStage = assertThrows(IllegalArgumentException.class, () ->
            reportService.upload(student, group, "Giữa kỳ", "Note", validFile)
        );
        assertTrue(exStage.getMessage().contains("Cần nộp báo cáo Đề cương"));

        // 2. Corrupt PDF check
        IllegalArgumentException exCorrupt = assertThrows(IllegalArgumentException.class, () ->
            reportService.upload(student, group, "Đề cương", "Note", invalidPdf)
        );
        assertTrue(exCorrupt.getMessage().contains("không phải PDF hoặc DOCX hợp lệ"));

        // 3. Valid "Đề cương" upload succeeds
        assertDoesNotThrow(() ->
            reportService.upload(student, group, "Đề cương", "Đề cương hoàn chỉnh", validFile)
        );
        entityManager.flush();

        // 4. Now "Giữa kỳ" can be uploaded
        assertDoesNotThrow(() ->
            reportService.upload(student, group, "Giữa kỳ", "Giữa kỳ hoàn chỉnh", validFile)
        );
    }

    // -------------------------------------------------------------------------
    // Scenario 8: Registration period update forbids changing type when topics exist
    // -------------------------------------------------------------------------
    @Test
    void testScenario8_RegistrationPeriodUpdateForbidsChangingTypeWhenTopicsExist() {
        Department dept = createDept("D8");
        RegistrationPeriod period = createPeriod(RegistrationPeriodType.COURSE, null, null);
        User advisor = createUser("adv8", Role.LECTURER, dept);
        createTopic(dept, period, advisor, "T8", TopicStatus.PENDING);

        LocalDateTime now = LocalDateTime.now();
        RegistrationPeriodRequest updateReq = new RegistrationPeriodRequest(
            "Period Renamed", RegistrationPeriodType.KLTN,
            now.minusDays(5), now.minusDays(3),
            now.minusDays(2), now.plusDays(5),
            now.plusDays(7), LocalDate.now().plusDays(10)
        );

        // Changing period type with existing topics is forbidden
        ConflictException exUpdate = assertThrows(ConflictException.class, () ->
            registrationPeriodService.update(period.getId(), updateReq)
        );
        assertTrue(exUpdate.getMessage().contains("Không thể đổi loại đợt đăng ký"));

        // Deleting period with existing topics is forbidden
        ConflictException exDelete = assertThrows(ConflictException.class, () ->
            registrationPeriodService.delete(period.getId())
        );
        assertTrue(exDelete.getMessage().contains("Không thể xóa đợt đăng ký"));
    }

    // -------------------------------------------------------------------------
    // Scenario 9: Council creation rejects null member in members list with HTTP 400
    // -------------------------------------------------------------------------
    @Test
    void testScenario9_CouncilCreationRejectsNullMemberWithBadRequest() throws Exception {
        Department dept = createDept("D9");
        User dean = createUser("dean9", Role.DEAN, dept);

        String invalidCouncilJson = """
            {
              "code": "HD-NULL",
              "name": "Hội đồng chứa phần tử null",
              "defenseDate": "2026-12-01T09:00:00",
              "room": "A1-101",
              "members": [null]
            }
            """;

        mvc.perform(post("/api/councils")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidCouncilJson)
                .with(authentication(auth(dean))))
            .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // Scenario 10: DEAN creates topic for another dept, Council CRUD, Group ops
    // -------------------------------------------------------------------------
    @Test
    void testScenario10_DeanCrossDepartmentTopic_CouncilCrud_GroupOperations() throws Exception {
        Department deptA = createDept("D10A");
        Department deptB = createDept("D10B");
        RegistrationPeriod period = createLecturerPeriod(RegistrationPeriodType.COURSE);
        User dean = createUser("dean10", Role.DEAN, deptA);
        User lecturerA = createUser("lect10a", Role.LECTURER, deptA);

        // 1. DEAN from Dept A can create topic for Dept B
        TopicRequest deanTopicReq = new TopicRequest();
        deanTopicReq.setTopicCode("T10-DEAN");
        deanTopicReq.setTitle("Topic for Dept B by Dean");
        deanTopicReq.setDescription("Mô tả hợp lệ trên 10 ký tự");
        deanTopicReq.setDepartmentId(deptB.getId());
        deanTopicReq.setPeriodId(period.getId());
        deanTopicReq.setTopicType("COURSE");
        deanTopicReq.setMaxStudents(2);

        assertDoesNotThrow(() -> topicService.createTopic(deanTopicReq, dean));

        // Normal lecturer from Dept A cannot create for Dept B
        TopicRequest lectTopicReq = new TopicRequest();
        lectTopicReq.setTopicCode("T10-LECT");
        lectTopicReq.setTitle("Topic for Dept B by Lecturer");
        lectTopicReq.setDescription("Mô tả hợp lệ trên 10 ký tự");
        lectTopicReq.setDepartmentId(deptB.getId());
        lectTopicReq.setPeriodId(period.getId());
        lectTopicReq.setTopicType("COURSE");
        lectTopicReq.setMaxStudents(2);

        assertThrows(AccessDeniedException.class, () ->
            topicService.createTopic(lectTopicReq, lecturerA)
        );

        // 2. Council PUT and DELETE
        User chair = createUser("chair10", Role.LECTURER, deptA);
        User sec = createUser("sec10", Role.LECTURER, deptA);
        User rev = createUser("rev10", Role.LECTURER, deptA);
        Council council = createCouncil("HD10", chair, sec, rev);

        LocalDateTime futureDate = LocalDateTime.now().plusDays(10);
        String updateCouncilJson = String.format("""
            {
              "code": "HD10-UPDATED",
              "name": "Hội đồng đổi tên",
              "defenseDate": "%s",
              "room": "B2-202",
              "members": [
                {"userId": %d, "role": "CHAIRPERSON"},
                {"userId": %d, "role": "SECRETARY"},
                {"userId": %d, "role": "REVIEWER"}
              ]
            }
            """, futureDate.toString(), chair.getId(), sec.getId(), rev.getId());

        // PUT update council
        mvc.perform(put("/api/councils/{id}", council.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateCouncilJson)
                .with(authentication(auth(dean))))
            .andExpect(status().isOk());

        Council updated = councils.findById(council.getId()).orElseThrow();
        assertEquals("HD10-UPDATED", updated.getCode());
        assertEquals("Hội đồng đổi tên", updated.getName());

        // DELETE council
        mvc.perform(delete("/api/councils/{id}", council.getId())
                .with(csrf())
                .with(authentication(auth(dean))))
            .andExpect(status().isOk());
        assertTrue(councils.findById(council.getId()).isEmpty());

        // 3. Group Operations: leaveGroup, removeMember, disbandGroup
        User studentLeader = createUser("lead10", Role.STUDENT, deptA);
        User studentMember = createUser("mem10", Role.STUDENT, deptA);

        studentService.createGroup(studentLeader.getUsername(), "Nhóm Kiểm Thử");
        studentService.invite(studentLeader.getUsername(), studentMember.getUserCode());

        Invitation inv = invitations.findByGroupIdAndStudentId(
            groups.findByLeaderId(studentLeader.getId()).orElseThrow().getId(),
            studentMember.getId()
        ).orElseThrow();
        studentService.respond(studentMember.getUsername(), inv.id, true);

        StudentGroup activeGroup = groups.findByLeaderId(studentLeader.getId()).orElseThrow();
        assertEquals(2, members.findByGroupId(activeGroup.getId()).size());

        // Leader removes member
        studentService.removeMember(studentLeader.getUsername(), studentMember.getUserCode());
        assertEquals(1, members.findByGroupId(activeGroup.getId()).size());

        // Member gets invited again and joins
        studentService.invite(studentLeader.getUsername(), studentMember.getUserCode());
        Invitation inv2 = invitations.findByGroupIdAndStudentId(activeGroup.getId(), studentMember.getId()).orElseThrow();
        studentService.respond(studentMember.getUsername(), inv2.id, true);
        assertEquals(2, members.findByGroupId(activeGroup.getId()).size());

        // Member leaves group
        studentService.leaveGroup(studentMember.getUsername());
        assertEquals(1, members.findByGroupId(activeGroup.getId()).size());

        // Leader disbands group
        studentService.disbandGroup(studentLeader.getUsername());
        assertTrue(groups.findById(activeGroup.getId()).isEmpty());
    }

    // -------------------------------------------------------------------------
    // Scenario 11: Multi-period student result selection (historical resolution)
    // -------------------------------------------------------------------------
    @Test
    void testScenario11_MultiPeriodStudentResultSelection_PrioritizesCurrentPeriodAndAllowsSpecificSelection() throws Exception {
        Department dept = createDept("D11");
        LocalDateTime now = LocalDateTime.now();

        // Period 1 (earlier completed period in the past)
        RegistrationPeriod period1 = new RegistrationPeriod();
        period1.setName("Period 1 Past " + UUID.randomUUID().toString().substring(0, 4));
        period1.setType(RegistrationPeriodType.COURSE);
        period1.setLecturerStartAt(now.minusDays(30));
        period1.setLecturerEndAt(now.minusDays(20));
        period1.setStudentStartAt(now.minusDays(19));
        period1.setStudentEndAt(now.minusDays(5));
        period1.setDefenseDate(now.minusDays(3).toLocalDate());
        periods.save(period1);

        // Period 2 (current active period)
        RegistrationPeriod period2 = new RegistrationPeriod();
        period2.setName("Period 2 Current " + UUID.randomUUID().toString().substring(0, 4));
        period2.setType(RegistrationPeriodType.KLTN);
        period2.setLecturerStartAt(now.minusDays(10));
        period2.setLecturerEndAt(now.minusDays(5));
        period2.setStudentStartAt(now.minusDays(4));
        period2.setStudentEndAt(now.plusDays(10));
        period2.setReviewDeadline(now.plusDays(12));
        period2.setDefenseDate(now.plusDays(15).toLocalDate());
        periods.save(period2);

        User advisor1 = createUser("adv11_1", Role.LECTURER, dept);
        User advisor2 = createUser("adv11_2", Role.LECTURER, dept);
        User student = createUser("stu11", Role.STUDENT, dept);
        User dean = createUser("dean11", Role.DEAN, dept);

        // Group 1 in Period 1 (earlier group)
        Topic topic1 = createTopic(dept, period1, advisor1, "TOPIC-P1", TopicStatus.APPROVED);
        StudentGroup group1 = createGroup(student, "GRP-P1", period1, topic1, GroupStatus.APPROVED);
        registrationService.submit(group1, topic1, student);
        registrationService.decidePending(group1.getId(), RegistrationStatus.APPROVED, "Duyet P1", advisor1);

        // Council & Defense for Group 1 in Period 1 (Score: 7.50)
        User chair1 = createUser("chair11_1", Role.LECTURER, dept);
        User sec1 = createUser("sec11_1", Role.LECTURER, dept);
        User rev1 = createUser("rev11_1", Role.LECTURER, dept);
        Council council1 = createCouncil("HD11-P1", period1.getDefenseDate().atTime(9, 0), chair1, sec1, rev1);

        SecurityContextHolder.getContext().setAuthentication(auth(dean));
        defenseService.assign(group1.getId(), council1.getId(), rev1.getId());
        Defense def1 = defenses.findByGroupId(group1.getId()).orElseThrow();
        def1.finalScore = new BigDecimal("7.50");
        def1.finalized = true;
        def1.published = true;
        defenses.save(def1);

        // Group 2 in Period 2 (current period)
        Topic topic2 = createTopic(dept, period2, advisor2, "TOPIC-P2", TopicStatus.APPROVED);
        StudentGroup group2 = createGroup(student, "GRP-P2", period2, topic2, GroupStatus.APPROVED);
        registrationService.submit(group2, topic2, student);
        registrationService.decidePending(group2.getId(), RegistrationStatus.APPROVED, "Duyet P2", advisor2);

        // Council & Defense for Group 2 in Period 2 (Score: 9.50)
        User chair2 = createUser("chair11_2", Role.LECTURER, dept);
        User sec2 = createUser("sec11_2", Role.LECTURER, dept);
        User rev2 = createUser("rev11_2", Role.LECTURER, dept);
        Council council2 = createCouncil("HD11-P2", period2.getDefenseDate().atTime(9, 0), chair2, sec2, rev2);

        defenseService.assign(group2.getId(), council2.getId(), rev2.getId());
        Defense def2 = defenses.findByGroupId(group2.getId()).orElseThrow();
        def2.finalScore = new BigDecimal("9.50");
        def2.finalized = true;
        def2.published = true;
        defenses.save(def2);

        entityManager.flush();

        // 1. Current result lookup by student:
        // Must prioritize active/current Period 2 and NEVER return the earliest random group (Period 1)
        SecurityContextHolder.getContext().setAuthentication(auth(student));
        StudentResultResponse currentResult = studentResultService.current();
        assertTrue(currentResult.published());
        assertEquals(new BigDecimal("9.50"), currentResult.score());
        assertEquals(topic2.getTitle(), currentResult.topic());

        // 2. Allow explicitly selecting completed Period 1
        StudentResultResponse resultPeriod1 = studentResultService.current(period1.getId());
        assertTrue(resultPeriod1.published());
        assertEquals(new BigDecimal("7.50"), resultPeriod1.score());
        assertEquals(topic1.getTitle(), resultPeriod1.topic());

        // 3. Allow explicitly selecting Period 2
        StudentResultResponse resultPeriod2 = studentResultService.current(period2.getId());
        assertTrue(resultPeriod2.published());
        assertEquals(new BigDecimal("9.50"), resultPeriod2.score());
        assertEquals(topic2.getTitle(), resultPeriod2.topic());

        // 4. Verify REST endpoint /api/student/result with and without periodId
        mvc.perform(get("/api/student/result").with(authentication(auth(student))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.published").value(true))
            .andExpect(jsonPath("$.score").value(9.50))
            .andExpect(jsonPath("$.topic").value(topic2.getTitle()));

        mvc.perform(get("/api/student/result?periodId=" + period1.getId()).with(authentication(auth(student))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.published").value(true))
            .andExpect(jsonPath("$.score").value(7.50))
            .andExpect(jsonPath("$.topic").value(topic1.getTitle()));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------
    private Department createDept(String code) {
        Department d = new Department();
        d.setCode(code + "-" + UUID.randomUUID().toString().substring(0, 4));
        d.setName("Department " + code);
        return departments.save(d);
    }

    private RegistrationPeriod createLecturerPeriod(RegistrationPeriodType type) {
        LocalDateTime now = LocalDateTime.now();
        RegistrationPeriod p = new RegistrationPeriod();
        p.setName("LecturerPeriod " + UUID.randomUUID().toString().substring(0, 4));
        p.setType(type);
        p.setLecturerStartAt(now.minusDays(2));
        p.setLecturerEndAt(now.plusDays(2));
        p.setStudentStartAt(now.plusDays(3));
        p.setStudentEndAt(now.plusDays(10));
        return periods.save(p);
    }

    private RegistrationPeriod createPeriod(RegistrationPeriodType type, LocalDateTime reviewDeadline, LocalDate defenseDate) {
        LocalDateTime now = LocalDateTime.now();
        RegistrationPeriod p = new RegistrationPeriod();
        p.setName("Period " + UUID.randomUUID().toString().substring(0, 4));
        p.setType(type);
        p.setLecturerStartAt(now.minusDays(5));
        p.setLecturerEndAt(now.minusDays(2));
        p.setStudentStartAt(now.minusDays(1));
        p.setStudentEndAt(now.plusDays(10));
        p.setReviewDeadline(reviewDeadline);
        p.setDefenseDate(defenseDate);
        return periods.save(p);
    }

    private User createUser(String prefix, Role role, Department dept) {
        String uid = UUID.randomUUID().toString().substring(0, 6);
        User u = new User();
        u.setUserCode(prefix + "_" + uid);
        u.setUsername((prefix + "_" + uid).toLowerCase());
        u.setFullName(prefix + " Full");
        u.setEmail((prefix + "_" + uid + "@example.com").toLowerCase());
        u.setPasswordHash("$2a$10$abcdefghijklmnopqrstuvwxyz123456");
        u.setRole(role);
        u.setStatus(UserStatus.ACTIVE);
        u.setDepartment(dept);
        return users.save(u);
    }

    private Topic createTopic(Department dept, RegistrationPeriod period, User advisor, String code, TopicStatus status) {
        Topic t = new Topic();
        t.setTopicCode(code + "-" + UUID.randomUUID().toString().substring(0, 4));
        t.setTitle("Topic " + code);
        t.setDescription("Mô tả đề tài hợp lệ trên 10 ký tự");
        t.setTopicType(period.getType());
        t.setStatus(status);
        t.setDepartment(dept);
        t.setPeriod(period);
        t.setCreatedBy(advisor);
        t.setAdvisor1(advisor);
        t.setMaxStudents(3);
        return topics.save(t);
    }

    private StudentGroup createGroup(User leader, String code, RegistrationPeriod period, Topic topic, GroupStatus status) {
        StudentGroup g = new StudentGroup();
        g.setGroupCode("GRP-" + code + "-" + UUID.randomUUID().toString().substring(0, 4));
        g.setGroupName("Group " + code);
        g.setLeader(leader);
        g.setPeriod(period);
        g.setTopic(topic);
        g.setStatus(status);
        groups.save(g);
        GroupMember gm = members.save(new GroupMember(g, leader, "LEADER", period));
        g.getMembers().add(gm);
        return g;
    }

    private Council createCouncil(String code, User chair, User secretary, User reviewer) {
        return createCouncil(code, LocalDateTime.now().plusDays(7), chair, secretary, reviewer);
    }

    private Council createCouncil(String code, LocalDateTime defenseDate, User chair, User secretary, User reviewer) {
        Council c = new Council();
        c.setCode(code + "-" + UUID.randomUUID().toString().substring(0, 4));
        c.setName("Council " + code);
        c.setDefenseDate(defenseDate);
        c.setRoom("A1-101");
        councils.save(c);

        c.getMembers().add(new CouncilMember(c, chair, CouncilRole.CHAIRPERSON));
        c.getMembers().add(new CouncilMember(c, secretary, CouncilRole.SECRETARY));
        c.getMembers().add(new CouncilMember(c, reviewer, CouncilRole.REVIEWER));
        return councils.save(c);
    }

    private UsernamePasswordAuthenticationToken auth(User user) {
        CurrentUser principal = CurrentUser.from(user);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }
}
