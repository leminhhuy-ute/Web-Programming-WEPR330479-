package vn.edu.hcmute.topicmanagement.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmute.topicmanagement.dto.request.AssignCouncilAndReviewerRequest;
import vn.edu.hcmute.topicmanagement.dto.request.GradeEvaluationRequest;
import vn.edu.hcmute.topicmanagement.dto.response.EvaluationResponse;
import vn.edu.hcmute.topicmanagement.entity.*;
import vn.edu.hcmute.topicmanagement.enums.CouncilStatus;
import vn.edu.hcmute.topicmanagement.enums.EvaluationType;
import vn.edu.hcmute.topicmanagement.exception.ConflictException;
import vn.edu.hcmute.topicmanagement.exception.ResourceNotFoundException;
import vn.edu.hcmute.topicmanagement.repository.*;

import java.util.List;

@Service
@Transactional
public class GradingService {

    private final TopicRepository topicRepository;
    private final CouncilRepository councilRepository;
    private final CouncilMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final EvaluationRepository evaluationRepository;

    public GradingService(TopicRepository topicRepository,
                          CouncilRepository councilRepository,
                          CouncilMemberRepository memberRepository,
                          UserRepository userRepository,
                          EvaluationRepository evaluationRepository) {
        this.topicRepository = topicRepository;
        this.councilRepository = councilRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.evaluationRepository = evaluationRepository;
    }

    public void assignCouncilAndReviewer(AssignCouncilAndReviewerRequest request) {
        Topic topic = topicRepository.findById(request.topicId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề tài có ID: " + request.topicId()));

        Council council = councilRepository.findById(request.councilId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hội đồng có ID: " + request.councilId()));

        User reviewer = userRepository.findById(request.reviewerId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên phản biện có ID: " + request.reviewerId()));

        if (council.getStatus() != CouncilStatus.READY && council.getStatus() != CouncilStatus.ACTIVE) {
            throw new ConflictException("Chỉ có thể phân công cho hội đồng đã sẵn sàng (READY / ACTIVE).");
        }

        if (!memberRepository.existsByCouncilIdAndLecturerId(council.getId(), reviewer.getId())) {
            throw new ConflictException("Giảng viên phản biện phải là thành viên trong hội đồng đã chọn.");
        }

        if (topic.getAdvisor1() != null && topic.getAdvisor1().getId().equals(reviewer.getId())) {
            throw new ConflictException("Giảng viên hướng dẫn không được làm cán bộ phản biện cho đề tài của mình.");
        }

        if (topic.getAdvisor2() != null && topic.getAdvisor2().getId().equals(reviewer.getId())) {
            throw new ConflictException("Giảng viên đồng hướng dẫn không được làm cán bộ phản biện cho đề tài của mình.");
        }
    }

    public EvaluationResponse gradeTopic(GradeEvaluationRequest request, Long evaluatorId) {
        Topic topic = topicRepository.findById(request.topicId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề tài có ID: " + request.topicId()));

        User evaluator = userRepository.findById(evaluatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cán bộ chấm điểm."));

        if (topic.getAdvisor1() != null && topic.getAdvisor1().getId().equals(evaluatorId)) {
            throw new ConflictException("Giảng viên hướng dẫn không được tham gia chấm điểm hội đồng/phản biện cho đề tài của mình.");
        }

        if (topic.getAdvisor2() != null && topic.getAdvisor2().getId().equals(evaluatorId)) {
            throw new ConflictException("Giảng viên đồng hướng dẫn không được tham gia chấm điểm cho đề tài của mình.");
        }

        if (request.score() < 0.0 || request.score() > 10.0) {
            throw new IllegalArgumentException("Điểm đánh giá phải nằm trong thang điểm từ 0.0 đến 10.0.");
        }

        Council council = null;
        if (request.councilId() != null) {
            council = councilRepository.findById(request.councilId()).orElse(null);
        }

        EvaluationType type = request.evaluationType() != null ? request.evaluationType() : EvaluationType.COUNCIL_MEMBER;

        var existingOpt = evaluationRepository.findByTopicAndEvaluatorAndEvaluationType(topic, evaluator, type);
        Evaluation evaluation;
        if (existingOpt.isPresent()) {
            evaluation = existingOpt.get();
            evaluation.setScore(request.score());
            evaluation.setFeedback(request.feedback().trim());
            if (council != null) evaluation.setCouncil(council);
        } else {
            evaluation = new Evaluation(
                    council,
                    topic,
                    evaluator,
                    type,
                    request.score(),
                    request.feedback().trim()
            );
        }

        return EvaluationResponse.fromEntity(evaluationRepository.save(evaluation));
    }

    @Transactional(readOnly = true)
    public List<EvaluationResponse> getTopicEvaluations(Long topicId) {
        return evaluationRepository.findByTopicId(topicId).stream()
                .map(EvaluationResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EvaluationResponse> getCouncilEvaluations(Long councilId) {
        return evaluationRepository.findByCouncilId(councilId).stream()
                .map(EvaluationResponse::fromEntity)
                .toList();
    }
}
