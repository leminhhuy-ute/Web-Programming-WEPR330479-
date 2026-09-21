package vn.edu.hcmute.topicmanagement;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmute.topicmanagement.dto.request.CreateGroupRequest;
import vn.edu.hcmute.topicmanagement.dto.request.InviteMemberRequest;
import vn.edu.hcmute.topicmanagement.dto.request.RegisterTopicRequest;
import vn.edu.hcmute.topicmanagement.dto.request.SubmitReportRequest;
import vn.edu.hcmute.topicmanagement.dto.response.StudentGroupResponse;
import vn.edu.hcmute.topicmanagement.dto.response.StudentWorkspaceResponse;
import vn.edu.hcmute.topicmanagement.entity.Department;
import vn.edu.hcmute.topicmanagement.entity.Topic;
import vn.edu.hcmute.topicmanagement.entity.User;
import vn.edu.hcmute.topicmanagement.enums.Role;
import vn.edu.hcmute.topicmanagement.enums.TopicStatus;
import vn.edu.hcmute.topicmanagement.exception.ConflictException;
import vn.edu.hcmute.topicmanagement.repository.DepartmentRepository;
import vn.edu.hcmute.topicmanagement.repository.TopicRepository;
import vn.edu.hcmute.topicmanagement.repository.UserRepository;
import vn.edu.hcmute.topicmanagement.service.StudentGroupService;

@SpringBootTest
@Transactional
class StudentModuleIntegrationTest {

    @Autowired
    private StudentGroupService studentGroupService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private vn.edu.hcmute.topicmanagement.service.DepartmentTopicService departmentTopicService;

    @Test
    void testStudentGroupCreationAndInvitation() {
        Department dept = departmentRepository.findAll().get(0);
        User sv1 = userRepository.save(new User("99110001", "svtest1", "hash", "Nguyễn Văn Một", "sv1@student.hcmute.edu.vn", Role.STUDENT, dept));
        User sv2 = userRepository.save(new User("99110002", "svtest2", "hash", "Trần Văn Hai", "sv2@student.hcmute.edu.vn", Role.STUDENT, dept));
        User sv3 = userRepository.save(new User("99110003", "svtest3", "hash", "Lê Văn Ba", "sv3@student.hcmute.edu.vn", Role.STUDENT, dept));
        User sv4 = userRepository.save(new User("99110004", "svtest4", "hash", "Phạm Văn Bốn", "sv4@student.hcmute.edu.vn", Role.STUDENT, dept));

        // 1. Create group
        StudentGroupResponse group = studentGroupService.createGroup(new CreateGroupRequest("Nhóm Test Đồ Án", "Ghi chú"), sv1.getId());
        Assertions.assertNotNull(group.id());
        Assertions.assertEquals(1, group.memberCount());

        // Cannot create another group when already in one
        Assertions.assertThrows(ConflictException.class, () -> studentGroupService.createGroup(new CreateGroupRequest("Nhóm 2", ""), sv1.getId()));

        // 2. Invite sv2
        var invite1 = studentGroupService.inviteMember(new InviteMemberRequest(sv2.getUserCode(), "Tham gia nhóm nhé"), sv1.getId());
        studentGroupService.respondInvitation(invite1.id(), true, sv2.getId());

        // 3. Invite sv3
        var invite2 = studentGroupService.inviteMember(new InviteMemberRequest(sv3.getUserCode(), "Tham gia nhóm nhé"), sv1.getId());
        studentGroupService.respondInvitation(invite2.id(), true, sv3.getId());

        // Group now has 3 members (full)
        StudentWorkspaceResponse ws = studentGroupService.getWorkspace(sv1.getId());
        Assertions.assertEquals(3, ws.group().memberCount());

        // 4. Exceed maximum 3 members: cannot invite 4th member
        Assertions.assertThrows(ConflictException.class, () ->
                studentGroupService.inviteMember(new InviteMemberRequest(sv4.getUserCode(), "Mời thêm người thứ 4"), sv1.getId()));

        // 5. Register topic
        Topic approvedTopic = topicRepository.findByStatus(TopicStatus.APPROVED).get(0);
        var reg = studentGroupService.registerTopic(new RegisterTopicRequest(approvedTopic.getId(), "Nhóm em xin đăng ký"), sv1.getId());
        Assertions.assertNotNull(reg.id());

        // 6. Advisor approves group registration
        departmentTopicService.reviewGroupRegistration(reg.id(),
                new vn.edu.hcmute.topicmanagement.dto.request.GroupApprovalRequest(
                        vn.edu.hcmute.topicmanagement.enums.GroupStatus.APPROVED, "GVHD đồng ý hướng dẫn"),
                approvedTopic.getAdvisor1().getId());

        // 7. Submit Progress Report
        var rep = studentGroupService.submitReport(new SubmitReportRequest(
                "Báo cáo tuần 1",
                "Đề cương",
                "Nhóm đã hoàn thiện sơ đồ ERD",
                "https://github.com/doc.pdf",
                100
        ), null, sv1.getId());
        Assertions.assertNotNull(rep.id());
        Assertions.assertEquals("Báo cáo tuần 1", rep.reportTitle());
    }
}
