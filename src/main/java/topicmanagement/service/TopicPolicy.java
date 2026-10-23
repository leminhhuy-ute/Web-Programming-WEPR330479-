package topicmanagement.service;

import java.time.LocalDateTime;
import java.util.Objects;
import org.springframework.security.access.AccessDeniedException;
import topicmanagement.entity.*;
import topicmanagement.enums.*;

public final class TopicPolicy {
    /** Group and topic capacity rules (1–3 students), shared by services, DTO validation and the H2 demo upgrade. */
    public static final int MIN_GROUP_SIZE = 1;
    public static final int MAX_GROUP_SIZE = 3;
    public static final int MIN_TOPIC_STUDENTS = 1;
    public static final int MAX_TOPIC_STUDENTS = 3;

    private TopicPolicy() {}
    public static void staff(User u) {
        if (u.getStatus() != UserStatus.ACTIVE || u.getRole() == Role.STUDENT)
            throw new AccessDeniedException("Yêu cầu tài khoản giảng viên đang hoạt động.");
    }
    public static void advisorForDepartment(User u, Department department, String label) {
        staff(u);
        if (u.getRole() == Role.DEAN) return;
        if (u.getDepartment() == null || department == null
                || !Objects.equals(u.getDepartment().getId(), department.getId()))
            throw new IllegalArgumentException(label + " không thuộc bộ môn của đề tài.");
    }
    public static void departmentManager(User u, Long departmentId) {
        staff(u);
        if (u.getRole() != Role.DEAN)
            throw new AccessDeniedException("Chỉ trưởng khoa được duyệt đề tài và phân công giảng viên hướng dẫn.");
    }
    public static void advisor(User u, Topic t) {
        staff(u);
        if (u.getRole() == Role.DEAN) return;
        if ((t.getAdvisor1() == null || !Objects.equals(t.getAdvisor1().getId(), u.getId()))
                && (t.getAdvisor2() == null || !Objects.equals(t.getAdvisor2().getId(), u.getId())))
            throw new AccessDeniedException("Bạn không hướng dẫn đề tài này.");
    }
    public static void registration(RegistrationPeriod p, RegistrationPeriodType type, boolean student) {
        if (p.getType() != type) throw new IllegalArgumentException("Loại đề tài phải trùng loại đợt đăng ký.");
        var now = LocalDateTime.now();
        var start = student ? p.getStudentStartAt() : p.getLecturerStartAt();
        var end = student ? p.getStudentEndAt() : p.getLecturerEndAt();
        if (now.isBefore(start) || now.isAfter(end))
            throw new IllegalArgumentException("Ngoài thời gian đăng ký của đợt này.");
    }

    public static void topicReviewWindow(RegistrationPeriod p) {
        if (!LocalDateTime.now().isBefore(p.getStudentStartAt()))
            throw new IllegalArgumentException(
                "Danh sách đề tài đã được công bố; không thể thay đổi phê duyệt sau khi giai đoạn sinh viên bắt đầu.");
    }
}
