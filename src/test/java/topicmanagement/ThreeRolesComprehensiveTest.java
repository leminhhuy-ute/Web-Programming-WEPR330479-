package topicmanagement;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import topicmanagement.council.*;
import topicmanagement.dto.request.*;
import topicmanagement.entity.*;
import topicmanagement.enums.*;
import topicmanagement.repository.*;
import topicmanagement.security.CurrentUser;
import topicmanagement.service.*;
import topicmanagement.student.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:three-roles-test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.profiles.active=test"
})
@Transactional
public class ThreeRolesComprehensiveTest {

    @Autowired DepartmentRepository departments;
    @Autowired RegistrationPeriodRepository periods;
    @Autowired UserRepository users;
    @Autowired TopicRepository topics;
    @Autowired StudentGroupRepository groups;
    @Autowired GroupMemberRepository members;
    @Autowired CouncilRepository councils;
    @Autowired DefenseRepository defenses;
    @Autowired DepartmentTopicService departmentTopicService;
    @Autowired StudentGroupService studentGroupService;
    @Autowired StudentService studentService;
    @Autowired TopicService topicService;
    @Autowired GradeService gradeService;
    @Autowired AnnouncementServiceV2 announcementService;
    @Autowired EntityManager em;

    private Department dept;
    private RegistrationPeriod period;
    private User dean;
    private User advisorLecturer;
    private User otherLecturer;
    private User studentLeader;
    private User studentMember;

    private String uid() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private User makeUser(String prefix, Role role, Department d) {
        String u = prefix + "_" + uid();
        User user = new User();
        user.setUsername(u);
        user.setEmail(u + "@test.edu.vn");
        user.setFullName("User " + u);
        user.setUserCode("C" + uid());
        user.setPasswordHash("hash123");
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.setDepartment(d);
        return users.save(user);
    }

