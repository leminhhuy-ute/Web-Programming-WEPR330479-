package vn.edu.hcmute.topicmanagement;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmute.topicmanagement.dto.request.AssignCouncilAndReviewerRequest;
import vn.edu.hcmute.topicmanagement.dto.request.CouncilMemberRequest;
import vn.edu.hcmute.topicmanagement.dto.request.CouncilRequest;
import vn.edu.hcmute.topicmanagement.dto.request.GradeEvaluationRequest;
import vn.edu.hcmute.topicmanagement.dto.response.CouncilResponse;
import vn.edu.hcmute.topicmanagement.entity.Department;
import vn.edu.hcmute.topicmanagement.entity.Topic;
import vn.edu.hcmute.topicmanagement.entity.User;
import vn.edu.hcmute.topicmanagement.enums.CouncilRole;
import vn.edu.hcmute.topicmanagement.enums.CouncilStatus;
import vn.edu.hcmute.topicmanagement.enums.EvaluationType;
import vn.edu.hcmute.topicmanagement.enums.Role;
import vn.edu.hcmute.topicmanagement.exception.ConflictException;
import vn.edu.hcmute.topicmanagement.repository.DepartmentRepository;
import vn.edu.hcmute.topicmanagement.repository.TopicRepository;
import vn.edu.hcmute.topicmanagement.repository.UserRepository;
import vn.edu.hcmute.topicmanagement.service.CouncilService;
import vn.edu.hcmute.topicmanagement.service.GradingService;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
@Transactional
class CouncilGradingIntegrationTest {

    @Autowired
    private CouncilService councilService;

    @Autowired
    private GradingService gradingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Test
    void testCouncilFormationAndReviewerGrading() {
        Department dept = departmentRepository.findAll().get(0);
        List<User> lecturers = userRepository.findByRole(Role.LECTURER);
        User chair = lecturers.get(0);
        User secretary = lecturers.get(1);
        User member = lecturers.get(2);

        // 1. Create Council
        CouncilResponse council = councilService.createCouncil(new CouncilRequest(
                "HD-TEST-99",
                "Hội đồng bảo vệ Thử nghiệm",
                dept.getId(),
                null,
                LocalDate.now().plusDays(30),
                "09:00",
                "Phòng A1-401"
        ));
        Assertions.assertEquals(CouncilStatus.PENDING.name(), council.status());

        // Cannot mark ready with 0 members
        Assertions.assertThrows(ConflictException.class, () -> councilService.markReady(council.id()));

        // Add 3 members: Chair, Secretary, Member
        councilService.addMember(council.id(), new CouncilMemberRequest(chair.getId(), CouncilRole.CHAIR));
        councilService.addMember(council.id(), new CouncilMemberRequest(secretary.getId(), CouncilRole.SECRETARY));
        councilService.addMember(council.id(), new CouncilMemberRequest(member.getId(), CouncilRole.MEMBER));

        // Now mark ready
        CouncilResponse readyCouncil = councilService.markReady(council.id());
        Assertions.assertEquals(CouncilStatus.READY.name(), readyCouncil.status());

        // 2. Assign Reviewer and Council to Topic
        Topic topic = topicRepository.findAll().get(0);

        // Conflict of interest check: Advisor cannot be reviewer
        if (topic.getAdvisor1() != null) {
            AssignCouncilAndReviewerRequest invalidAssign = new AssignCouncilAndReviewerRequest(
                    topic.getId(),
                    council.id(),
                    topic.getAdvisor1().getId()
            );
            Assertions.assertThrows(ConflictException.class, () -> gradingService.assignCouncilAndReviewer(invalidAssign));

            // Conflict of interest check: Advisor cannot grade own topic
            GradeEvaluationRequest advisorGrade = new GradeEvaluationRequest(
                    topic.getId(),
                    council.id(),
                    9.5,
                    EvaluationType.COUNCIL,
                    "GVHD tự chấm điểm"
            );
            Assertions.assertThrows(ConflictException.class, () -> gradingService.gradeTopic(advisorGrade, topic.getAdvisor1().getId()));
        }

        // Conflict of interest check: Co-advisor cannot grade own topic
        if (topic.getAdvisor2() != null) {
            GradeEvaluationRequest coAdvisorGrade = new GradeEvaluationRequest(
                    topic.getId(),
                    council.id(),
                    9.0,
                    EvaluationType.COUNCIL,
                    "GV đồng hướng dẫn tự chấm điểm"
            );
            Assertions.assertThrows(ConflictException.class, () -> gradingService.gradeTopic(coAdvisorGrade, topic.getAdvisor2().getId()));
        }

        // 3. Valid Grade by Independent Member
        GradeEvaluationRequest validGrade = new GradeEvaluationRequest(
                topic.getId(),
                council.id(),
                8.8,
                EvaluationType.COUNCIL,
                "Đề tài đạt yêu cầu chất lượng chuyên môn tốt."
        );
        var eval = gradingService.gradeTopic(validGrade, member.getId());
        Assertions.assertNotNull(eval.id());
        Assertions.assertEquals(8.8, eval.score());
    }
}
