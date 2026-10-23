package topicmanagement;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
    User s1, s2, s3, s4;
    Topic topic1, topic2, topic3;

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

        topic1 = createTopic("DT-SE-01", "Đề tài 1 sinh viên", 1);
        topic2 = createTopic("DT-SE-02", "Đề tài 2 sinh viên", 2);
        topic3 = createTopic("DT-SE-03", "Đề tài 3 sinh viên", 3);
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
    @DisplayName("Edge Case 1: Nhóm 1 sinh viên (chỉ nhóm trưởng) được đăng ký đề tài")
    void testGroupOfOneStudentCanRegister() {
        studentService.createGroup(s1.getUsername(), "Nhóm một người");
        assertDoesNotThrow(() -> studentService.register(s1.getUsername(), topic1.getTopicCode()));
        assertEquals("PENDING", studentService.state(s1.getUsername()).registration().status());
    }

    @Test
    @DisplayName("Edge Case 2: Nhóm 2 sinh viên được đăng ký đề tài nhận tối đa 2 sinh viên")
    void testGroupOfTwoStudentsCanRegister() {
        studentService.createGroup(s1.getUsername(), "Nhóm hai người");
        addMember(s1.getUsername(), s2);
        assertDoesNotThrow(() -> studentService.register(s1.getUsername(), topic2.getTopicCode()));
    }

    @Test
    @DisplayName("Edge Case 3: Nhóm 3 sinh viên đăng ký đề tài tối đa 2 bị từ chối, đề tài tối đa 3 thành công")
    void testGroupOfThreeStudents() {
        studentService.createGroup(s1.getUsername(), "Nhóm ba người");
        addMember(s1.getUsername(), s2);
        addMember(s1.getUsername(), s3);
        var ex = assertThrows(IllegalArgumentException.class, () ->
            studentService.register(s1.getUsername(), topic2.getTopicCode()));
        assertTrue(ex.getMessage().contains("Nhóm vượt số thành viên cho phép của đề tài."));
        assertDoesNotThrow(() -> studentService.register(s1.getUsername(), topic3.getTopicCode()));
        var state = studentService.state(s1.getUsername());
        assertEquals(topic3.getTopicCode(), state.registration().topic().id());
        assertEquals(3, state.group().maxMembers());
    }

    @Test
    @DisplayName("Edge Case 4: Nhóm không được vượt quá 3 thành viên (từ chối lời mời thứ 4)")
    void testGroupCannotExceedThreeMembers() {
        studentService.createGroup(s1.getUsername(), "Nhóm đủ người");
        addMember(s1.getUsername(), s2);
        addMember(s1.getUsername(), s3);
        var ex = assertThrows(IllegalArgumentException.class, () ->
            studentService.invite(s1.getUsername(), s4.getUserCode()));
        assertTrue(ex.getMessage().contains("Nhóm đã đủ 3 thành viên."));
    }

    @Test
    @DisplayName("Edge Case 5: Lời mời gửi khi nhóm có 2 người bị từ chối khi chấp nhận nếu nhóm đã đủ 3")
    void testInvitationAcceptanceRejectedWhenGroupAlreadyFull() {
        studentService.createGroup(s1.getUsername(), "Nhóm tranh chỗ");
        addMember(s1.getUsername(), s2);
        studentService.invite(s1.getUsername(), s3.getUserCode());
        studentService.invite(s1.getUsername(), s4.getUserCode());
        var inv3 = invitations.findByStudentIdAndStatusOrderByCreatedAtDesc(s3.getId(), "PENDING").getFirst();
        var inv4 = invitations.findByStudentIdAndStatusOrderByCreatedAtDesc(s4.getId(), "PENDING").getFirst();
        assertDoesNotThrow(() -> studentService.respond(s3.getUsername(), inv3.id, true));
        var ex = assertThrows(IllegalArgumentException.class, () ->
            studentService.respond(s4.getUsername(), inv4.id, true));
        assertTrue(ex.getMessage().contains("Nhóm đã đủ 3 thành viên."));
        var state = studentService.state(s1.getUsername());
        assertEquals(3, state.group().members().size());
        assertFalse(state.group().members().stream().anyMatch(m -> m.id().equals(s4.getUserCode())));
    }

    @Test
    @DisplayName("Edge Case 6: Ràng buộc maxStudents của đề tài phải từ 1 đến 3 khi tạo")
    void testTopicMaxStudentsValidation() {
        LocalDateTime now = LocalDateTime.now();
        RegistrationPeriod lecPeriod = new RegistrationPeriod();
        lecPeriod.setName("Đợt mở cho giảng viên");
        lecPeriod.setType(RegistrationPeriodType.COURSE);
        lecPeriod.setLecturerStartAt(now.minusDays(2));
        lecPeriod.setLecturerEndAt(now.plusDays(2));
        lecPeriod.setStudentStartAt(now.plusDays(3));
        lecPeriod.setStudentEndAt(now.plusDays(10));
        RegistrationPeriod open = periods.save(lecPeriod);

        for (int invalid : new int[]{0, 4}) {
            TopicRequest req = request("Đề tài không hợp lệ " + invalid, invalid, open);
            assertThrows(IllegalArgumentException.class, () -> topicService.createTopic(req, lecturer));
        }
        for (int valid : new int[]{1, 2, 3}) {
            TopicRequest req = request("Đề tài hợp lệ quy mô " + valid, valid, open);
            assertDoesNotThrow(() -> topicService.createTopic(req, lecturer));
        }
    }

    @Test
    @DisplayName("Tạo nhóm và mọi thay đổi thành viên chỉ được phép trong cửa sổ sinh viên")
    void membershipChangesRequireAnOpenStudentRegistrationWindow() {
        LocalDateTime now = LocalDateTime.now();
        period.setStudentStartAt(now.plusHours(1));
        period.setStudentEndAt(now.plusHours(2));
        periods.save(period);
        assertThrows(IllegalArgumentException.class, () ->
            studentService.createGroup(s1.getUsername(), "Nhóm trước hạn"));

        period.setStudentStartAt(now.minusDays(1));
        period.setStudentEndAt(now.plusDays(1));
        periods.save(period);
        studentService.createGroup(s1.getUsername(), "Nhóm đang mở");
        addMember(s1.getUsername(), s2);
        studentService.invite(s1.getUsername(), s3.getUserCode());
        var pending = invitations.findByStudentIdAndStatusOrderByCreatedAtDesc(s3.getId(), "PENDING").getFirst();

        period.setStudentEndAt(LocalDateTime.now().minusSeconds(1));
        periods.save(period);
        assertThrows(IllegalArgumentException.class, () ->
            studentService.invite(s1.getUsername(), s4.getUserCode()));
        assertThrows(IllegalArgumentException.class, () ->
            studentService.respond(s3.getUsername(), pending.id, true));
        assertDoesNotThrow(() -> studentService.respond(s3.getUsername(), pending.id, false));
        assertThrows(IllegalArgumentException.class, () ->
            studentService.transfer(s1.getUsername(), s2.getUserCode()));
        assertThrows(IllegalArgumentException.class, () ->
            studentService.removeMember(s1.getUsername(), s2.getUserCode()));
        assertThrows(IllegalArgumentException.class, () -> studentService.leaveGroup(s2.getUsername()));
        assertThrows(IllegalArgumentException.class, () -> studentService.disbandGroup(s1.getUsername()));

        var group = groups.findAll().getFirst();
        assertEquals(2, members.findByGroupId(group.getId()).size());
        assertEquals("sv01", group.getLeader().getUsername());
    }

    @Test
    @DisplayName("Hai lời mời được nhận đồng thời không làm nhóm vượt quá 3 thành viên")
    void concurrentAcceptsCannotExceedThreeMembers() throws Exception {
        studentService.createGroup(s1.getUsername(), "Nhóm tranh chỗ");
        studentService.invite(s1.getUsername(), s2.getUserCode());
        studentService.invite(s1.getUsername(), s3.getUserCode());
        studentService.invite(s1.getUsername(), s4.getUserCode());
        Long groupId = groups.findAll().getFirst().getId();
        var pending = List.of(s2, s3, s4).stream()
            .map(user -> invitations.findByGroupIdAndStudentId(groupId, user.getId()).orElseThrow())
            .map(invitation -> new Acceptance(invitation.student.getUsername(), invitation.id))
            .toList();

        List<Boolean> outcomes = acceptConcurrently(pending);

        assertEquals(2, outcomes.stream().filter(Boolean::booleanValue).count());
        var group = groups.findById(groupId).orElseThrow();
        assertEquals(3, members.findByGroupId(groupId).size());
        assertEquals(3, group.getMemberCount());
    }

    @Test
    @DisplayName("Nhận đồng thời lời mời từ hai nhóm cùng đợt chỉ cho phép một membership")
    void concurrentAcceptsCannotPutOneStudentInTwoGroupsInTheSamePeriod() throws Exception {
        studentService.createGroup(s1.getUsername(), "Nhóm A");
        studentService.createGroup(s2.getUsername(), "Nhóm B");
        studentService.invite(s1.getUsername(), s4.getUserCode());
        studentService.invite(s2.getUsername(), s4.getUserCode());
        List<Long> groupIds = groups.findAll().stream().map(StudentGroup::getId).sorted().toList();
        var invitationA = invitations.findByGroupIdAndStudentId(groupIds.get(0), s4.getId()).orElseThrow();
        var invitationB = invitations.findByGroupIdAndStudentId(groupIds.get(1), s4.getId()).orElseThrow();

        List<Boolean> outcomes = acceptConcurrently(List.of(
            new Acceptance(s4.getUsername(), invitationA.id),
            new Acceptance(s4.getUsername(), invitationB.id)));

        assertEquals(1, outcomes.stream().filter(Boolean::booleanValue).count());
        assertTrue(members.findByRegistrationPeriodIdAndStudentId(period.getId(), s4.getId()).isPresent());
    }

    private record Acceptance(String username, Long invitationId) {}

    private List<Boolean> acceptConcurrently(List<Acceptance> acceptances) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(acceptances.size());
        CountDownLatch ready = new CountDownLatch(acceptances.size());
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<Boolean>> results = acceptances.stream().map(acceptance -> executor.submit(() -> {
                ready.countDown();
                if (!start.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Concurrent start timed out.");
                try {
                    studentService.respond(acceptance.username(), acceptance.invitationId(), true);
                    return true;
                } catch (IllegalArgumentException expected) {
                    return false;
                }
            })).toList();
            assertTrue(ready.await(10, TimeUnit.SECONDS), "All workers should reach the start barrier.");
            start.countDown();
            List<Boolean> outcomes = new ArrayList<>();
            for (Future<Boolean> result : results) outcomes.add(result.get(20, TimeUnit.SECONDS));
            return outcomes;
        } finally {
            start.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS), "Worker threads should terminate.");
        }
    }

    private TopicRequest request(String title, int maxStudents, RegistrationPeriod open) {
        TopicRequest req = new TopicRequest();
        req.setTitle(title);
        req.setDescription("Mô tả hợp lệ trên 10 ký tự cho " + title);
        req.setMaxStudents(maxStudents);
        req.setTopicType("COURSE");
        req.setDepartmentId(dept.getId());
        req.setPeriodId(open.getId());
        return req;
    }
}
