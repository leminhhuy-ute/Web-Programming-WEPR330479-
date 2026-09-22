package topicmanagement.service;

import java.time.LocalDateTime;
import java.util.Objects;
import org.springframework.security.access.AccessDeniedException;
import topicmanagement.entity.*;
import topicmanagement.enums.*;

public final class TopicPolicy {
    private TopicPolicy() {}
    public static void staff(User u) {
        if (u.getStatus() != UserStatus.ACTIVE || u.getRole() == Role.STUDENT)
            throw new AccessDeniedException("Yêu cầu tài khoản giảng viên đang hoạt động.");
    }
    public static void departmentManager(User u, Long departmentId) {
        staff(u);
        if (u.getRole() == Role.DEAN) return;
        if (u.getRole() != Role.HEAD_OF_DEPT || u.getDepartment() == null
                || !Objects.equals(u.getDepartment().getId(), departmentId))
            throw new AccessDeniedException("Chỉ trưởng bộ môn phụ trách hoặc trưởng khoa được duyệt.");
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
}
