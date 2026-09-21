package vn.edu.hcmute.topicmanagement.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmute.topicmanagement.dto.request.CouncilMemberRequest;
import vn.edu.hcmute.topicmanagement.dto.request.CouncilRequest;
import vn.edu.hcmute.topicmanagement.dto.response.CouncilMemberResponse;
import vn.edu.hcmute.topicmanagement.dto.response.CouncilResponse;
import vn.edu.hcmute.topicmanagement.entity.*;
import vn.edu.hcmute.topicmanagement.enums.CouncilRole;
import vn.edu.hcmute.topicmanagement.enums.CouncilStatus;
import vn.edu.hcmute.topicmanagement.exception.ConflictException;
import vn.edu.hcmute.topicmanagement.exception.ResourceNotFoundException;
import vn.edu.hcmute.topicmanagement.repository.*;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CouncilService {

    private final CouncilRepository councilRepository;
    private final CouncilMemberRepository memberRepository;
    private final DepartmentRepository departmentRepository;
    private final RegistrationPeriodRepository periodRepository;
    private final UserRepository userRepository;

    public CouncilService(CouncilRepository councilRepository,
                          CouncilMemberRepository memberRepository,
                          DepartmentRepository departmentRepository,
                          RegistrationPeriodRepository periodRepository,
                          UserRepository userRepository) {
        this.councilRepository = councilRepository;
        this.memberRepository = memberRepository;
        this.departmentRepository = departmentRepository;
        this.periodRepository = periodRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<CouncilResponse> findAll(Long departmentId, CouncilStatus status) {
        List<Council> councils = councilRepository.findAll();
        return councils.stream()
                .filter(c -> departmentId == null || (c.getDepartment() != null && c.getDepartment().getId().equals(departmentId)))
                .filter(c -> status == null || c.getStatus() == status)
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CouncilResponse findById(Long id) {
        Council c = councilRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hội đồng có ID: " + id));
        return mapToResponse(c);
    }

    public CouncilResponse createCouncil(CouncilRequest request) {
        Department dept = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bộ môn."));

        RegistrationPeriod period = null;
        if (request.periodId() != null) {
            period = periodRepository.findById(request.periodId()).orElse(null);
        }

        String code = request.code();
        if (code == null || code.isBlank()) {
            code = "HD-" + dept.getCode() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        }

        Council council = new Council(
                code,
                request.name().trim(),
                dept,
                period,
                request.defenseDate(),
                request.defenseTime(),
                request.room()
        );
        council.setStatus(CouncilStatus.PENDING);

        return mapToResponse(councilRepository.save(council));
    }

    public CouncilResponse updateCouncil(Long id, CouncilRequest request) {
        Council council = councilRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hội đồng có ID: " + id));

        Department dept = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bộ môn."));

        RegistrationPeriod period = null;
        if (request.periodId() != null) {
            period = periodRepository.findById(request.periodId()).orElse(null);
        }

        council.setName(request.name().trim());
        council.setDepartment(dept);
        council.setPeriod(period);
        council.setDefenseDate(request.defenseDate());
        council.setDefenseTime(request.defenseTime());
        council.setRoom(request.room());

        return mapToResponse(councilRepository.save(council));
    }

    public CouncilMemberResponse addMember(Long councilId, CouncilMemberRequest request) {
        Council council = councilRepository.findById(councilId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hội đồng có ID: " + councilId));

        User lecturer = userRepository.findById(request.lecturerId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên có ID: " + request.lecturerId()));

        if (memberRepository.countByCouncilId(councilId) >= 5) {
            throw new ConflictException("Mỗi hội đồng bảo vệ chỉ được phép có tối đa 5 giảng viên.");
        }

        if (memberRepository.existsByCouncilIdAndLecturerId(councilId, request.lecturerId())) {
            throw new ConflictException("Giảng viên " + lecturer.getFullName() + " đã có trong hội đồng.");
        }

        CouncilRole role = request.role();
        boolean isChair = (role == CouncilRole.CHAIR || role == CouncilRole.CHAIRPERSON);
        boolean isSecretary = (role == CouncilRole.SECRETARY);

        if (isChair && (memberRepository.existsByCouncilIdAndMemberRole(councilId, CouncilRole.CHAIR) ||
                memberRepository.existsByCouncilIdAndMemberRole(councilId, CouncilRole.CHAIRPERSON))) {
            throw new ConflictException("Hội đồng đã có Chủ tịch hội đồng.");
        }

        if (isSecretary && memberRepository.existsByCouncilIdAndMemberRole(councilId, CouncilRole.SECRETARY)) {
            throw new ConflictException("Hội đồng đã có Thư ký hội đồng.");
        }

        CouncilMember member = new CouncilMember(council, lecturer, role);
        return CouncilMemberResponse.fromEntity(memberRepository.save(member));
    }

    public void removeMember(Long councilId, Long memberId) {
        CouncilMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thành viên hội đồng."));

        if (!member.getCouncil().getId().equals(councilId)) {
            throw new IllegalArgumentException("Thành viên không thuộc hội đồng này.");
        }

        memberRepository.delete(member);
    }

    public CouncilResponse markReady(Long councilId) {
        Council council = councilRepository.findById(councilId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hội đồng có ID: " + councilId));

        List<CouncilMember> members = memberRepository.findByCouncilId(councilId);
        int size = members.size();
        if (size < 3 || size > 5) {
            throw new ConflictException("Hội đồng bảo vệ phải có từ 3 đến 5 giảng viên (hiện có " + size + " thành viên).");
        }

        boolean hasChair = members.stream().anyMatch(m -> m.getMemberRole() == CouncilRole.CHAIR || m.getMemberRole() == CouncilRole.CHAIRPERSON);
        boolean hasSecretary = members.stream().anyMatch(m -> m.getMemberRole() == CouncilRole.SECRETARY);

        if (!hasChair || !hasSecretary) {
            throw new ConflictException("Hội đồng bắt buộc phải có đầy đủ Chủ tịch hội đồng và Thư ký.");
        }

        council.setStatus(CouncilStatus.READY);
        return mapToResponse(councilRepository.save(council));
    }

    public void deleteCouncil(Long id) {
        Council council = councilRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hội đồng có ID: " + id));
        councilRepository.delete(council);
    }

    private CouncilResponse mapToResponse(Council council) {
        List<CouncilMemberResponse> members = memberRepository.findByCouncilId(council.getId()).stream()
                .map(CouncilMemberResponse::fromEntity)
                .toList();
        return CouncilResponse.fromEntity(council, members);
    }
}
