package topicmanagement;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import topicmanagement.council.*;
import topicmanagement.entity.*;
import topicmanagement.enums.*;
import topicmanagement.exception.ConflictException;
import topicmanagement.repository.*;
import topicmanagement.security.CurrentUser;
import topicmanagement.service.TopicRegistrationService;
import topicmanagement.student.StudentService;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:period-council-regression;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop", "spring.profiles.active=test"})
@Transactional
class PeriodAndCouncilRegressionTest {
    @Autowired UserRepository users;
    @Autowired DepartmentRepository departments;
    @Autowired RegistrationPeriodRepository periods;
    @Autowired TopicRepository topics;
    @Autowired StudentGroupRepository groups;
    @Autowired GroupMemberRepository members;
    @Autowired TopicRegistrationRepository registrations;
    @Autowired TopicRegistrationService registrationService;
    @Autowired StudentService students;
    @Autowired CouncilService councilService;
    @Autowired CouncilRepository councils;
    @Autowired topicmanagement.service.StudentResultService results;
    @Autowired topicmanagement.notification.EmailNotificationRepository emailNotifications;
    @Autowired topicmanagement.council.DefenseRepository defenseRows;
    @Autowired topicmanagement.export.ResultExportService exports;
    Department department;
    User dean, advisor, advisor2, chair, secretary, reviewer, student;
    RegistrationPeriod period;
    Topic topic;
    LocalDateTime now;

