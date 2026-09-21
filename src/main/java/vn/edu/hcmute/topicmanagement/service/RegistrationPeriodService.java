package vn.edu.hcmute.topicmanagement.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmute.topicmanagement.dto.request.RegistrationPeriodRequest;
import vn.edu.hcmute.topicmanagement.dto.response.RegistrationPeriodResponse;
import vn.edu.hcmute.topicmanagement.entity.RegistrationPeriod;
import vn.edu.hcmute.topicmanagement.enums.RegistrationPeriodType;
import vn.edu.hcmute.topicmanagement.exception.ResourceNotFoundException;
import vn.edu.hcmute.topicmanagement.repository.RegistrationPeriodRepository;

import java.util.List;

@Service
@Transactional
public class RegistrationPeriodService {

    private final RegistrationPeriodRepository periodRepository;

    public RegistrationPeriodService(RegistrationPeriodRepository periodRepository) {
        this.periodRepository = periodRepository;
    }

    public List<RegistrationPeriodResponse> findAll(String keyword, RegistrationPeriodType type) {
        return periodRepository.search(keyword == null ? "" : keyword.trim(), type)
                .stream()
                .map(this::map)
                .toList();
    }

    public RegistrationPeriodResponse findById(Long id) {
        return map(getEntity(id));
    }

    public RegistrationPeriodResponse create(RegistrationPeriodRequest r) {
        return save(new RegistrationPeriod(), r);
    }

    public RegistrationPeriodResponse update(Long id, RegistrationPeriodRequest r) {
        return save(getEntity(id), r);
    }

    private RegistrationPeriodResponse save(RegistrationPeriod period, RegistrationPeriodRequest r) {
        period.setName(r.name().trim());
        period.setType(r.type());
        period.setLecturerStartAt(r.lecturerStartAt());
        period.setLecturerEndAt(r.lecturerEndAt());
        period.setStudentStartAt(r.studentStartAt());
        period.setStudentEndAt(r.studentEndAt());
        period.setReviewDeadline(r.reviewDeadline());
        period.setDefenseDate(r.defenseDate());

        validate(period);

        return map(periodRepository.save(period));
    }

    public void validate(RegistrationPeriod p) {
        if (!p.getLecturerStartAt().isBefore(p.getLecturerEndAt()) || !p.getStudentStartAt().isBefore(p.getStudentEndAt())) {
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu.");
        }
        if (!p.getLecturerEndAt().isBefore(p.getStudentStartAt())) {
            throw new IllegalArgumentException("Thời gian đăng ký sinh viên phải bắt đầu sau khi đăng ký giảng viên kết thúc.");
        }
        if (p.getType() == RegistrationPeriodType.COURSE || p.getType() == RegistrationPeriodType.NCKH) {
            p.setReviewDeadline(null);
            p.setDefenseDate(null);
            return;
        }
        if (p.getReviewDeadline() == null) {
            throw new IllegalArgumentException("Đợt " + p.getType() + " bắt buộc phải có hạn giảng viên phản biện.");
        }
        if (!p.getReviewDeadline().isAfter(p.getStudentEndAt())) {
            throw new IllegalArgumentException("Hạn giảng viên phản biện phải sau thời gian kết thúc đăng ký sinh viên.");
        }
        if (p.getType() == RegistrationPeriodType.TLCN) {
            p.setDefenseDate(null);
            return;
        }
        if (p.getDefenseDate() == null) {
            throw new IllegalArgumentException("Đợt KLTN bắt buộc phải có ngày báo cáo hội đồng.");
        }
        if (!p.getDefenseDate().isAfter(p.getReviewDeadline().toLocalDate())) {
            throw new IllegalArgumentException("Ngày báo cáo hội đồng phải sau ngày hạn giảng viên phản biện.");
        }
    }

    public void delete(Long id) {
        periodRepository.delete(getEntity(id));
    }

    private RegistrationPeriod getEntity(Long id) {
        return periodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đợt đăng ký không tồn tại."));
    }

    public RegistrationPeriodResponse map(RegistrationPeriod p) {
        String label = switch (p.getType()) {
            case KLTN -> "Khóa luận tốt nghiệp";
            case TLCN -> "Tiểu luận chuyên ngành";
            case COURSE -> "Đồ án môn học";
            case NCKH -> "Nghiên cứu khoa học";
        };

        return new RegistrationPeriodResponse(
                p.getId(),
                p.getName(),
                p.getType().name(),
                label,
                p.getLecturerStartAt(),
                p.getLecturerEndAt(),
                p.getStudentStartAt(),
                p.getStudentEndAt(),
                p.getReviewDeadline(),
                p.getDefenseDate(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}
