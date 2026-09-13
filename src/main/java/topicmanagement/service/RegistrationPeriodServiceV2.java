package topicmanagement.service;

import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import topicmanagement.dto.request.RegistrationPeriodRequest;
import topicmanagement.dto.response.RegistrationPeriodResponse;
import topicmanagement.entity.RegistrationPeriod;
import topicmanagement.enums.RegistrationPeriodType;
import topicmanagement.exception.*;
import topicmanagement.repository.RegistrationPeriodRepository;

@Service
@Transactional
public class RegistrationPeriodServiceV2 {
  private final RegistrationPeriodRepository periods;

  public RegistrationPeriodServiceV2(RegistrationPeriodRepository p) {
    periods = p;
  }

  public List<RegistrationPeriodResponse> findAll(String q, RegistrationPeriodType t) {
    return periods.search(q == null ? "" : q.trim(), t).stream().map(this::map).toList();
  }

  public RegistrationPeriodResponse findById(Long id) {
    return map(entity(id));
  }

  public RegistrationPeriodResponse create(RegistrationPeriodRequest r) {
    return save(new RegistrationPeriod(), r);
  }

  public RegistrationPeriodResponse update(Long id, RegistrationPeriodRequest r) {
    return save(entity(id), r);
  }

  private RegistrationPeriodResponse save(RegistrationPeriod p, RegistrationPeriodRequest r) {
    p.setName(r.name().trim());
    p.setType(r.type());
    p.setLecturerStartAt(r.lecturerStartAt());
    p.setLecturerEndAt(r.lecturerEndAt());
    p.setStudentStartAt(r.studentStartAt());
    p.setStudentEndAt(r.studentEndAt());
    p.setReviewDeadline(r.reviewDeadline());
    p.setDefenseDate(r.defenseDate());
    validate(p);
    return map(periods.save(p));
  }

  public void validate(RegistrationPeriod p) {
    if (!p.getLecturerStartAt().isBefore(p.getLecturerEndAt())
        || !p.getStudentStartAt().isBefore(p.getStudentEndAt()))
      throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu.");
    if (!p.getLecturerEndAt().isBefore(p.getStudentStartAt()))
      throw new IllegalArgumentException(
          "Thời gian đăng ký sinh viên phải bắt đầu sau khi đăng ký giảng viên kết thúc.");
    if (p.getType() == RegistrationPeriodType.COURSE
        || p.getType() == RegistrationPeriodType.NCKH) {
      p.setReviewDeadline(null);
      p.setDefenseDate(null);
      return;
    }
    if (p.getReviewDeadline() == null)
      throw new IllegalArgumentException("Đợt " + p.getType() + " bắt buộc phải có hạn GVPB.");
    if (!p.getReviewDeadline().isAfter(p.getStudentEndAt()))
      throw new IllegalArgumentException("Hạn GVPB phải sau thời gian kết thúc đăng ký sinh viên.");
    if (p.getType() == RegistrationPeriodType.TLCN) {
      p.setDefenseDate(null);
      return;
    }
    if (p.getDefenseDate() == null)
      throw new IllegalArgumentException("Đợt KLTN bắt buộc phải có ngày báo cáo hội đồng.");
    if (!p.getDefenseDate().isAfter(p.getReviewDeadline().toLocalDate()))
      throw new IllegalArgumentException("Ngày báo cáo hội đồng phải sau ngày hạn GVPB.");
  }

  public void delete(Long id) {
    periods.delete(entity(id));
  }

  private RegistrationPeriod entity(Long id) {
    return periods
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Đợt đăng ký không tồn tại."));
  }

  private RegistrationPeriodResponse map(RegistrationPeriod p) {
    String label = p.getType() == RegistrationPeriodType.COURSE ? "Môn học" : p.getType().name();
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
        p.getUpdatedAt());
  }
}
