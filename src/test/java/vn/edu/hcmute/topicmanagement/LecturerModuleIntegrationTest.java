package vn.edu.hcmute.topicmanagement;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmute.topicmanagement.dto.request.AssignAdvisorsRequest;
import vn.edu.hcmute.topicmanagement.dto.request.TopicApprovalRequest;
import vn.edu.hcmute.topicmanagement.dto.request.TopicRequest;
import vn.edu.hcmute.topicmanagement.dto.response.TopicResponse;
import vn.edu.hcmute.topicmanagement.entity.Department;
import vn.edu.hcmute.topicmanagement.entity.RegistrationPeriod;
import vn.edu.hcmute.topicmanagement.entity.User;
import vn.edu.hcmute.topicmanagement.enums.RegistrationPeriodType;
import vn.edu.hcmute.topicmanagement.enums.Role;
import vn.edu.hcmute.topicmanagement.enums.TopicStatus;
import vn.edu.hcmute.topicmanagement.repository.DepartmentRepository;
import vn.edu.hcmute.topicmanagement.repository.RegistrationPeriodRepository;
import vn.edu.hcmute.topicmanagement.repository.UserRepository;
import vn.edu.hcmute.topicmanagement.service.DepartmentTopicService;
import vn.edu.hcmute.topicmanagement.service.TopicService;

@SpringBootTest
@Transactional
class LecturerModuleIntegrationTest {

    @Autowired
    private TopicService topicService;

    @Autowired
    private DepartmentTopicService departmentTopicService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private RegistrationPeriodRepository periodRepository;

    @Test
    void testTopicProposalAndDepartmentApproval() {
        User lecturer = userRepository.findByRole(Role.LECTURER).get(0);
        Department dept = departmentRepository.findAll().get(0);
        RegistrationPeriod period = periodRepository.findAll().get(0);

        TopicRequest req = new TopicRequest(
                null,
                "Đề tài nghiên cứu ứng dụng AI vào Chấm thi trắc nghiệm",
                "Mô tả đề tài chi tiết cho sinh viên",
                "Python OpenCV, Spring Boot",
                3,
                RegistrationPeriodType.KLTN,
                dept.getId(),
                period.getId()
        );

        TopicResponse proposed = topicService.createTopic(req, lecturer.getId());
        Assertions.assertNotNull(proposed.id());
        Assertions.assertEquals(TopicStatus.PENDING.name(), proposed.status());

        // Head of department reviews and approves
        User head = userRepository.findByRole(Role.HEAD_OF_DEPT).get(0);
        TopicApprovalRequest approvalReq = new TopicApprovalRequest(TopicStatus.APPROVED, null);
        TopicResponse approved = departmentTopicService.approveOrRejectTopic(proposed.id(), approvalReq, head.getId());
        Assertions.assertEquals(TopicStatus.APPROVED.name(), approved.status());

        // Assign Advisor 2
        User advisor2 = userRepository.findByRole(Role.LECTURER).get(1);
        AssignAdvisorsRequest assignReq = new AssignAdvisorsRequest(lecturer.getId(), advisor2.getId());
        TopicResponse assigned = departmentTopicService.assignAdvisors(proposed.id(), assignReq, head.getId());
        Assertions.assertEquals(advisor2.getFullName(), assigned.advisor2Name());

        // Advisor 1 and 2 cannot be identical
        AssignAdvisorsRequest duplicateReq = new AssignAdvisorsRequest(lecturer.getId(), lecturer.getId());
        Assertions.assertThrows(IllegalArgumentException.class, () -> departmentTopicService.assignAdvisors(proposed.id(), duplicateReq, head.getId()));
    }
}
