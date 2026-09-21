package vn.edu.hcmute.topicmanagement.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.hcmute.topicmanagement.dto.request.*;
import vn.edu.hcmute.topicmanagement.dto.response.*;
import vn.edu.hcmute.topicmanagement.entity.*;
import vn.edu.hcmute.topicmanagement.enums.*;
import vn.edu.hcmute.topicmanagement.exception.ConflictException;
import vn.edu.hcmute.topicmanagement.exception.ResourceNotFoundException;
import vn.edu.hcmute.topicmanagement.repository.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class StudentGroupService {

    private final StudentGroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupInvitationRepository invitationRepository;
    private final TopicRegistrationRepository registrationRepository;
    private final ProgressReportRepository reportRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public StudentGroupService(StudentGroupRepository groupRepository,
                               GroupMemberRepository groupMemberRepository,
                               GroupInvitationRepository invitationRepository,
                               TopicRegistrationRepository registrationRepository,
                               ProgressReportRepository reportRepository,
                               TopicRepository topicRepository,
                               UserRepository userRepository,
                               UserService userService) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.invitationRepository = invitationRepository;
        this.registrationRepository = registrationRepository;
        this.reportRepository = reportRepository;
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public StudentWorkspaceResponse getWorkspace(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin sinh viên."));

        UserResponse userResponse = userService.map(student);

        StudentGroupResponse groupResponse = null;
        TopicRegistrationResponse registrationResponse = null;
        List<ProgressReportResponse> reportResponses = List.of();

        var memberOpt = groupMemberRepository.findByStudent(student);
        if (memberOpt.isPresent()) {
            StudentGroup group = memberOpt.get().getGroup();
            List<GroupMemberResponse> members = groupMemberRepository.findByGroup(group).stream()
                    .map(GroupMemberResponse::fromEntity)
                    .toList();
            groupResponse = StudentGroupResponse.fromEntity(group, members);

            var regList = registrationRepository.findByGroupOrderByRegisteredAtDesc(group);
            if (!regList.isEmpty()) {
                registrationResponse = TopicRegistrationResponse.fromEntity(regList.get(0));
            }

            reportResponses = reportRepository.findByGroupOrderBySubmittedAtDesc(group).stream()
                    .map(ProgressReportResponse::fromEntity)
                    .toList();
        }

        List<InvitationResponse> pendingInvitations = invitationRepository
                .findByInviteeAndStatus(student, InvitationStatus.PENDING).stream()
                .map(InvitationResponse::fromEntity)
                .toList();

        return new StudentWorkspaceResponse(userResponse, groupResponse, registrationResponse, pendingInvitations, reportResponses);
    }

    public StudentGroupResponse createGroup(CreateGroupRequest request, Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin sinh viên."));

        if (groupMemberRepository.existsByStudent(student)) {
            throw new ConflictException("Bạn đã tham gia một nhóm sinh viên, không thể tạo nhóm mới.");
        }

        String groupCode = "GRP-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        StudentGroup group = new StudentGroup(
                groupCode,
                request.groupName().trim(),
                student,
                3,
                request.notes()
        );
        StudentGroup savedGroup = groupRepository.save(group);

        GroupMember leaderMember = new GroupMember(savedGroup, student, MemberRole.LEADER);
        groupMemberRepository.save(leaderMember);

        return StudentGroupResponse.fromEntity(savedGroup, List.of(GroupMemberResponse.fromEntity(leaderMember)));
    }

    public InvitationResponse inviteMember(InviteMemberRequest request, Long currentUserId) {
        User leader = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin sinh viên."));

        var memberOpt = groupMemberRepository.findByStudent(leader);
        if (memberOpt.isEmpty() || memberOpt.get().getRole() != MemberRole.LEADER) {
            throw new IllegalArgumentException("Chỉ nhóm trưởng mới có quyền gửi lời mời tham gia nhóm.");
        }

        StudentGroup group = memberOpt.get().getGroup();
        if (group.isFull()) {
            throw new ConflictException("Nhóm đã đạt số lượng tối đa 3 thành viên.");
        }

        String code = request.studentCode().trim();
        User target = userRepository.findByUserCode(code)
                .or(() -> userRepository.findByUsername(code))
                .or(() -> userRepository.findByEmailIgnoreCase(code))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sinh viên có MSSV: " + code));

        if (target.getId().equals(leader.getId())) {
            throw new IllegalArgumentException("Không thể tự mời chính mình vào nhóm.");
        }

        if (target.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException("Người dùng được mời không phải là sinh viên.");
        }

        if (groupMemberRepository.existsByStudent(target)) {
            throw new ConflictException("Sinh viên " + target.getFullName() + " (" + target.getUserCode() + ") đã thuộc một nhóm khác.");
        }

        var existingInvite = invitationRepository.findByGroupAndInviteeAndStatus(group, target, InvitationStatus.PENDING);
        if (existingInvite.isPresent()) {
            throw new ConflictException("Bạn đã gửi lời mời đang chờ phản hồi tới sinh viên này.");
        }

        GroupInvitation invitation = new GroupInvitation(group, leader, target, request.message());
        return InvitationResponse.fromEntity(invitationRepository.save(invitation));
    }

    public void respondInvitation(Long invitationId, boolean accept, Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin sinh viên."));

        GroupInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lời mời."));

        if (!invitation.getInvitee().getId().equals(studentId)) {
            throw new IllegalArgumentException("Bạn không có quyền phản hồi lời mời này.");
        }

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new ConflictException("Lời mời này đã được xử lý trước đó.");
        }

        if (accept) {
            if (groupMemberRepository.existsByStudent(student)) {
                throw new ConflictException("Bạn đã tham gia một nhóm khác.");
            }

            StudentGroup group = invitation.getGroup();
            if (group.isFull()) {
                throw new ConflictException("Nhóm đã đủ 3 thành viên, không thể tham gia.");
            }

            groupMemberRepository.save(new GroupMember(group, student, MemberRole.MEMBER));
            group.setMemberCount(group.getMemberCount() + 1);
            groupRepository.save(group);

            invitation.setStatus(InvitationStatus.ACCEPTED);

            // Decline all other pending invites for this student
            var otherInvites = invitationRepository.findByInviteeAndStatus(student, InvitationStatus.PENDING);
            for (var other : otherInvites) {
                if (!other.getId().equals(invitation.getId())) {
                    other.setStatus(InvitationStatus.REJECTED);
                    invitationRepository.save(other);
                }
            }
        } else {
            invitation.setStatus(InvitationStatus.REJECTED);
        }

        invitationRepository.save(invitation);
    }

    public void transferLeader(TransferLeaderRequest request, Long currentUserId) {
        User leader = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin người dùng."));

        var leaderMemberOpt = groupMemberRepository.findByStudent(leader);
        if (leaderMemberOpt.isEmpty() || leaderMemberOpt.get().getRole() != MemberRole.LEADER) {
            throw new IllegalArgumentException("Chỉ nhóm trưởng hiện tại mới có quyền chuyển giao nhóm.");
        }

        StudentGroup group = leaderMemberOpt.get().getGroup();
        User newLeader = userRepository.findById(request.newLeaderId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thành viên được chỉ định."));

        var targetMemberOpt = groupMemberRepository.findByGroupAndStudent(group, newLeader);
        if (targetMemberOpt.isEmpty()) {
            throw new IllegalArgumentException("Nhóm trưởng mới phải là thành viên hiện tại trong nhóm.");
        }

        GroupMember currentLeaderGm = leaderMemberOpt.get();
        currentLeaderGm.setRole(MemberRole.MEMBER);
        groupMemberRepository.save(currentLeaderGm);

        GroupMember targetGm = targetMemberOpt.get();
        targetGm.setRole(MemberRole.LEADER);
        groupMemberRepository.save(targetGm);

        group.setLeader(newLeader);
        groupRepository.save(group);
    }

    public void leaveGroup(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin sinh viên."));

        var memberOpt = groupMemberRepository.findByStudent(student);
        if (memberOpt.isEmpty()) {
            throw new ConflictException("Bạn chưa tham gia nhóm nào.");
        }

        GroupMember gm = memberOpt.get();
        StudentGroup group = gm.getGroup();

        if (gm.getRole() == MemberRole.LEADER && group.getMemberCount() > 1) {
            throw new ConflictException("Nhóm trưởng cần chuyển giao quyền trưởng nhóm cho thành viên khác trước khi rời nhóm.");
        }

        if (group.getMemberCount() <= 1) {
            groupMemberRepository.delete(gm);
            groupRepository.delete(group);
        } else {
            groupMemberRepository.delete(gm);
            group.setMemberCount(group.getMemberCount() - 1);
            groupRepository.save(group);
        }
    }

    public TopicRegistrationResponse registerTopic(RegisterTopicRequest request, Long studentId) {
        User leader = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin sinh viên."));

        var memberOpt = groupMemberRepository.findByStudent(leader);
        if (memberOpt.isEmpty() || memberOpt.get().getRole() != MemberRole.LEADER) {
            throw new IllegalArgumentException("Chỉ nhóm trưởng mới có quyền đăng ký đề tài.");
        }

        StudentGroup group = memberOpt.get().getGroup();

        var existingApproved = registrationRepository.findFirstByGroupAndStatus(group, GroupStatus.APPROVED);
        if (existingApproved.isPresent()) {
            throw new ConflictException("Nhóm đã có đề tài được duyệt chính thức: " + existingApproved.get().getTopic().getTitle());
        }

        Topic topic = topicRepository.findById(request.topicId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề tài có ID: " + request.topicId()));

        if (topic.getStatus() != TopicStatus.APPROVED) {
            throw new ConflictException("Chỉ có thể đăng ký đề tài đã được Khoa và Bộ môn phê duyệt.");
        }

        TopicRegistration reg = new TopicRegistration(group, topic, request.proposalNote());
        return TopicRegistrationResponse.fromEntity(registrationRepository.save(reg));
    }

    public ProgressReportResponse submitReport(SubmitReportRequest request, MultipartFile file, Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin sinh viên."));

        var memberOpt = groupMemberRepository.findByStudent(student);
        if (memberOpt.isEmpty()) {
            throw new ConflictException("Bạn chưa tham gia nhóm sinh viên.");
        }

        StudentGroup group = memberOpt.get().getGroup();
        if (group.getTopic() == null && registrationRepository.findFirstByGroupAndStatus(group, GroupStatus.APPROVED).isEmpty()) {
            throw new ConflictException("Nhóm cần có đề tài được duyệt trước khi nộp báo cáo tiến độ.");
        }

        String fileName = null;
        String fileUrl = request.attachmentUrl();
        if (file != null && !file.isEmpty()) {
            if (file.getSize() > 10 * 1024 * 1024) {
                throw new IllegalArgumentException("Tệp tải lên không được vượt quá 10MB.");
            }
            fileName = file.getOriginalFilename();
            if (fileUrl == null || fileUrl.isBlank()) {
                fileUrl = "/uploads/reports/" + fileName;
            }
        }

        ProgressReport report = new ProgressReport(
                group,
                student,
                request.reportTitle().trim(),
                request.stage().trim(),
                request.contentSummary(),
                fileUrl,
                fileName,
                request.completionPercentage()
        );

        return ProgressReportResponse.fromEntity(reportRepository.save(report));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> searchAvailableStudents(String keyword) {
        return userRepository.searchAvailableStudents(keyword == null ? "" : keyword.trim()).stream()
                .map(userService::map)
                .toList();
    }
}
