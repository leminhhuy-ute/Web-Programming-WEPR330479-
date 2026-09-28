package topicmanagement.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import topicmanagement.dto.request.AdvisorQuotaRequest;
import topicmanagement.dto.response.AdvisorQuotaResponse;
import topicmanagement.entity.*;
import topicmanagement.enums.*;
import topicmanagement.exception.ResourceNotFoundException;
import topicmanagement.repository.*;

@Service
@Transactional
public class AdvisorQuotaService {
    private final AdvisorQuotaRepository quotas;
    private final RegistrationPeriodRepository periods;
    private final UserRepository users;
    private final TopicRegistrationRepository registrations;

    public AdvisorQuotaService(AdvisorQuotaRepository quotas, RegistrationPeriodRepository periods,
            UserRepository users, TopicRegistrationRepository registrations) {
        this.quotas = quotas;
        this.periods = periods;
        this.users = users;
        this.registrations = registrations;
    }

    @Transactional(readOnly = true)
    public List<AdvisorQuotaResponse> findAll() {
        return quotas.findAll().stream().map(this::map).toList();
    }

    public AdvisorQuotaResponse save(AdvisorQuotaRequest request) {
        RegistrationPeriod period = periods.findById(request.periodId())
            .orElseThrow(() -> new ResourceNotFoundException("Đợt đăng ký không tồn tại."));
        User lecturer = users.findById(request.lecturerId())
            .orElseThrow(() -> new ResourceNotFoundException("Giảng viên không tồn tại."));
        TopicPolicy.staff(lecturer);
        AdvisorQuota quota = quotas.findByRegistrationPeriodIdAndLecturerId(period.getId(), lecturer.getId())
            .orElseGet(AdvisorQuota::new);
        quota.setRegistrationPeriod(period);
        quota.setLecturer(lecturer);
        quota.setMaxGroups(request.maxGroups());
        long current = current(period.getId(), lecturer.getId());
        if (current > request.maxGroups())
            throw new IllegalArgumentException("Hạn mức mới thấp hơn số nhóm giảng viên đang hướng dẫn (" + current + ").");
        return map(quotas.save(quota));
    }

    public void assertAvailable(RegistrationPeriod period, User lecturer, long additionalGroups) {
        var quota = quotas.lockByPeriodAndLecturer(period.getId(), lecturer.getId());
        if (quota.isEmpty()) return; // Không cấu hình quota để giữ tương thích dữ liệu/demo cũ.
        long current = current(period.getId(), lecturer.getId());
        if (current + additionalGroups > quota.get().getMaxGroups())
            throw new IllegalArgumentException("Giảng viên " + lecturer.getFullName()
                + " đã đạt hạn mức " + quota.get().getMaxGroups() + " nhóm trong đợt này.");
    }

    private long current(Long periodId, Long lecturerId) {
        return registrations.countAdvisorGroups(periodId, lecturerId, RegistrationStatus.APPROVED);
    }

    private AdvisorQuotaResponse map(AdvisorQuota q) {
        return new AdvisorQuotaResponse(q.getId(), q.getRegistrationPeriod().getId(),
            q.getRegistrationPeriod().getName(), q.getLecturer().getId(), q.getLecturer().getFullName(),
            q.getMaxGroups(), current(q.getRegistrationPeriod().getId(), q.getLecturer().getId()));
    }
}
