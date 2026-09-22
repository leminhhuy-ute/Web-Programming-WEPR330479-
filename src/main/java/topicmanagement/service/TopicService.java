package topicmanagement.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import topicmanagement.dto.request.TopicRequest;
import topicmanagement.dto.response.TopicResponse;
import topicmanagement.entity.Department;
import topicmanagement.entity.RegistrationPeriod;
import topicmanagement.entity.Topic;
import topicmanagement.entity.User;
import topicmanagement.enums.RegistrationPeriodType;
import topicmanagement.enums.TopicStatus;
import topicmanagement.exception.ConflictException;
import topicmanagement.exception.ResourceNotFoundException;
import topicmanagement.repository.DepartmentRepository;
import topicmanagement.repository.RegistrationPeriodRepository;
import topicmanagement.repository.TopicRepository;
import topicmanagement.repository.UserRepository;

@Service
public class TopicService {
    private final TopicRepository topicRepository;
    private final DepartmentRepository departmentRepository;
    private final RegistrationPeriodRepository periodRepository;
    private final UserRepository userRepository;

    @Autowired
    public TopicService(
            TopicRepository topicRepository,
            DepartmentRepository departmentRepository,
            RegistrationPeriodRepository periodRepository,
            UserRepository userRepository) {
        this.topicRepository = topicRepository;
        this.departmentRepository = departmentRepository;
        this.periodRepository = periodRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<TopicResponse> filterTopics(
            Long departmentId,
            Long periodId,
            String typeStr,
            String statusStr,
            Long createdById,
            String search) {

        RegistrationPeriodType type = null;
        if (typeStr != null && !typeStr.isBlank()) {
            try { type = RegistrationPeriodType.valueOf(typeStr); } catch (IllegalArgumentException ignored) {}
        }

        TopicStatus status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            try { status = TopicStatus.valueOf(statusStr); } catch (IllegalArgumentException ignored) {}
        }

        List<Topic> topics = topicRepository.filterTopics(departmentId, periodId, type, status, createdById, search);
        return topics.stream().map(TopicResponse::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TopicResponse getTopicById(Long id) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề tài có ID: " + id));
        return new TopicResponse(topic);
    }

    @Transactional
    public TopicResponse createTopic(TopicRequest request, User currentUser) {
        Department dept = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bộ môn."));
        RegistrationPeriod period = periodRepository.findById(request.getPeriodId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt đăng ký."));

        String code = request.getTopicCode();
        if (code == null || code.isBlank()) {
            code = "DT-" + dept.getCode() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        } else if (topicRepository.existsByTopicCode(code)) {
            throw new ConflictException("Mã đề tài '" + code + "' đã tồn tại trên hệ thống.");
        }

        RegistrationPeriodType type;
        try {
            type = RegistrationPeriodType.valueOf(request.getTopicType());
        } catch (Exception e) {
            throw new IllegalArgumentException("Loại đề tài không hợp lệ: " + request.getTopicType());
        }

        Topic topic = new Topic();
        topic.setTopicCode(code);
        topic.setTitle(request.getTitle());
        topic.setDescription(request.getDescription());
        topic.setRequirements(request.getRequirements());
        topic.setMaxStudents(request.getMaxStudents());
        topic.setTopicType(type);
        topic.setDepartment(dept);
        topic.setPeriod(period);
        topic.setCreatedBy(currentUser);
        topic.setStatus(TopicStatus.PENDING);

        Topic saved = topicRepository.save(topic);
        return new TopicResponse(saved);
    }

    @Transactional
    public TopicResponse updateTopic(Long id, TopicRequest request, User currentUser) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề tài có ID: " + id));

        // Ownership or department check
        if (!topic.getCreatedBy().getId().equals(currentUser.getId()) &&
            (currentUser.getDepartment() == null || !currentUser.getDepartment().getId().equals(topic.getDepartment().getId()))) {
            throw new IllegalArgumentException("Bạn không có quyền chỉnh sửa đề tài này.");
        }

        Department dept = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bộ môn."));
        RegistrationPeriod period = periodRepository.findById(request.getPeriodId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt đăng ký."));

        RegistrationPeriodType type;
        try {
            type = RegistrationPeriodType.valueOf(request.getTopicType());
        } catch (Exception e) {
            throw new IllegalArgumentException("Loại đề tài không hợp lệ: " + request.getTopicType());
        }

        topic.setTitle(request.getTitle());
        topic.setDescription(request.getDescription());
        topic.setRequirements(request.getRequirements());
        topic.setMaxStudents(request.getMaxStudents());
        topic.setTopicType(type);
        topic.setDepartment(dept);
        topic.setPeriod(period);

        Topic updated = topicRepository.save(topic);
        return new TopicResponse(updated);
    }

    @Transactional
    public void deleteTopic(Long id, User currentUser) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề tài có ID: " + id));

        if (!topic.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Bạn chỉ có thể xóa đề tài do chính mình tạo.");
        }

        if (topic.getStatus() == TopicStatus.APPROVED) {
            throw new ConflictException("Không thể xóa đề tài đã được duyệt.");
        }

        topicRepository.delete(topic);
    }
}
