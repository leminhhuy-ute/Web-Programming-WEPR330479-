package topicmanagement.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import topicmanagement.dto.request.AssignAdvisorsRequest;
import topicmanagement.dto.request.TopicApprovalRequest;
import topicmanagement.dto.response.TopicResponse;
import topicmanagement.entity.Topic;
import topicmanagement.entity.User;
import topicmanagement.enums.TopicStatus;
import topicmanagement.exception.ConflictException;
import topicmanagement.exception.ResourceNotFoundException;
import topicmanagement.repository.TopicRepository;
import topicmanagement.repository.UserRepository;

@Service
public class DepartmentTopicService {
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final topicmanagement.council.DefenseRepository defenses;

    @Autowired
    public DepartmentTopicService(TopicRepository topicRepository, UserRepository userRepository,
            topicmanagement.council.DefenseRepository defenses) {
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
        this.defenses = defenses;
    }

    @Transactional(readOnly = true)
    public List<TopicResponse> getDepartmentTopics(Long departmentId, String statusStr) {
        TopicStatus status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            try { status = TopicStatus.valueOf(statusStr); } catch (IllegalArgumentException ignored) {}
        }
        
        List<Topic> topics;
        if (status != null) {
            topics = topicRepository.findByDepartmentIdAndStatus(departmentId, status);
        } else {
            topics = topicRepository.findByDepartmentId(departmentId);
        }
        return topics.stream().map(TopicResponse::new).collect(Collectors.toList());
    }

    @Transactional
    public TopicResponse approveOrRejectTopic(Long topicId, TopicApprovalRequest request, User currentUser) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề tài có ID: " + topicId));

        TopicPolicy.departmentManager(currentUser, topic.getDepartment().getId());
        TopicStatus newStatus;
        try {
            newStatus = TopicStatus.valueOf(request.getStatus());
        } catch (Exception e) {
            throw new IllegalArgumentException("Trạng thái phê duyệt không hợp lệ (Phải là APPROVED hoặc REJECTED).");
        }

        if (newStatus == TopicStatus.PENDING) throw new IllegalArgumentException("Chỉ chấp nhận APPROVED hoặc REJECTED.");
        if (topic.getStatus() == TopicStatus.APPROVED) throw new ConflictException("Đề tài đã công bố không được đổi trạng thái.");
        if (newStatus == TopicStatus.REJECTED && (request.getRejectionReason() == null || request.getRejectionReason().isBlank()))
            throw new IllegalArgumentException("Vui lòng nhập lý do từ chối.");
        topic.setStatus(newStatus);
        if (newStatus == TopicStatus.REJECTED) {
            topic.setRejectionReason(request.getRejectionReason());
        } else {
            topic.setRejectionReason(null);
            // Mặc định gán GV tạo đề tài làm GVHD1 nếu chưa gán
            if (topic.getAdvisor1() == null) {
                topic.setAdvisor1(topic.getCreatedBy());
            }
        }

        Topic saved = topicRepository.save(topic);
        return new TopicResponse(saved);
    }

    @Transactional
    public TopicResponse assignAdvisors(Long topicId, AssignAdvisorsRequest request, User currentUser) {
        if (defenses.existsByGroupTopicId(topicId))
            throw new ConflictException("Không đổi GVHD sau khi đã phân công hội đồng.");
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề tài có ID: " + topicId));

        TopicPolicy.departmentManager(currentUser, topic.getDepartment().getId());
        if (topic.getStatus() != TopicStatus.APPROVED) {
            throw new ConflictException("Chỉ có thể phân công GVHD cho đề tài đã được duyệt (APPROVED).");
        }

        User adv1 = userRepository.findById(request.getAdvisor1Id())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Giảng viên hướng dẫn 1."));
        
        User adv2 = null;
        if (request.getAdvisor2Id() != null) {
            if (request.getAdvisor2Id().equals(request.getAdvisor1Id())) {
                throw new IllegalArgumentException("GVHD 1 và GVHD 2 không được trùng nhau.");
            }
            adv2 = userRepository.findById(request.getAdvisor2Id())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Giảng viên hướng dẫn 2."));
        }

        TopicPolicy.staff(adv1);
        if (adv2 != null) TopicPolicy.staff(adv2);
        topic.setAdvisor1(adv1);
        topic.setAdvisor2(adv2);

        Topic saved = topicRepository.save(topic);
        return new TopicResponse(saved);
    }
}
