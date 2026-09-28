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
import topicmanagement.repository.StudentGroupRepository;

@Service
public class DepartmentTopicService {
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final topicmanagement.council.DefenseRepository defenses;
    private final StudentGroupRepository groups;
    private final AdvisorQuotaService quotas;
    private final AuditLogService audit;

    @Autowired
    public DepartmentTopicService(TopicRepository topicRepository, UserRepository userRepository,
            topicmanagement.council.DefenseRepository defenses, StudentGroupRepository groups,
            AdvisorQuotaService quotas, AuditLogService audit) {
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
        this.defenses = defenses;
        this.groups = groups;
        this.quotas = quotas;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<TopicResponse> getDepartmentTopics(Long departmentId, String statusStr) {
        TopicStatus status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            try { status = TopicStatus.valueOf(statusStr); } catch (IllegalArgumentException ignored) {}
        }
        
        List<Topic> topics;
        if (departmentId == null) {
            topics = (status != null) ? topicRepository.findByStatus(status) : topicRepository.findAll();
        } else if (status != null) {
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
        TopicPolicy.topicReviewWindow(topic.getPeriod());
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
        TopicStatus oldStatus = topic.getStatus();
        topic.setStatus(newStatus);
        if (newStatus == TopicStatus.REJECTED) {
            topic.setRejectionReason(request.getRejectionReason());
        } else {
            topic.setRejectionReason(null);
            // Mặc định gán GV tạo đề tài làm GVHD1 nếu chưa gán
            if (topic.getAdvisor1() == null) {
                User creator = topic.getCreatedBy();
                if (creator == null || creator.getStatus() != topicmanagement.enums.UserStatus.ACTIVE
                        || creator.getRole() == topicmanagement.enums.Role.STUDENT) {
                    throw new IllegalArgumentException("Không thể duyệt đề tài: người tạo đề tài không hoạt động hoặc không đủ điều kiện làm GVHD.");
                }
                if (creator.getRole() != topicmanagement.enums.Role.DEAN
                        && (creator.getDepartment() == null || !creator.getDepartment().getId().equals(topic.getDepartment().getId()))) {
                    throw new IllegalArgumentException("Không thể duyệt đề tài: người tạo đề tài không thuộc bộ môn này.");
                }
                topic.setAdvisor1(creator);
            }
            User adv1 = topic.getAdvisor1();
            TopicPolicy.staff(adv1);
            if (adv1.getRole() != topicmanagement.enums.Role.DEAN
                    && (adv1.getDepartment() == null || !adv1.getDepartment().getId().equals(topic.getDepartment().getId()))) {
                throw new IllegalArgumentException("GVHD 1 không thuộc bộ môn của đề tài.");
            }
            if (topic.getAdvisor2() != null) {
                User adv2 = topic.getAdvisor2();
                TopicPolicy.staff(adv2);
                if (adv2.getId().equals(adv1.getId())) {
                    throw new IllegalArgumentException("GVHD 1 và GVHD 2 không được trùng nhau.");
                }
            }
        }

        Topic saved = topicRepository.save(topic);
        audit.log(currentUser, newStatus == TopicStatus.APPROVED ? "APPROVE_TOPIC" : "REJECT_TOPIC",
            "Topic", topicId, "status=" + oldStatus,
            "status=" + newStatus + (request.getRejectionReason() == null ? "" : ",reason=" + request.getRejectionReason()));
        return new TopicResponse(saved);
    }

    @Transactional
    public TopicResponse assignAdvisors(Long topicId, AssignAdvisorsRequest request, User currentUser) {
        Topic topic = topicRepository.lockById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề tài có ID: " + topicId));

        TopicPolicy.departmentManager(currentUser, topic.getDepartment().getId());
        if (defenses.existsByGroupTopicId(topicId))
            throw new ConflictException("Không đổi GVHD sau khi đã phân công hội đồng.");
        if (topic.getStatus() == TopicStatus.REJECTED) {
            throw new ConflictException("Không thể phân công GVHD cho đề tài đã bị từ chối.");
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
        if (adv1.getRole() != topicmanagement.enums.Role.DEAN
                && (adv1.getDepartment() == null || !adv1.getDepartment().getId().equals(topic.getDepartment().getId()))) {
            throw new IllegalArgumentException("GVHD 1 không thuộc bộ môn của đề tài.");
        }
        if (adv2 != null) TopicPolicy.staff(adv2);
        long approvedGroups = groups.findByTopicIdAndStatus(topicId, topicmanagement.enums.GroupStatus.APPROVED).size();
        if (topic.getAdvisor1() == null || !topic.getAdvisor1().getId().equals(adv1.getId()))
            quotas.assertAvailable(topic.getPeriod(), adv1, approvedGroups);
        if (adv2 != null && (topic.getAdvisor2() == null || !topic.getAdvisor2().getId().equals(adv2.getId())))
            quotas.assertAvailable(topic.getPeriod(), adv2, approvedGroups);
        String oldAdvisors = "advisor1=" + (topic.getAdvisor1() == null ? null : topic.getAdvisor1().getId())
            + ",advisor2=" + (topic.getAdvisor2() == null ? null : topic.getAdvisor2().getId());
        topic.setAdvisor1(adv1);
        topic.setAdvisor2(adv2);

        Topic saved = topicRepository.save(topic);
        audit.log(currentUser, "ASSIGN_ADVISORS", "Topic", topicId, oldAdvisors,
            "advisor1=" + adv1.getId() + ",advisor2=" + (adv2 == null ? null : adv2.getId()));
        return new TopicResponse(saved);
    }
}
