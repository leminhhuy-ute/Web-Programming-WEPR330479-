package vn.edu.hcmute.topicmanagement.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmute.topicmanagement.domain.*;
import vn.edu.hcmute.topicmanagement.repository.*;

@Service
@Transactional
public class CouncilService {
    private final CouncilRepository councilRepository;
    private final CouncilMemberRepository memberRepository;
    private final LecturerRepository lecturerRepository;
    private final TopicRepository topicRepository;

    public CouncilService(CouncilRepository councilRepository, CouncilMemberRepository memberRepository,
                          LecturerRepository lecturerRepository, TopicRepository topicRepository) {
        this.councilRepository = councilRepository;
        this.memberRepository = memberRepository;
        this.lecturerRepository = lecturerRepository;
        this.topicRepository = topicRepository;
    }

    public Council save(Council council) {
        return councilRepository.save(council);
    }

    public void addMember(Long councilId, Long lecturerId, CouncilRole role) {
        Council council = councilRepository.findById(councilId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy hội đồng."));
        Lecturer lecturer = lecturerRepository.findById(lecturerId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy giảng viên."));
        if (!lecturer.isActive()) throw new BusinessException("Giảng viên đã ngừng hoạt động.");
        if (memberRepository.countByCouncilId(councilId) >= 5)
            throw new BusinessException("Mỗi hội đồng chỉ được có tối đa 5 giảng viên.");
        if (memberRepository.existsByCouncilIdAndLecturerId(councilId, lecturerId))
            throw new BusinessException("Giảng viên đã có trong hội đồng.");
        if ((role == CouncilRole.CHAIRPERSON || role == CouncilRole.SECRETARY)
                && memberRepository.existsByCouncilIdAndRole(councilId, role))
            throw new BusinessException("Hội đồng đã có " + role.getLabel().toLowerCase() + ".");
        memberRepository.save(new CouncilMember(council, lecturer, role));
    }

    public void removeMember(Long councilId, Long memberId) {
        CouncilMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy thành viên."));
        if (!member.getCouncil().getId().equals(councilId))
            throw new BusinessException("Thành viên không thuộc hội đồng này.");
        if (topicRepository.countByCouncilId(councilId) > 0)
            throw new BusinessException("Không thể xóa thành viên khi hội đồng đã được phân công đề tài.");
        memberRepository.delete(member);
    }

    public void markReady(Long councilId) {
        Council council = councilRepository.findWithMembersById(councilId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy hội đồng."));
        int size = council.getMembers().size();
        if (size < 3 || size > 5) throw new BusinessException("Hội đồng phải có từ 3 đến 5 giảng viên.");
        boolean hasChair = council.getMembers().stream().anyMatch(m -> m.getRole() == CouncilRole.CHAIRPERSON);
        boolean hasSecretary = council.getMembers().stream().anyMatch(m -> m.getRole() == CouncilRole.SECRETARY);
        if (!hasChair || !hasSecretary) throw new BusinessException("Hội đồng phải có Chủ tịch và Thư ký.");
        council.setStatus(CouncilStatus.READY);
    }
}
