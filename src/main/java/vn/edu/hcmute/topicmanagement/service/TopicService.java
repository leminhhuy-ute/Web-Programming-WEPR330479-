package vn.edu.hcmute.topicmanagement.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmute.topicmanagement.dto.request.TopicRequest;
import vn.edu.hcmute.topicmanagement.dto.response.TopicResponse;
import vn.edu.hcmute.topicmanagement.entity.Department;
import vn.edu.hcmute.topicmanagement.entity.RegistrationPeriod;
import vn.edu.hcmute.topicmanagement.entity.Topic;
import vn.edu.hcmute.topicmanagement.entity.User;
import vn.edu.hcmute.topicmanagement.enums.RegistrationPeriodType;
import vn.edu.hcmute.topicmanagement.enums.Role;
import vn.edu.hcmute.topicmanagement.enums.TopicStatus;
import vn.edu.hcmute.topicmanagement.exception.ConflictException;
import vn.edu.hcmute.topicmanagement.exception.ResourceNotFoundException;
import vn.edu.hcmute.topicmanagement.repository.DepartmentRepository;
import vn.edu.hcmute.topicmanagement.repository.RegistrationPeriodRepository;
import vn.edu.hcmute.topicmanagement.repository.TopicRepository;
import vn.edu.hcmute.topicmanagement.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TopicService {

    private final TopicRepository topicRepository;
    private final DepartmentRepository departmentRepository;
    private final RegistrationPeriodRepository periodRepository;
    private final UserRepository userRepository;

    public TopicService(TopicRepository topicRepository,
                        DepartmentRepository departmentRepository,
                        RegistrationPeriodRepository periodRepository,
                        UserRepository userRepository) {
        this.topicRepository = topicRepository;
        this.departmentRepository = departmentRepository;
        this.periodRepository = periodRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<TopicResponse> filterTopics(String keyword, Long departmentId, RegistrationPeriodType periodType, TopicStatus status) {
        return topicRepository.filterTopics(keyword, departmentId, periodType, status)
                .stream()
                .map(TopicResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TopicResponse> getMyTopics(Long userId) {
        return topicRepository.findByCreatedByIdOrAdvisor1IdOrAdvisor2Id(userId, userId, userId)
                .stream()
                .map(TopicResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public TopicResponse getTopicById(Long id) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề tài có ID: " + id));
        return TopicResponse.fromEntity(topic);
    }

    public TopicResponse createTopic(TopicRequest request, Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng hiện tại."));

        Department dept = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bộ môn đã chọn."));

        RegistrationPeriod period = periodRepository.findById(request.periodId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt đăng ký đã chọn."));

        String code = request.topicCode();
        if (code == null || code.isBlank()) {
            code = "DT-" + dept.getCode() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        } else if (topicRepository.existsByTopicCode(code)) {
            throw new ConflictException("Mã đề tài '" + code + "' đã tồn tại trên hệ thống.");
        }

        Topic topic = new Topic();
        topic.setTopicCode(code);
        topic.setTitle(request.title().trim());
        topic.setDescription(request.description().trim());
        topic.setRequirements(request.requirements());
        topic.setMaxStudents(request.maxStudents() > 0 ? request.maxStudents() : 3);
        topic.setTopicType(request.topicType());
        topic.setDepartment(dept);
        topic.setPeriod(period);
        topic.setCreatedBy(currentUser);
        topic.setAdvisor1(currentUser); // Mặc định người đề xuất là GVHD1
        topic.setStatus(TopicStatus.PENDING);

        return TopicResponse.fromEntity(topicRepository.save(topic));
    }

    public TopicResponse updateTopic(Long id, TopicRequest request, Long currentUserId) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề tài có ID: " + id));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng."));

        boolean isDean = currentUser.getRole() == Role.DEAN;
        boolean isCreator = topic.getCreatedBy().getId().equals(currentUserId);
        boolean isDeptHead = currentUser.getRole() == Role.HEAD_OF_DEPT &&
                currentUser.getDepartment() != null &&
                currentUser.getDepartment().getId().equals(topic.getDepartment().getId());

        if (!isDean && !isCreator && !isDeptHead) {
            throw new IllegalArgumentException("Bạn không có quyền chỉnh sửa đề tài này.");
        }

        Department dept = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bộ môn."));
        RegistrationPeriod period = periodRepository.findById(request.periodId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt đăng ký."));

        topic.setTitle(request.title().trim());
        topic.setDescription(request.description().trim());
        topic.setRequirements(request.requirements());
        topic.setMaxStudents(request.maxStudents() > 0 ? request.maxStudents() : 3);
        topic.setTopicType(request.topicType());
        topic.setDepartment(dept);
        topic.setPeriod(period);

        return TopicResponse.fromEntity(topicRepository.save(topic));
    }

    public void deleteTopic(Long id, Long currentUserId) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề tài có ID: " + id));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng."));

        boolean isDean = currentUser.getRole() == Role.DEAN;
        boolean isCreator = topic.getCreatedBy().getId().equals(currentUserId);

        if (!isDean && !isCreator) {
            throw new IllegalArgumentException("Bạn chỉ có thể xóa đề tài do chính mình tạo.");
        }

        if (topic.getStatus() == TopicStatus.APPROVED) {
            throw new ConflictException("Không thể xóa đề tài đã được duyệt chính thức.");
        }

        topicRepository.delete(topic);
    }
}
