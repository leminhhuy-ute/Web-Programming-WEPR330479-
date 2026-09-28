package topicmanagement.student;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.zip.ZipInputStream;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import topicmanagement.council.DefenseRepository;
import topicmanagement.council.Defense;
import topicmanagement.dto.response.ReportSummaryResponse;
import topicmanagement.dto.response.PageResponse;
import topicmanagement.entity.*;
import topicmanagement.enums.*;
import topicmanagement.repository.*;
import topicmanagement.service.AuditLogService;

@Service
@Transactional(readOnly = true)
public class ReportService {
    private final ReportRepository reports;
    private final GroupMemberRepository members;
    private final StudentGroupRepository groups;
    private final DefenseRepository defenses;
    private final AuditLogService audit;
    private final TopicRegistrationRepository topicRegistrations;

    public ReportService(ReportRepository reports, GroupMemberRepository members,
            StudentGroupRepository groups,
            DefenseRepository defenses, AuditLogService audit) {
        this(reports, members, groups, defenses, audit, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public ReportService(ReportRepository reports, GroupMemberRepository members,
            StudentGroupRepository groups,
            DefenseRepository defenses, AuditLogService audit,
            @org.springframework.beans.factory.annotation.Autowired(required = false) TopicRegistrationRepository topicRegistrations) {
        this.reports = reports;
        this.members = members;
        this.groups = groups;
        this.defenses = defenses;
        this.audit = audit;
        this.topicRegistrations = topicRegistrations;
    }

    @Transactional
    public void upload(User actor, StudentGroup group, String stage, String note, MultipartFile file) throws IOException {
        if (!group.getLeader().getId().equals(actor.getId())) throw denied();
        if (group.getStatus() != GroupStatus.APPROVED)
            throw bad("Chỉ nộp báo cáo sau khi nhóm được duyệt.");
        if (!List.of("Đề cương", "Giữa kỳ", "Cuối kỳ").contains(stage))
            throw bad("Loại báo cáo không hợp lệ.");
        if (note == null || note.length() > 1000) throw bad("Ghi chú tối đa 1.000 ký tự.");
        if (file.isEmpty() || file.getSize() > 10L * 1024 * 1024)
            throw bad("Chọn tệp PDF hoặc DOCX, tối đa 10 MB.");

        TopicRegistration reg = null;
        if (topicRegistrations != null) {
            reg = topicRegistrations.findFirstByGroupIdAndStatusInOrderByCreatedAtDesc(
                group.getId(), List.of(RegistrationStatus.APPROVED)).orElse(null);
        }

        // Validate stage ordering: Đề cương -> Giữa kỳ -> Cuối kỳ
        if ("Giữa kỳ".equals(stage)) {
            boolean hasDeCuong = reg != null ? reports.existsByRegistrationIdAndStage(reg.getId(), "Đề cương")
                : reports.existsByGroupIdAndStage(group.getId(), "Đề cương");
            if (!hasDeCuong) {
                throw bad("Cần nộp báo cáo Đề cương trước khi nộp báo cáo Giữa kỳ.");
            }
        } else if ("Cuối kỳ".equals(stage)) {
            boolean hasGiuaKy = reg != null ? reports.existsByRegistrationIdAndStage(reg.getId(), "Giữa kỳ")
                : reports.existsByGroupIdAndStage(group.getId(), "Giữa kỳ");
            if (!hasGiuaKy) {
                throw bad("Cần nộp báo cáo Giữa kỳ trước khi nộp báo cáo Cuối kỳ.");
            }
        }

        Topic topic = (reg != null) ? reg.getTopic() : group.getTopic();
        RegistrationPeriod period = (topic != null) ? topic.getPeriod() : group.getPeriod();
        if (period != null) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(period.getStudentStartAt())) {
                throw bad("Chưa đến thời gian nộp báo cáo.");
            }
            if (period.getType() == RegistrationPeriodType.KLTN && period.getDefenseDate() != null) {
                if (now.toLocalDate().isAfter(period.getDefenseDate())) {
                    throw bad("Đã quá ngày báo cáo hội đồng.");
                }
            } else if (period.getReviewDeadline() != null) {
                if (now.isAfter(period.getReviewDeadline())) {
                    throw bad("Đã quá hạn nộp báo cáo.");
                }
            } else if (now.isAfter(period.getStudentEndAt())) {
                throw bad("Đã quá hạn nộp báo cáo của đợt này.");
            }
        }

        String filename = sanitizeFilename(file.getOriginalFilename());
        byte[] bytes = file.getBytes();
        String contentType = validatedContentType(filename, bytes);
        Report report = new Report();
        report.group = group;
        report.registration = reg;
        report.topic = topic;
        report.submittedBy = actor;
        report.filename = filename;
        report.contentType = contentType;
        report.stage = stage;
        report.note = note.strip();
        report.reportSize = (long) bytes.length;
        report.checksum = sha256(bytes);
        report.storagePath = null; // Reserved for private object storage without changing the demo workflow.
        report.content = bytes;
        report.late = false;
        reports.save(report);
        audit.log(actor, "UPLOAD_REPORT", "Report", report.id, null,
            "filename=" + filename + ",sha256=" + report.checksum + ",size=" + report.reportSize);
    }

