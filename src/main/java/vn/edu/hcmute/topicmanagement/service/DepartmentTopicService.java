package vn.edu.hcmute.topicmanagement.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmute.topicmanagement.dto.request.AssignAdvisorsRequest;
import vn.edu.hcmute.topicmanagement.dto.request.GroupApprovalRequest;
import vn.edu.hcmute.topicmanagement.dto.request.TopicApprovalRequest;
import vn.edu.hcmute.topicmanagement.dto.response.TopicRegistrationResponse;
import vn.edu.hcmute.topicmanagement.dto.response.TopicResponse;
import vn.edu.hcmute.topicmanagement.entity.StudentGroup;
import vn.edu.hcmute.topicmanagement.entity.Topic;
import vn.edu.hcmute.topicmanagement.entity.TopicRegistration;
import vn.edu.hcmute.topicmanagement.entity.User;
import vn.edu.hcmute.topicmanagement.enums.GroupStatus;
import vn.edu.hcmute.topicmanagement.enums.TopicStatus;
import vn.edu.hcmute.topicmanagement.exception.ConflictException;
import vn.edu.hcmute.topicmanagement.exception.ResourceNotFoundException;
import vn.edu.hcmute.topicmanagement.repository.StudentGroupRepository;
import vn.edu.hcmute.topicmanagement.repository.TopicRegistrationRepository;
import vn.edu.hcmute.topicmanagement.repository.TopicRepository;
import vn.edu.hcmute.topicmanagement.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class DepartmentTopicService {

    private final TopicRepository topicRepository;
    private final TopicRegistrationRepository registrationRepository;
    private final StudentGroupRepository groupRepository;
    private final UserRepository userRepository;

    public DepartmentTopicService(TopicRepository topicRepository,
                                  TopicRegistrationRepository registrationRepository,
                                  StudentGroupRepository groupRepository,
                                  UserRepository userRepository) {
        this.topicRepository = topicRepository;
        this.registrationRepository = registrationRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<TopicResponse> getDepartmentTopics(Long departmentId, TopicStatus status) {
        List<Topic> topics = (status != null)
                ? topicRepository.findByDepartmentIdAndStatus(departmentId, status)
                : topicRepository.findByDepartmentId(departmentId);

        return topics.stream().map(TopicResponse::fromEntity).toList();
    }

    public TopicResponse approveOrRejectTopic(Long topicId, TopicApprovalRequest request, Long reviewerId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề tài có ID: " + topicId));

        TopicStatus newStatus = request.status();
        topic.setStatus(newStatus);

        if (newStatus == TopicStatus.REJECTED) {
            topic.setRejectionReason(request.rejectionReason());
        } else {
            topic.setRejectionReason(null);
            if (topic.getAdvisor1() == null) {
                topic.setAdvisor1(topic.getCreatedBy());
            }
        }

        return TopicResponse.fromEntity(topicRepository.save(topic));
    }

    public TopicResponse assignAdvisors(Long topicId, AssignAdvisorsRequest request, Long reviewerId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề tài có ID: " + topicId));

        if (topic.getStatus() != TopicStatus.APPROVED) {
            throw new ConflictException("Chỉ có thể phân công cán bộ hướng dẫn cho đề tài đã được phê duyệt.");
        }

        User adv1 = userRepository.findById(request.advisor1Id())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Giảng viên hướng dẫn 1."));

        User adv2 = null;
        if (request.advisor2Id() != null) {
            if (request.advisor2Id().equals(request.advisor1Id())) {
                throw new IllegalArgumentException("GVHD 1 và GVHD 2 không được trùng nhau.");
            }
            adv2 = userRepository.findById(request.advisor2Id())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Giảng viên hướng dẫn 2."));
        }

        topic.setAdvisor1(adv1);
        topic.setAdvisor2(adv2);

        return TopicResponse.fromEntity(topicRepository.save(topic));
    }

    @Transactional(readOnly = true)
    public List<TopicRegistrationResponse> getPendingRegistrationsForAdvisor(Long advisorId) {
        return registrationRepository.findByAdvisorId(advisorId)
                .stream()
                .map(TopicRegistrationResponse::fromEntity)
                .toList();
    }

    public TopicRegistrationResponse reviewGroupRegistration(Long registrationId, GroupApprovalRequest request, Long reviewerId) {
        TopicRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đăng ký đề tài có ID: " + registrationId));

        GroupStatus newStatus = request.status();
        registration.setStatus(newStatus);
        registration.setSupervisorFeedback(request.supervisorFeedback());
        registration.setReviewedAt(LocalDateTime.now());

        if (newStatus == GroupStatus.APPROVED) {
            StudentGroup group = registration.getGroup();
            group.setTopic(registration.getTopic());
            group.setStatus(GroupStatus.APPROVED);
            groupRepository.save(group);
        }

        return TopicRegistrationResponse.fromEntity(registrationRepository.save(registration));
    }
}
