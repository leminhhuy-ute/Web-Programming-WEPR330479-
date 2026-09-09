package vn.edu.hcmute.topicmanagement.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmute.topicmanagement.domain.*;
import vn.edu.hcmute.topicmanagement.repository.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional
public class GradingService {
    private final TopicRepository topicRepository;
    private final CouncilRepository councilRepository;
    private final CouncilMemberRepository memberRepository;
    private final LecturerRepository lecturerRepository;
    private final EvaluationRepository evaluationRepository;

    public GradingService(TopicRepository topicRepository, CouncilRepository councilRepository,
                          CouncilMemberRepository memberRepository, LecturerRepository lecturerRepository,
                          EvaluationRepository evaluationRepository) {
        this.topicRepository = topicRepository; this.councilRepository = councilRepository;
        this.memberRepository = memberRepository; this.lecturerRepository = lecturerRepository;
        this.evaluationRepository = evaluationRepository;
    }

    public void assign(Long topicId, Long councilId, Long reviewerId) {
        Topic topic = topicRepository.findById(topicId).orElseThrow(() -> new BusinessException("Không tìm thấy đề tài."));
        Council council = councilRepository.findWithMembersById(councilId).orElseThrow(() -> new BusinessException("Không tìm thấy hội đồng."));
        Lecturer reviewer = lecturerRepository.findById(reviewerId).orElseThrow(() -> new BusinessException("Không tìm thấy giảng viên phản biện."));
        if (council.getStatus() != CouncilStatus.READY)
            throw new BusinessException("Chỉ có thể phân công cho hội đồng đã sẵn sàng.");
        if (!memberRepository.existsByCouncilIdAndLecturerId(councilId, reviewerId))
            throw new BusinessException("Giảng viên phản biện phải là thành viên hội đồng.");
        if (topic.getAdvisor().getId().equals(reviewerId))
            throw new BusinessException("Giảng viên không được phản biện đề tài mình hướng dẫn.");
        topic.setCouncil(council); topic.setReviewer(reviewer); topic.setStatus(TopicStatus.ASSIGNED);
    }

    public void grade(Long topicId, Long evaluatorId, BigDecimal score, String comment) {
        Topic topic = topicRepository.findDetailById(topicId).orElseThrow(() -> new BusinessException("Không tìm thấy đề tài."));
        Lecturer evaluator = lecturerRepository.findById(evaluatorId).orElseThrow(() -> new BusinessException("Không tìm thấy giảng viên."));
        if (topic.getCouncil() == null) throw new BusinessException("Đề tài chưa được phân công hội đồng.");
        if (topic.getAdvisor().getId().equals(evaluatorId))
            throw new BusinessException("Giảng viên không được chấm đề tài mình hướng dẫn.");
        if (!memberRepository.existsByCouncilIdAndLecturerId(topic.getCouncil().getId(), evaluatorId))
            throw new BusinessException("Chỉ thành viên hội đồng mới được chấm đề tài.");
        if (score == null || score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(BigDecimal.TEN) > 0)
            throw new BusinessException("Điểm phải nằm trong khoảng 0–10.");
        if (comment == null || comment.isBlank()) throw new BusinessException("Vui lòng nhập nhận xét.");
        Evaluation evaluation = evaluationRepository.findByTopicIdAndEvaluatorId(topicId, evaluatorId)
                .orElseGet(() -> new Evaluation(topic, evaluator, score, comment.trim()));
        evaluation.setScore(score); evaluation.setComment(comment.trim());
        evaluationRepository.save(evaluation);
        List<Evaluation> evaluations = evaluationRepository.findByTopicIdOrderByEvaluatorFullNameAsc(topicId);
        BigDecimal average = evaluations.stream().map(Evaluation::getScore).reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(evaluations.size()), 2, RoundingMode.HALF_UP);
        topic.setFinalScore(average);
        topic.setStatus(evaluations.size() >= eligibleEvaluatorCount(topic) ? TopicStatus.GRADED : TopicStatus.GRADING);
    }

    private long eligibleEvaluatorCount(Topic topic) {
        return memberRepository.findByCouncilId(topic.getCouncil().getId()).stream()
                .filter(member -> !member.getLecturer().getId().equals(topic.getAdvisor().getId())).count();
    }
}
