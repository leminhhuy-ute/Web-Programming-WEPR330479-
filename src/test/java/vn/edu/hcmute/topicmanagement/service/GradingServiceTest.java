package vn.edu.hcmute.topicmanagement.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import vn.edu.hcmute.topicmanagement.domain.*;
import vn.edu.hcmute.topicmanagement.repository.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class GradingServiceTest {
    private TopicRepository topics;
    private CouncilRepository councils;
    private CouncilMemberRepository members;
    private LecturerRepository lecturers;
    private EvaluationRepository evaluations;
    private GradingService service;

    @BeforeEach
    void setUp() {
        topics = mock(TopicRepository.class); councils = mock(CouncilRepository.class);
        members = mock(CouncilMemberRepository.class); lecturers = mock(LecturerRepository.class);
        evaluations = mock(EvaluationRepository.class);
        service = new GradingService(topics, councils, members, lecturers, evaluations);
    }

    @Test
    void advisorCannotBeAssignedAsReviewer() {
        Lecturer advisor = lecturer(1L, "GV001");
        Council council = new Council("HD01", "Hội đồng 01", LocalDateTime.now().plusDays(1), "A4-401");
        ReflectionTestUtils.setField(council, "id", 10L); council.setStatus(CouncilStatus.READY);
        Topic topic = new Topic("DT01", "Đề tài", "Nhóm 01", advisor);
        when(topics.findById(20L)).thenReturn(Optional.of(topic));
        when(councils.findWithMembersById(10L)).thenReturn(Optional.of(council));
        when(lecturers.findById(1L)).thenReturn(Optional.of(advisor));
        when(members.existsByCouncilIdAndLecturerId(10L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.assign(20L, 10L, 1L))
                .isInstanceOf(BusinessException.class).hasMessageContaining("không được phản biện");
    }

    @Test
    void advisorCannotGradeOwnTopic() {
        Lecturer advisor = lecturer(1L, "GV001");
        Council council = new Council("HD01", "Hội đồng 01", LocalDateTime.now().plusDays(1), "A4-401");
        ReflectionTestUtils.setField(council, "id", 10L);
        Topic topic = new Topic("DT01", "Đề tài", "Nhóm 01", advisor); topic.setCouncil(council);
        when(topics.findDetailById(20L)).thenReturn(Optional.of(topic));
        when(lecturers.findById(1L)).thenReturn(Optional.of(advisor));

        assertThatThrownBy(() -> service.grade(20L, 1L, BigDecimal.valueOf(8), "Tốt"))
                .isInstanceOf(BusinessException.class).hasMessageContaining("không được chấm");
        verifyNoInteractions(evaluations);
    }

    private Lecturer lecturer(Long id, String code) {
        Lecturer lecturer = new Lecturer(code, "TS. Nguyễn Văn A", code.toLowerCase() + "@hcmute.edu.vn", Department.SOFTWARE_ENGINEERING);
        ReflectionTestUtils.setField(lecturer, "id", id); return lecturer;
    }
}
