package topicmanagement;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import topicmanagement.dto.request.TopicRequest;
import topicmanagement.entity.*;
import topicmanagement.enums.*;
import topicmanagement.repository.*;
import topicmanagement.service.TopicService;
import topicmanagement.student.InvitationRepository;
import topicmanagement.student.StudentService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:group-size-test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.profiles.active=test"
})
public class StudentGroupSizeEdgeCaseTest {

    @Autowired StudentService studentService;
    @Autowired TopicService topicService;
    @Autowired UserRepository users;
    @Autowired DepartmentRepository departments;
    @Autowired RegistrationPeriodRepository periods;
    @Autowired TopicRepository topics;
    @Autowired StudentGroupRepository groups;
    @Autowired GroupMemberRepository members;
    @Autowired InvitationRepository invitations;

    Department dept;
    RegistrationPeriod period;
    User lecturer;
    User s1, s2, s3, s4, s5, s6;
    Topic topic3, topic4, topic5;

    @BeforeEach
    void setUp() {
        invitations.deleteAll();
        members.deleteAll();
        groups.deleteAll();
        topics.deleteAll();
        periods.deleteAll();
        users.deleteAll();
        departments.deleteAll();

        dept = new Department();
        dept.setCode("SE");
        dept.setName("Software Engineering");
        dept = departments.save(dept);

        LocalDateTime now = LocalDateTime.now();
        period = new RegistrationPeriod();
        period.setName("Đợt test quy mô nhóm");
        period.setType(RegistrationPeriodType.COURSE);
        period.setLecturerStartAt(now.minusDays(5));
        period.setLecturerEndAt(now.minusDays(3));
        period.setStudentStartAt(now.minusDays(2));
        period.setStudentEndAt(now.plusDays(5));
        period = periods.save(period);

        lecturer = createUser("gv01", "GV01", Role.LECTURER);
        s1 = createUser("sv01", "SV01", Role.STUDENT);
        s2 = createUser("sv02", "SV02", Role.STUDENT);
        s3 = createUser("sv03", "SV03", Role.STUDENT);
        s4 = createUser("sv04", "SV04", Role.STUDENT);
        s5 = createUser("sv05", "SV05", Role.STUDENT);
        s6 = createUser("sv06", "SV06", Role.STUDENT);

        topic3 = createTopic("DT-SE-03", "Đề tài 3 sinh viên", 3);
        topic4 = createTopic("DT-SE-04", "Đề tài 4 sinh viên", 4);
        topic5 = createTopic("DT-SE-05", "Đề tài 5 sinh viên", 5);
    }

    private User createUser(String username, String code, Role role) {
        User u = new User();
        u.setUsername(username);
        u.setUserCode(code);
        u.setFullName("User " + username);
        u.setEmail(username + "@test.edu.vn");
        u.setPasswordHash("hash");
        u.setRole(role);
        u.setStatus(UserStatus.ACTIVE);
        u.setDepartment(dept);
        return users.save(u);
    }

    private Topic createTopic(String code, String title, int maxStudents) {
        Topic t = new Topic();
        t.setTopicCode(code);
        t.setTitle(title);
        t.setDescription("Mô tả chi tiết đề tài kiểm thử độ dài đầy đủ");
        t.setMaxStudents(maxStudents);
        t.setTopicType(RegistrationPeriodType.COURSE);
        t.setStatus(TopicStatus.APPROVED);
        t.setDepartment(dept);
        t.setPeriod(period);
        t.setCreatedBy(lecturer);
        t.setAdvisor1(lecturer);
        return topics.save(t);
    }

    private void addMember(String leaderUsername, User target) {
        studentService.invite(leaderUsername, target.getUserCode());
        var inv = invitations.findByStudentIdAndStatusOrderByCreatedAtDesc(target.getId(), "PENDING").getFirst();
        studentService.respond(target.getUsername(), inv.id, true);
    }

    @Test
    @DisplayName("Edge Case 1: Nhóm 1 sinh viên (chỉ nhóm trưởng) bị từ chối khi đăng ký đề tài")
    void testGroupOfOneStudentCannotRegister() {
        studentService.createGroup(s1.getUsername(), "Nhóm 1 thành viên");
        var ex = assertThrows(IllegalArgumentException.class, () ->
            studentService.register(s1.getUsername(), topic3.getTopicCode())
        );
        assertTrue(ex.getMessage().contains("Nhóm phải có từ 3 đến 5 sinh viên mới được đăng ký đề tài."));
    }