    public List<ReportSummaryResponse> listForStudent(User actor, StudentGroup group) {
        assertGroupMember(actor, group);
        return reports.findSummariesByGroupId(group.getId());
    }

    public List<ReportSummaryResponse> listForAdvisor(User actor, Long groupId) {
        StudentGroup group = groupFromMembershipOrReport(groupId);
        assertAdvisor(actor, group);
        return reports.findSummariesByGroupId(groupId);
    }

    public PageResponse<ReportSummaryResponse> pageForAdvisor(User actor, Long groupId, int page, int size) {
        StudentGroup group = group(groupId);
        assertAdvisor(actor, group);
        return summaryPage(groupId, page, size);
    }

    public List<ReportSummaryResponse> listForCouncil(User actor, Long defenseId) {
        var defense = defenses.findById(defenseId).orElseThrow(this::denied);
        assertCouncilMember(actor, defense);
        return reports.findSummariesByGroupId(defense.group.getId());
    }

    public Map<Long,List<ReportSummaryResponse>> listForCouncil(User actor, List<Defense> defenseList) {
        defenseList.forEach(defense -> assertCouncilMember(actor, defense));
        if (defenseList.isEmpty()) return Map.of();
        List<Long> groupIds = defenseList.stream().map(defense -> defense.group.getId()).distinct().toList();
        return reports.findSummariesByGroupIdIn(groupIds).stream()
            .collect(java.util.stream.Collectors.groupingBy(ReportSummaryResponse::groupId));
    }

    public PageResponse<ReportSummaryResponse> pageForCouncil(User actor, Long defenseId, int page, int size) {
        var defense = defenses.findById(defenseId).orElseThrow(this::denied);
        assertCouncilMember(actor, defense);
        return summaryPage(defense.group.getId(), page, size);
    }

    public ReportSummaryResponse metadataForAdvisor(User actor, Long reportId) {
        ReportSummaryResponse summary = reports.findSummaryById(reportId).orElseThrow(this::denied);
        assertAdvisor(actor, group(summary.groupId()));
        return summary;
    }

    public ReportSummaryResponse metadataForCouncil(User actor, Long reportId) {
        ReportSummaryResponse summary = reports.findSummaryById(reportId).orElseThrow(this::denied);
        var defense = defenses.findByGroupId(summary.groupId()).orElseThrow(this::denied);
        assertCouncilMember(actor, defense);
        return summary;
    }

    @Transactional
    public Report downloadForStudent(User actor, Long id) {
        Report report = reports.findById(id).orElseThrow(this::denied);
        assertGroupMember(actor, report.group);
        logDownload(actor, report, "STUDENT_DOWNLOAD_REPORT");
        return report;
    }

    @Transactional
    public Report downloadForAdvisor(User actor, Long id) {
        Report report = reports.findById(id).orElseThrow(this::denied);
        assertAdvisor(actor, report.group);
        logDownload(actor, report, "ADVISOR_DOWNLOAD_REPORT");
        return report;
    }