    @BeforeEach void seed() {
        now = LocalDateTime.now();
        department = new Department(); department.setCode("REG"); department.setName("Regression");
        departments.save(department);
        dean = user("dean", Role.DEAN); advisor = user("advisor", Role.LECTURER);
        advisor2 = user("advisor2", Role.LECTURER); chair = user("chair", Role.LECTURER);
        secretary = user("secretary", Role.LECTURER); reviewer = user("reviewer", Role.LECTURER);
        student = user("student", Role.STUDENT);
        period = period("Original");
        topic = new Topic(); topic.setTopicCode("REG-TOPIC"); topic.setTitle("Regression topic");
        topic.setDepartment(department); topic.setPeriod(period); topic.setTopicType(RegistrationPeriodType.COURSE);
        topic.setCreatedBy(advisor); topic.setAdvisor1(advisor); topic.setAdvisor2(advisor2);
        topic.setMaxStudents(3); topic.setStatus(TopicStatus.APPROVED); topics.save(topic);
        var principal = CurrentUser.from(dean);
        SecurityContextHolder.getContext().setAuthentication(
            UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities()));
    }
    @AfterEach void clearAuthentication() { SecurityContextHolder.clearContext(); }

    @Test void overlappingPeriodsCannotBeUsedToRegisterAnOlderTopic() {
        RegistrationPeriod newer = period("Newer");
        students.createGroup("student", "New group");
        var group = groups.findAll().getFirst();
        assertEquals(newer.getId(), group.getPeriod().getId());
        assertThrows(ConflictException.class, () -> students.register("student", topic.getTopicCode()));
        assertEquals(0, registrations.count());
        assertNull(group.getTopic());
    }

    @Test void samePeriodRegistrationStillWorksWithOneStudent() {
        students.createGroup("student", "Valid group");
        students.register("student", topic.getTopicCode());
        var group = groups.findAll().getFirst();
        assertEquals(topic.getId(), group.getTopic().getId());
        assertEquals(period.getId(), members.findByGroupId(group.getId()).getFirst().getRegistrationPeriod().getId());
        assertEquals(RegistrationStatus.PENDING, registrations.findAll().getFirst().getStatus());
    }

    @Test void studentCanExplicitlyChooseAnOlderOpenPeriodForANewGroup() {
        period("Newer overlapping period");
        students.createGroup("student", "Chosen period", period.getId());
        students.register("student", topic.getTopicCode());
        var view = students.state("student");
        assertEquals(period.getId(), view.group().periodId());
        assertEquals(period.getId(), view.registration().topic().periodId());
        assertEquals(3, view.registration().topic().capacity());
    }

    @Test void resultPeriodChoicesContainOnlyTheStudentsOwnMemberships() {
        period("Not joined");
        group("OWN", period);
        var principal = CurrentUser.from(student);
        SecurityContextHolder.getContext().setAuthentication(
            UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities()));
        assertEquals(1, results.periodOptions().size());
        assertEquals(period.getId(), results.periodOptions().getFirst().id());
        assertTrue(students.periodChoices("student").stream().filter(p -> p.id().equals(period.getId()))
            .findFirst().orElseThrow().joined());
    }

    @Test void explicitGroupCreationOutsideTheStudentWindowIsRejected() {
        period.setStudentEndAt(now.minusHours(1));
        assertThrows(IllegalArgumentException.class,
            () -> students.createGroup("student", "Closed", period.getId()));
        assertEquals(0, groups.count());
    }

    @Test void legacyGroupAndMembershipAreBoundToTheTopicPeriodTogether() {
        StudentGroup group = group("LEGACY", null);
        registrationService.submit(group, topic, student);
        assertEquals(period.getId(), group.getPeriod().getId());
        assertEquals(period.getId(), members.findByGroupId(group.getId()).getFirst().getRegistrationPeriod().getId());
    }

    @Test void legacyMembershipCannotBypassOneGroupPerActualPeriod() {
        StudentGroup old = group("OLD", null); old.setTopic(topic);
        StudentGroup another = group("ANOTHER", null);
        assertThrows(ConflictException.class, () -> registrationService.submit(another, topic, student));
    }

    @Test void updatingAssignedCouncilCannotAddEitherSupervisor() {
        Long id = assignedCouncil();
        for (User supervisor : List.of(advisor, advisor2)) {
            var invalid = List.of(member(chair, CouncilRole.CHAIRPERSON), member(supervisor, CouncilRole.SECRETARY),
                member(reviewer, CouncilRole.REVIEWER));
            assertThrows(ConflictException.class, () -> councilService.update(id, input(invalid, now.plusDays(3), "B1")));
        }
        assertTrue(councils.findById(id).orElseThrow().getMembers().stream()
            .anyMatch(m -> m.getLecturer().getId().equals(secretary.getId())));
    }

    @Test void assignedReviewerCannotBeRemovedOrLoseReviewerRole() {
        Long id = assignedCouncil(); User alternate = user("alternate", Role.LECTURER);
        var invalid = List.of(member(chair, CouncilRole.CHAIRPERSON), member(secretary, CouncilRole.SECRETARY),
            member(reviewer, CouncilRole.MEMBER), member(alternate, CouncilRole.REVIEWER));
        assertThrows(ConflictException.class, () -> councilService.update(id, input(invalid, now.plusDays(3), "B1")));
    }

    @Test void changingCouncilDateMustRespectEveryAssignedPeriod() {
        period.setDefenseDate(now.plusDays(3).toLocalDate());
        Long id = assignedCouncil();
        assertThrows(ConflictException.class, () -> councilService.update(id, input(roster(), now.plusDays(4), "B1")));
    }

    @Test void validRoomChangeBeforeGradingRemainsAllowed() {
        Long id = assignedCouncil();
        councilService.update(id, input(roster(), now.plusDays(3), "B2"));
        assertEquals("B2", councils.findById(id).orElseThrow().getRoom());
    }

    @Test void publicationQueuesOneEmailForEachStudentAndIsIdempotent() {
        Long councilId=assignedCouncil();
        var defense=defenseRows.findByCouncilId(councilId).getFirst();
        var second=user("secondStudent",Role.STUDENT);
        members.save(new GroupMember(defense.group,second,"MEMBER",period));
        defense.finalized=true;defense.finalScore=new java.math.BigDecimal("8.50");
        councilService.publish(defense.id);
        councilService.publish(defense.id);
        assertEquals(2,emailNotifications.count());
        var recipient=emailNotifications.findAll().stream().filter(n->n.student.getId().equals(student.getId())).findFirst().orElseThrow();
        assertEquals(student.getEmail(),recipient.recipient);
        assertEquals(topicmanagement.notification.EmailNotification.Status.PENDING,recipient.status);
        assertEquals(0,recipient.attempts);
        assertTrue(recipient.body.contains("8.50"));
        assertTrue(recipient.body.contains("\nĐợt: Original"));
    }

    @Test void unpublishedResultCannotQueueAnEmail() {
        Long councilId=assignedCouncil();
        var defense=defenseRows.findByCouncilId(councilId).getFirst();
        assertThrows(IllegalArgumentException.class,()->councilService.publish(defense.id));
        assertFalse(defense.published);
        assertEquals(0,emailNotifications.count());
    }

    @Test void exportsIncludeOnlyPublishedResultsInTheSelectedPeriod() throws Exception {
        Long councilId=assignedCouncil();var defense=defenseRows.findByCouncilId(councilId).getFirst();
        defense.finalized=true;defense.finalScore=new java.math.BigDecimal("8.50");
        assertTrue(exports.rows(period.getId()).isEmpty());
        councilService.publish(defense.id);
        assertEquals(1,exports.rows(period.getId()).size());
        assertEquals(student.getUserCode(),exports.rows(period.getId()).getFirst().studentCode());
        assertTrue(exports.rows(period("Other period").getId()).isEmpty());
        try(var workbook=new org.apache.poi.xssf.usermodel.XSSFWorkbook(new java.io.ByteArrayInputStream(exports.excel(period.getId())))) {
            assertEquals(8.5,workbook.getSheetAt(0).getRow(1).getCell(7).getNumericCellValue());
        }
    }

    @Test void studentPdfUsesOnlyTheAuthenticatedStudentsPublishedResult() throws Exception {
        Long councilId=assignedCouncil();var defense=defenseRows.findByCouncilId(councilId).getFirst();
        var principal=CurrentUser.from(student);
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(principal,null,principal.getAuthorities()));
        assertThrows(IllegalArgumentException.class,()->exports.studentPdf(period.getId()));
        assertThrows(org.springframework.security.access.AccessDeniedException.class,()->exports.rows(period.getId()));
        defense.finalized=true;defense.finalScore=new java.math.BigDecimal("8.50");defense.published=true;
        try(var document=org.apache.pdfbox.Loader.loadPDF(exports.studentPdf(period.getId()))) {
            String text=new org.apache.pdfbox.text.PDFTextStripper().getText(document);
            assertTrue(text.contains("8.50"));assertTrue(text.contains("Regression topic"));
        }
        var outsider=user("outsiderPdf",Role.STUDENT);principal=CurrentUser.from(outsider);
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(principal,null,principal.getAuthorities()));
        assertThrows(IllegalArgumentException.class,()->exports.studentPdf(period.getId()));
    }

    private Long assignedCouncil() {
        StudentGroup group = group("ASSIGNED", period); group.setTopic(topic); group.setStatus(GroupStatus.APPROVED);
        councilService.create(input(roster(), now.plusDays(3), "B1"));
        Long id = councils.findAll().getFirst().getId();
        councilService.assign(group.getId(), id, reviewer.getId());
        return id;
    }
    private CouncilService.CreateInput input(List<CouncilService.MemberInput> roster, LocalDateTime date, String room) {
        return new CouncilService.CreateInput("REG-COUNCIL", "Regression council", date, room, roster);
    }
    private List<CouncilService.MemberInput> roster() {
        return List.of(member(chair, CouncilRole.CHAIRPERSON), member(secretary, CouncilRole.SECRETARY),
            member(reviewer, CouncilRole.REVIEWER));
    }
    private CouncilService.MemberInput member(User user, CouncilRole role) {
        return new CouncilService.MemberInput(user.getId(), role);
    }
    private StudentGroup group(String code, RegistrationPeriod p) {
        var group = new StudentGroup(); group.setGroupCode(code); group.setGroupName(code);
        group.setLeader(student); group.setPeriod(p); groups.save(group);
        members.save(new GroupMember(group, student, "LEADER", p));
        return group;
    }
    private RegistrationPeriod period(String name) {
        var p = new RegistrationPeriod(); p.setName(name); p.setType(RegistrationPeriodType.COURSE);
        p.setLecturerStartAt(now.minusDays(4)); p.setLecturerEndAt(now.minusDays(3));
        p.setStudentStartAt(now.minusDays(2)); p.setStudentEndAt(now.plusDays(7));
        return periods.save(p);
    }
    private User user(String name, Role role) {
        var u = new User(); u.setUsername(name); u.setUserCode(name); u.setFullName(name);
        u.setEmail(name + "@example.test"); u.setPasswordHash("unused"); u.setRole(role);
        u.setStatus(UserStatus.ACTIVE); u.setDepartment(department); return users.save(u);
    }
}