    @Test
    @DisplayName("Edge Case 2: Nhóm 2 sinh viên (đã mời 1 bạn) vẫn bị từ chối khi đăng ký đề tài")
    void testGroupOfTwoStudentsCannotRegister() {
        studentService.createGroup(s1.getUsername(), "Nhóm 2 thành viên");
        addMember(s1.getUsername(), s2);

        var ex = assertThrows(IllegalArgumentException.class, () ->
            studentService.register(s1.getUsername(), topic3.getTopicCode())
        );
        assertTrue(ex.getMessage().contains("Nhóm phải có từ 3 đến 5 sinh viên mới được đăng ký đề tài."));
    }

    @Test
    @DisplayName("Edge Case 3: Nhóm đủ 3 sinh viên được phép đăng ký đề tài có maxStudents >= 3")
    void testGroupOfThreeStudentsCanRegister() {
        studentService.createGroup(s1.getUsername(), "Nhóm 3 thành viên");
        addMember(s1.getUsername(), s2);
        addMember(s1.getUsername(), s3);

        assertDoesNotThrow(() ->
            studentService.register(s1.getUsername(), topic3.getTopicCode())
        );
        var state = studentService.state(s1.getUsername());
        assertNotNull(state.registration());
        assertEquals(topic3.getTopicCode(), state.registration().topic().id());
        assertEquals("PENDING", state.registration().status());
    }

    @Test
    @DisplayName("Edge Case 4: Nhóm 4 sinh viên đăng ký đề tài maxStudents=3 bị từ chối, nhưng đề tài maxStudents=4 thành công")
    void testGroupOfFourStudents() {
        studentService.createGroup(s1.getUsername(), "Nhóm 4 thành viên");
        addMember(s1.getUsername(), s2);
        addMember(s1.getUsername(), s3);
        addMember(s1.getUsername(), s4);

        // Topic with maxStudents = 3 rejected
        var ex = assertThrows(IllegalArgumentException.class, () ->
            studentService.register(s1.getUsername(), topic3.getTopicCode())
        );
        assertTrue(ex.getMessage().contains("Nhóm vượt số thành viên cho phép của đề tài."));

        // Topic with maxStudents = 4 succeeds
        assertDoesNotThrow(() ->
            studentService.register(s1.getUsername(), topic4.getTopicCode())
        );
    }

    @Test
    @DisplayName("Edge Case 5: Nhóm 5 sinh viên đăng ký đề tài maxStudents=5 thành công")
    void testGroupOfFiveStudentsCanRegister() {
        studentService.createGroup(s1.getUsername(), "Nhóm 5 thành viên");
        addMember(s1.getUsername(), s2);
        addMember(s1.getUsername(), s3);
        addMember(s1.getUsername(), s4);
        addMember(s1.getUsername(), s5);

        assertDoesNotThrow(() ->
            studentService.register(s1.getUsername(), topic5.getTopicCode())
        );
    }

    @Test
    @DisplayName("Edge Case 6: Nhóm không được vượt quá 5 thành viên (từ chối thành viên thứ 6)")
    void testGroupCannotExceedFiveMembers() {
        studentService.createGroup(s1.getUsername(), "Nhóm đầy đủ");
        addMember(s1.getUsername(), s2);
        addMember(s1.getUsername(), s3);
        addMember(s1.getUsername(), s4);
        addMember(s1.getUsername(), s5);

        // Inviting 6th student rejected
        var ex = assertThrows(IllegalArgumentException.class, () ->
            studentService.invite(s1.getUsername(), s6.getUserCode())
        );
        assertTrue(ex.getMessage().contains("Nhóm đã đủ 5 thành viên."));
    }