    private void authAs(User u) {
        CurrentUser principal = CurrentUser.from(u);
        SecurityContextHolder.getContext().setAuthentication(
            UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities()));
    }

    @BeforeEach
    void setup() {
        dept = new Department();
        dept.setCode("CNTT" + uid().substring(0, 4));
        dept.setName("Bộ môn Công nghệ Phần mềm " + uid());
        dept = departments.save(dept);

        LocalDateTime now = LocalDateTime.now();
        period = new RegistrationPeriod();
        period.setName("Đợt 1 - 2026");
        period.setType(RegistrationPeriodType.COURSE);
        period.setLecturerStartAt(now.minusDays(5));
        period.setLecturerEndAt(now.plusDays(5));
        period.setStudentStartAt(now.plusDays(1));
        period.setStudentEndAt(now.plusDays(5));
        period = periods.save(period);

        dean = makeUser("dean", Role.DEAN, dept);
        advisorLecturer = makeUser("adv", Role.LECTURER, dept);
        otherLecturer = makeUser("oth", Role.LECTURER, dept);
        studentLeader = makeUser("lead", Role.STUDENT, dept);
        studentMember = makeUser("mem", Role.STUDENT, dept);
    }

    @Test
    @DisplayName("Yêu cầu 1 & 2: Hệ thống chỉ có đúng 3 vai trò (DEAN, LECTURER, STUDENT); không còn HEAD_OF_DEPT")
    void test1_OnlyThreeRolesExist() {
        Role[] roles = Role.values();
        assertEquals(3, roles.length, "Role enum must contain exactly 3 roles");
        assertTrue(Arrays.stream(roles).anyMatch(r -> r == Role.DEAN));
        assertTrue(Arrays.stream(roles).anyMatch(r -> r == Role.LECTURER));
        assertTrue(Arrays.stream(roles).anyMatch(r -> r == Role.STUDENT));
        assertFalse(Arrays.stream(roles).anyMatch(r -> r.name().equals("HEAD_OF_DEPT")));

        AnnouncementAudience[] audiences = AnnouncementAudience.values();
        assertEquals(3, audiences.length, "AnnouncementAudience must contain exactly 3 audiences");
        assertTrue(Arrays.stream(audiences).anyMatch(a -> a == AnnouncementAudience.ALL));
        assertTrue(Arrays.stream(audiences).anyMatch(a -> a == AnnouncementAudience.LECTURER));
        assertTrue(Arrays.stream(audiences).anyMatch(a -> a == AnnouncementAudience.STUDENT));
        assertFalse(Arrays.stream(audiences).anyMatch(a -> a.name().equals("HEAD_OF_DEPT")));
    }

    @Test
    @DisplayName("Yêu cầu 3: DEAN có thẩm quyền duyệt đề tài và gán GVHD")
    void test3_DeanCanApproveTopicsAndAssignAdvisors() {
        Topic topic = new Topic();
        topic.setTopicCode("TOP_" + uid());
        topic.setTitle("Đề tài nghiên cứu ứng dụng AI");
        topic.setDescription("Mô tả chi tiết đề tài nghiên cứu");
        topic.setTopicType(RegistrationPeriodType.COURSE);
        topic.setStatus(TopicStatus.PENDING);
        topic.setDepartment(dept);
        topic.setPeriod(period);
        topic.setCreatedBy(advisorLecturer);
        Topic savedTopic = topics.save(topic);

        authAs(dean);
        TopicApprovalRequest approveReq = new TopicApprovalRequest();
        approveReq.setStatus("APPROVED");
        assertDoesNotThrow(() -> departmentTopicService.approveOrRejectTopic(savedTopic.getId(), approveReq, dean));

        AssignAdvisorsRequest assignReq = new AssignAdvisorsRequest();
        assignReq.setAdvisor1Id(advisorLecturer.getId());
        assignReq.setAdvisor2Id(otherLecturer.getId());
        assertDoesNotThrow(() -> departmentTopicService.assignAdvisors(savedTopic.getId(), assignReq, dean));

        Topic updated = topics.findById(savedTopic.getId()).orElseThrow();
        assertEquals(TopicStatus.APPROVED, updated.getStatus());
        assertEquals(advisorLecturer.getId(), updated.getAdvisor1().getId());
        assertEquals(otherLecturer.getId(), updated.getAdvisor2().getId());
    }

    @Test
    @DisplayName("Yêu cầu 4: LECTURER bị cấm thực hiện các quyền quản trị của Trưởng khoa (Duyệt đề tài, Gán GVHD)")
    void test4_LecturerForbiddenFromDeanAdministrativeTasks() {
        Topic topic = new Topic();
        topic.setTopicCode("TOP_" + uid());
        topic.setTitle("Đề tài do giảng viên đề xuất");
        topic.setDescription("Mô tả chi tiết đề tài nghiên cứu");
        topic.setTopicType(RegistrationPeriodType.COURSE);
        topic.setStatus(TopicStatus.PENDING);
        topic.setDepartment(dept);
        topic.setPeriod(period);
        topic.setCreatedBy(advisorLecturer);
        Topic savedTopic = topics.save(topic);

        authAs(otherLecturer);
        TopicApprovalRequest approveReq = new TopicApprovalRequest();
        approveReq.setStatus("APPROVED");

        assertThrows(AccessDeniedException.class, () ->
            departmentTopicService.approveOrRejectTopic(savedTopic.getId(), approveReq, otherLecturer)
        );

        AssignAdvisorsRequest assignReq = new AssignAdvisorsRequest();
        assignReq.setAdvisor1Id(advisorLecturer.getId());
        assertThrows(AccessDeniedException.class, () ->
            departmentTopicService.assignAdvisors(savedTopic.getId(), assignReq, otherLecturer)
        );

        assertThrows(AccessDeniedException.class, () ->
            announcementService.createForDepartment(new AnnouncementRequest("Tiêu đề", "Nội dung", AnnouncementAudience.STUDENT), otherLecturer)
        );
    }

    @Test
    @DisplayName("Yêu cầu 5: LECTURER chỉ được xem và xử lý nhóm do mình hướng dẫn; không được xem nhóm của GV khác")
    void test5_LecturerCanOnlyProcessTheirAdvisedGroups() {
        Topic advisedTopic = new Topic();
        advisedTopic.setTopicCode("TOP_ADV_" + uid());
        advisedTopic.setTitle("Đề tài có GVHD");
        advisedTopic.setDescription("Mô tả chi tiết đề tài có hướng dẫn");
        advisedTopic.setTopicType(RegistrationPeriodType.COURSE);
        advisedTopic.setStatus(TopicStatus.APPROVED);
        advisedTopic.setDepartment(dept);
        advisedTopic.setPeriod(period);
        advisedTopic.setCreatedBy(advisorLecturer);
        advisedTopic.setAdvisor1(advisorLecturer);
        Topic savedAdvisedTopic = topics.save(advisedTopic);

        StudentGroup myGroup = new StudentGroup();
        myGroup.setGroupName("Nhóm hướng dẫn 1");
        myGroup.setGroupCode("GRP_" + uid());
        myGroup.setLeader(studentLeader);
        myGroup.setStatus(GroupStatus.APPROVED);
        myGroup.setPeriod(period);
        myGroup.setTopic(savedAdvisedTopic);
        StudentGroup savedMyGroup = groups.save(myGroup);

        Topic otherTopic = new Topic();
        otherTopic.setTopicCode("TOP_OTH_" + uid());
        otherTopic.setTitle("Đề tài của GV khác");
        otherTopic.setDescription("Mô tả chi tiết đề tài của GV khác");
        otherTopic.setTopicType(RegistrationPeriodType.COURSE);
        otherTopic.setStatus(TopicStatus.APPROVED);
        otherTopic.setDepartment(dept);
        otherTopic.setPeriod(period);
        otherTopic.setCreatedBy(otherLecturer);
        otherTopic.setAdvisor1(otherLecturer);
        Topic savedOtherTopic = topics.save(otherTopic);

        StudentGroup otherGroup = new StudentGroup();
        otherGroup.setGroupName("Nhóm của GV khác");
        otherGroup.setGroupCode("GRP_OTH_" + uid());
        otherGroup.setLeader(studentMember);
        otherGroup.setStatus(GroupStatus.APPROVED);
        otherGroup.setPeriod(period);
        otherGroup.setTopic(savedOtherTopic);
        StudentGroup savedOtherGroup = groups.save(otherGroup);

        authAs(advisorLecturer);
        // Advisor can access their advised group guidance
        assertDoesNotThrow(() -> studentGroupService.getGuidance(savedMyGroup.getId(), advisorLecturer));

        // Advisor CANNOT access group they do not advise
        assertThrows(AccessDeniedException.class, () ->
            studentGroupService.getGuidance(savedOtherGroup.getId(), advisorLecturer)
        );

        // getGroupsForLecturer only returns groups they advise
        var myGroups = studentGroupService.getGroupsForLecturer(advisorLecturer);
        assertEquals(1, myGroups.size());
        assertEquals(savedMyGroup.getId(), myGroups.get(0).getId());

        // DEAN can access any group
        authAs(dean);
        assertDoesNotThrow(() -> studentGroupService.getGuidance(savedOtherGroup.getId(), dean));
        var allGroups = studentGroupService.getGroupsForLecturer(dean);
        assertTrue(allGroups.size() >= 2);
    }

    @Test
    @DisplayName("Yêu cầu 6: Giảng viên hướng dẫn không được chấm điểm đề tài mình đang hướng dẫn trong hội đồng")
    void test6_AdvisorCannotGradeTheirOwnAdvisedTopic() {
        Topic topic = new Topic();
        topic.setTopicCode("TOP_COUNCIL_" + uid());
        topic.setTitle("Đề tài bảo vệ trước hội đồng");
        topic.setDescription("Mô tả chi tiết đề tài bảo vệ");
        topic.setTopicType(RegistrationPeriodType.COURSE);
        topic.setStatus(TopicStatus.APPROVED);
        topic.setDepartment(dept);
        topic.setPeriod(period);
        topic.setCreatedBy(advisorLecturer);
        topic.setAdvisor1(advisorLecturer);
        topic = topics.save(topic);

        StudentGroup group = new StudentGroup();
        group.setGroupName("Nhóm bảo vệ");
        group.setGroupCode("GRP_DEF_" + uid());
        group.setLeader(studentLeader);
        group.setStatus(GroupStatus.APPROVED);
        group.setPeriod(period);
        group.setTopic(topic);
        group = groups.save(group);

        Council council = new Council("C_01", "Hội đồng 01", LocalDateTime.now().plusDays(2), "A1-101");
        council.setStatus(CouncilStatus.READY);
        council = councils.save(council);

        CouncilMember m1 = new CouncilMember(council, advisorLecturer, CouncilRole.MEMBER);
        CouncilMember m2 = new CouncilMember(council, otherLecturer, CouncilRole.CHAIRPERSON);
        council.getMembers().add(m1);
        council.getMembers().add(m2);
        councils.save(council);

        Defense defense = new Defense();
        defense.council = council;
        defense.group = group;
        defense.reviewer = otherLecturer;
        defense.finalized = false;
        defense = defenses.save(defense);

        em.flush();
        em.clear();

        // Advisor lecturer tries to grade own advised topic -> throws AccessDeniedException
        authAs(advisorLecturer);
        Long defenseId = defense.id;
        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () ->
            gradeService.grade(defenseId, new BigDecimal("8.50"), "Đạt yêu cầu")
        );
        assertTrue(ex.getMessage().contains("Giảng viên đang hướng dẫn đề tài không thể tham gia phản biện hoặc chấm điểm"));

        // Other lecturer (not advisor) CAN grade
        authAs(otherLecturer);
        assertDoesNotThrow(() ->
            gradeService.grade(defenseId, new BigDecimal("9.00"), "Rất xuất sắc")
        );
    }

    @Test
    @DisplayName("Yêu cầu 7: STUDENT bị từ chối truy cập mọi thao tác của cán bộ/giảng viên")
    void test7_StudentCannotAccessStaffEndpoints() {
        authAs(studentLeader);
        assertThrows(AccessDeniedException.class, () -> TopicPolicy.staff(studentLeader));
        assertThrows(AccessDeniedException.class, () -> TopicPolicy.departmentManager(studentLeader, dept.getId()));
        assertThrows(AccessDeniedException.class, () ->
            announcementService.createForDepartment(new AnnouncementRequest("Title", "Content", AnnouncementAudience.STUDENT), studentLeader)
        );
    }

    @Test
    @DisplayName("Yêu cầu 8: Lỗi bảo mật periodId == null bị chặn, không thể lọt qua kiểm tra đợt đăng ký")
    void test8_CreateGroupWithNullPeriodIdIsRejected() {
        authAs(studentLeader);
        // Outside of student registration window (studentStartAt is in the future)
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            studentService.createGroup(studentLeader.getUsername(), "Nhóm bypass", null)
        );
        assertTrue(ex.getMessage().contains("Ngoài thời gian đăng ký của đợt này."));

        // Non-existent periodId
        IllegalArgumentException exNonExistent = assertThrows(IllegalArgumentException.class, () ->
            studentService.createGroup(studentLeader.getUsername(), "Nhóm không tồn tại", 999999L)
        );
        assertTrue(exNonExistent.getMessage().contains("Đợt đăng ký không tồn tại"));
    }

    @Test
    @DisplayName("Yêu cầu 9: Giảng viên đề xuất đề tài trong thời gian cho phép; ngoài thời gian bị từ chối")
    void test9_LecturerTopicProposalWindowValidation() {
        authAs(advisorLecturer);

        TopicRequest validReq = new TopicRequest();
        validReq.setTitle("Đề tài mới trong hạn");
        validReq.setDescription("Mô tả đề tài hợp lệ đầy đủ nội dung nghiên cứu");
        validReq.setTopicType("COURSE");
        validReq.setMaxStudents(3);
        validReq.setDepartmentId(dept.getId());
        validReq.setPeriodId(period.getId());

        assertDoesNotThrow(() -> topicService.createTopic(validReq, advisorLecturer));

        // Create expired period
        RegistrationPeriod expired = new RegistrationPeriod();
        expired.setName("Đợt đã hết hạn");
        expired.setType(RegistrationPeriodType.COURSE);
        expired.setLecturerStartAt(LocalDateTime.now().minusDays(20));
        expired.setLecturerEndAt(LocalDateTime.now().minusDays(10));
        expired.setStudentStartAt(LocalDateTime.now().minusDays(9));
        expired.setStudentEndAt(LocalDateTime.now().minusDays(5));
        expired = periods.save(expired);

        TopicRequest invalidReq = new TopicRequest();
        invalidReq.setTitle("Đề tài ngoài hạn");
        invalidReq.setDescription("Mô tả đề tài hợp lệ đầy đủ nội dung nghiên cứu");
        invalidReq.setTopicType("COURSE");
        invalidReq.setMaxStudents(3);
        invalidReq.setDepartmentId(dept.getId());
        invalidReq.setPeriodId(expired.getId());

        assertThrows(IllegalArgumentException.class, () ->
            topicService.createTopic(invalidReq, advisorLecturer)
        );
    }
}