    @Transactional
    public Report downloadForCouncil(User actor, Long id) {
        Report report = reports.findById(id).orElseThrow(this::denied);
        var defense = defenses.findByGroupId(report.group.getId()).orElseThrow(this::denied);
        assertCouncilMember(actor, defense);
        logDownload(actor, report, "COUNCIL_DOWNLOAD_REPORT");
        return report;
    }

    private void assertGroupMember(User actor, StudentGroup group) {
        if (members.findByStudentId(actor.getId()).stream()
                .noneMatch(m -> m.getGroup().getId().equals(group.getId()))) throw denied();
    }

    private void assertAdvisor(User actor, StudentGroup group) {
        Topic topic = group.getTopic();
        boolean advisor = topic != null && ((topic.getAdvisor1() != null && topic.getAdvisor1().getId().equals(actor.getId()))
            || (topic.getAdvisor2() != null && topic.getAdvisor2().getId().equals(actor.getId())));
        if (!advisor && actor.getRole() != Role.DEAN) throw denied();
    }

    private void assertCouncilMember(User actor, topicmanagement.council.Defense defense) {
        boolean councilMember = defense.council.getMembers().stream()
            .anyMatch(m -> m.getLecturer().getId().equals(actor.getId()));
        if (!councilMember && actor.getRole() != Role.DEAN) throw denied();
    }

    private StudentGroup groupFromMembershipOrReport(Long groupId) {
        return group(groupId);
    }

    private StudentGroup group(Long groupId) {
        return groups.findById(groupId).orElseThrow(this::denied);
    }

    private PageResponse<ReportSummaryResponse> summaryPage(Long groupId, int page, int size) {
        var pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100),
            Sort.by(Sort.Direction.DESC, "submittedAt"));
        return PageResponse.from(reports.findSummariesByGroupId(groupId, pageable));
    }

    private void logDownload(User actor, Report report, String action) {
        audit.log(actor, action, "Report", report.id, null, "sha256=" + report.checksum);
    }

    private String sanitizeFilename(String original) {
        String filename = Objects.toString(original, "report").replace('\\', '/');
        filename = filename.substring(filename.lastIndexOf('/') + 1)
            .replaceAll("[\\p{Cntrl}]", "").strip();
        if (filename.isBlank() || filename.length() > 180) throw bad("Tên tệp không hợp lệ hoặc quá dài.");
        return filename;
    }

    private String validatedContentType(String filename, byte[] bytes) {
        String lower = filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".pdf") && isPdf(bytes))
            return "application/pdf";
        if (lower.endsWith(".docx") && isDocx(bytes))
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        throw bad("Nội dung tệp không phải PDF hoặc DOCX hợp lệ.");
    }

    private boolean isPdf(byte[] bytes) {
        if (bytes == null || bytes.length < 20) return false;
        String content = new String(bytes, StandardCharsets.ISO_8859_1);
        if (!content.startsWith("%PDF-")) return false;
        if (!content.contains("%%EOF")) return false;
        return content.contains("obj") && (content.contains("endobj") || content.contains("trailer") || content.contains("xref"));
    }

    private boolean isDocx(byte[] bytes) {
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            boolean content = false, document = false;
            int entries = 0;
            long expanded = 0;
            byte[] buffer = new byte[8192];
            for (var entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                if (++entries > 1000) return false;
                if (entry.getName().equals("[Content_Types].xml")) content = true;
                if (entry.getName().equals("word/document.xml")) document = true;
                for (int n; (n = zip.read(buffer)) != -1;) {
                    expanded += n;
                    if (expanded > 30L * 1024 * 1024) return false;
                }
            }
            return content && document;
        } catch (IOException ex) {
            return false;
        }
    }

    private String sha256(byte[] content) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(content);
            return java.util.HexFormat.of().formatHex(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể tạo checksum báo cáo.", ex);
        }
    }

    private IllegalArgumentException bad(String message) { return new IllegalArgumentException(message); }
    private AccessDeniedException denied() { return new AccessDeniedException("Bạn không có quyền xem báo cáo này."); }
}