    @Test
    @DisplayName("Edge Case 7: Lời mời tạo khi nhóm có 4 người, nhưng bị từ chối khi chấp nhận nếu nhóm đã đủ 5 người")
    void testInvitationAcceptanceRejectedWhenGroupAlreadyFull() {
        studentService.createGroup(s1.getUsername(), "Nhóm 4 người chuẩn bị đủ 5");
        addMember(s1.getUsername(), s2);
        addMember(s1.getUsername(), s3);
        addMember(s1.getUsername(), s4);

        // Lúc này nhóm đang có 4 thành viên (s1, s2, s3, s4)
        // Nhóm trưởng gửi lời mời cho cả s5 và s6 (hợp lệ tại thời điểm gửi vì nhóm chưa đủ 5)
        studentService.invite(s1.getUsername(), s5.getUserCode());
        studentService.invite(s1.getUsername(), s6.getUserCode());

        var inv5 = invitations.findByStudentIdAndStatusOrderByCreatedAtDesc(s5.getId(), "PENDING").getFirst();
        var inv6 = invitations.findByStudentIdAndStatusOrderByCreatedAtDesc(s6.getId(), "PENDING").getFirst();

        // s5 chấp nhận lời mời trước -> nhóm đạt đủ 5 thành viên
        assertDoesNotThrow(() ->
            studentService.respond(s5.getUsername(), inv5.id, true)
        );

        // s6 sau đó chấp nhận lời mời -> phải bị từ chối vì nhóm đã đủ 5 thành viên
        var ex = assertThrows(IllegalArgumentException.class, () ->
            studentService.respond(s6.getUsername(), inv6.id, true)
        );
        assertTrue(ex.getMessage().contains("Nhóm đã đủ 5 thành viên."));

        // Xác nhận số thành viên thực tế trong nhóm vẫn là 5, s6 không bị thêm vào nhóm
        var state = studentService.state(s1.getUsername());
        assertEquals(5, state.group().members().size());
        assertFalse(state.group().members().stream().anyMatch(m -> m.id().equals(s6.getUserCode())));
    }

    @Test
    @DisplayName("Edge Case 8: Ràng buộc maxStudents của đề tài phải từ 3 đến 5 khi tạo và cập nhật")
    void testTopicMaxStudentsValidation() {
        LocalDateTime now = LocalDateTime.now();
        RegistrationPeriod lecPeriod = new RegistrationPeriod();
        lecPeriod.setName("Đợt mở cho giảng viên");
        lecPeriod.setType(RegistrationPeriodType.COURSE);
        lecPeriod.setLecturerStartAt(now.minusDays(2));
        lecPeriod.setLecturerEndAt(now.plusDays(2));
        lecPeriod.setStudentStartAt(now.plusDays(3));
        lecPeriod.setStudentEndAt(now.plusDays(10));
        lecPeriod = periods.save(lecPeriod);

        // Topic with maxStudents = 2 -> rejected
        TopicRequest reqBelow = new TopicRequest();
        reqBelow.setTitle("Đề tài dưới 3 sinh viên");
        reqBelow.setDescription("Mô tả hợp lệ trên 10 ký tự");
        reqBelow.setMaxStudents(2);
        reqBelow.setTopicType("COURSE");
        reqBelow.setDepartmentId(dept.getId());
        reqBelow.setPeriodId(lecPeriod.getId());

        assertThrows(IllegalArgumentException.class, () ->
            topicService.createTopic(reqBelow, lecturer)
        );

        // Topic with maxStudents = 6 -> rejected
        TopicRequest reqAbove = new TopicRequest();
        reqAbove.setTitle("Đề tài trên 5 sinh viên");
        reqAbove.setDescription("Mô tả hợp lệ trên 10 ký tự");
        reqAbove.setMaxStudents(6);
        reqAbove.setTopicType("COURSE");
        reqAbove.setDepartmentId(dept.getId());
        reqAbove.setPeriodId(lecPeriod.getId());

        assertThrows(IllegalArgumentException.class, () ->
            topicService.createTopic(reqAbove, lecturer)
        );

        // Valid values: 3, 4, 5 -> all succeed
        for (int validSize : new int[]{3, 4, 5}) {
            TopicRequest reqValid = new TopicRequest();
            reqValid.setTitle("Đề tài hợp lệ quy mô " + validSize);
            reqValid.setDescription("Mô tả hợp lệ trên 10 ký tự quy mô " + validSize);
            reqValid.setMaxStudents(validSize);
            reqValid.setTopicType("COURSE");
            reqValid.setDepartmentId(dept.getId());
            reqValid.setPeriodId(lecPeriod.getId());

            assertDoesNotThrow(() ->
                topicService.createTopic(reqValid, lecturer)
            );
        }
    }
}
