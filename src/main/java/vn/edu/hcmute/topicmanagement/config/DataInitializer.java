package vn.edu.hcmute.topicmanagement.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vn.edu.hcmute.topicmanagement.entity.*;
import vn.edu.hcmute.topicmanagement.enums.*;
import vn.edu.hcmute.topicmanagement.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final RegistrationPeriodRepository periodRepository;
    private final AnnouncementRepository announcementRepository;
    private final TopicRepository topicRepository;
    private final StudentGroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupInvitationRepository invitationRepository;
    private final TopicRegistrationRepository registrationRepository;
    private final ProgressReportRepository reportRepository;
    private final CouncilRepository councilRepository;
    private final CouncilMemberRepository councilMemberRepository;
    private final EvaluationRepository evaluationRepository;

    public DataInitializer(DepartmentRepository departmentRepository,
                           UserRepository userRepository,
                           RegistrationPeriodRepository periodRepository,
                           AnnouncementRepository announcementRepository,
                           TopicRepository topicRepository,
                           StudentGroupRepository groupRepository,
                           GroupMemberRepository groupMemberRepository,
                           GroupInvitationRepository invitationRepository,
                           TopicRegistrationRepository registrationRepository,
                           ProgressReportRepository reportRepository,
                           CouncilRepository councilRepository,
                           CouncilMemberRepository councilMemberRepository,
                           EvaluationRepository evaluationRepository) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.periodRepository = periodRepository;
        this.announcementRepository = announcementRepository;
        this.topicRepository = topicRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.invitationRepository = invitationRepository;
        this.registrationRepository = registrationRepository;
        this.reportRepository = reportRepository;
        this.councilRepository = councilRepository;
        this.councilMemberRepository = councilMemberRepository;
        this.evaluationRepository = evaluationRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        // 1. Departments
        Department cnpm = departmentRepository.save(new Department("CNPM", "Bộ môn Công nghệ Phần mềm", "Khoa Công nghệ Thông tin - HCMUTE"));
        Department khmt = departmentRepository.save(new Department("KHMT", "Bộ môn Khoa học Máy tính", "Khoa Công nghệ Thông tin - HCMUTE"));
        Department httt = departmentRepository.save(new Department("HTTT", "Bộ môn Hệ thống Thông tin", "Khoa Công nghệ Thông tin - HCMUTE"));
        Department ktmt = departmentRepository.save(new Department("KTMT", "Bộ môn Kỹ thuật Máy tính", "Khoa Công nghệ Thông tin - HCMUTE"));
        Department attt = departmentRepository.save(new Department("ATTT", "Bộ môn An toàn Thông tin", "Khoa Công nghệ Thông tin - HCMUTE"));

        // BCrypt hash for "Password@123"
        String defaultHash = "$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a";

        // 2. Users
        User dean = userRepository.save(new User("DEAN01", "dean", defaultHash, "PGS.TS. Trần Văn Trưởng Khoa", "dean@hcmute.edu.vn", Role.DEAN, cnpm));
        User head = userRepository.save(new User("HEAD01", "head_cnpm", defaultHash, "TS. Nguyễn Thị Trưởng Bộ Môn", "head.cnpm@hcmute.edu.vn", Role.HEAD_OF_DEPT, cnpm));
        User lec1 = userRepository.save(new User("GV001", "lecturer1", defaultHash, "TS. Nguyễn Hoàng Nam", "nam.nh@hcmute.edu.vn", Role.LECTURER, cnpm));
        User lec2 = userRepository.save(new User("GV002", "lecturer2", defaultHash, "PGS.TS. Trần Thị Minh Châu", "chau.ttm@hcmute.edu.vn", Role.LECTURER, khmt));
        User rev1 = userRepository.save(new User("GV003", "reviewer1", defaultHash, "ThS. Vũ Hải Đăng", "dang.vh@hcmute.edu.vn", Role.LECTURER, attt));

        // Students (HCMUTE Web Programming team)
        User svHuy = userRepository.save(new User("24110019", "24110019", defaultHash, "Lê Minh Huy", "24110019@student.hcmute.edu.vn", Role.STUDENT, cnpm));
        User svDo = userRepository.save(new User("24110012", "24110012", defaultHash, "Trần Hữu Thành Đô", "24110012@student.hcmute.edu.vn", Role.STUDENT, cnpm));
        User svBaoHuy = userRepository.save(new User("24162043", "24162043", defaultHash, "Lê Bảo Huy", "24162043@student.hcmute.edu.vn", Role.STUDENT, cnpm));
        User svQuangHuy = userRepository.save(new User("24162048", "24162048", defaultHash, "Nguyễn Quang Huy", "24162048@student.hcmute.edu.vn", Role.STUDENT, cnpm));
        User svMinh = userRepository.save(new User("24110033", "24110033", defaultHash, "Lê Đăng Minh", "24110033@student.hcmute.edu.vn", Role.STUDENT, cnpm));
        User svAnh = userRepository.save(new User("22110001", "22110001", defaultHash, "Nguyễn Minh Anh", "22110001@student.hcmute.edu.vn", Role.STUDENT, cnpm));
        User svLinh = userRepository.save(new User("22110002", "22110002", defaultHash, "Trần Ngọc Linh", "22110002@student.hcmute.edu.vn", Role.STUDENT, cnpm));

        // 3. Registration Periods
        RegistrationPeriod pKltn = new RegistrationPeriod();
        pKltn.setName("Khóa luận tốt nghiệp HK1 2026-2027");
        pKltn.setType(RegistrationPeriodType.KLTN);
        pKltn.setLecturerStartAt(LocalDateTime.now().minusDays(30));
        pKltn.setLecturerEndAt(LocalDateTime.now().minusDays(15));
        pKltn.setStudentStartAt(LocalDateTime.now().minusDays(14));
        pKltn.setStudentEndAt(LocalDateTime.now().plusDays(20));
        pKltn.setReviewDeadline(LocalDateTime.now().plusDays(45));
        pKltn.setDefenseDate(LocalDate.now().plusDays(60));
        pKltn = periodRepository.save(pKltn);

        RegistrationPeriod pTlcn = new RegistrationPeriod();
        pTlcn.setName("Tiểu luận chuyên ngành HK1 2026-2027");
        pTlcn.setType(RegistrationPeriodType.TLCN);
        pTlcn.setLecturerStartAt(LocalDateTime.now().minusDays(20));
        pTlcn.setLecturerEndAt(LocalDateTime.now().minusDays(10));
        pTlcn.setStudentStartAt(LocalDateTime.now().minusDays(9));
        pTlcn.setStudentEndAt(LocalDateTime.now().plusDays(15));
        pTlcn.setReviewDeadline(LocalDateTime.now().plusDays(35));
        periodRepository.save(pTlcn);

        RegistrationPeriod pCourse = new RegistrationPeriod();
        pCourse.setName("Đồ án môn học Web Programming HK1");
        pCourse.setType(RegistrationPeriodType.COURSE);
        pCourse.setLecturerStartAt(LocalDateTime.now().minusDays(10));
        pCourse.setLecturerEndAt(LocalDateTime.now().minusDays(5));
        pCourse.setStudentStartAt(LocalDateTime.now().minusDays(4));
        pCourse.setStudentEndAt(LocalDateTime.now().plusDays(30));
        periodRepository.save(pCourse);

        // 4. Announcements
        announcementRepository.save(new Announcement(
                "Thông báo khởi động đợt đăng ký Khóa luận tốt nghiệp HK1",
                "Khoa CNTT thông báo lịch đăng ký đề tài KLTN cho sinh viên năm cuối. Đề nghị các nhóm sinh viên hoàn thành lập nhóm và chọn đề tài đúng hạn.",
                AnnouncementAudience.ALL, dean));

        announcementRepository.save(new Announcement(
                "Nhắc nhở Giảng viên cập nhật danh mục đề tài",
                "Kính đề nghị quý Thầy/Cô bộ môn gửi đề xuất đề tài lên hệ thống trước hạn để Hội đồng khoa phê duyệt.",
                AnnouncementAudience.LECTURER, dean));

        announcementRepository.save(new Announcement(
                "Hướng dẫn nộp đề cương và quy cách trình bày báo cáo",
                "Các nhóm sinh viên đã được duyệt đề tài chú ý tải mẫu đề cương chi tiết tại thư mục tài liệu và nộp đúng hạn cột mốc 1.",
                AnnouncementAudience.STUDENT, dean));

        // 5. Topics
        Topic t1 = new Topic();
        t1.setTopicCode("KLTN26-001");
        t1.setTitle("Hệ thống Quản lý Chuỗi cung ứng nông sản ứng dụng Blockchain và IoT");
        t1.setDescription("Nghiên cứu giải pháp truy xuất nguồn gốc nông sản từ nông trại đến bàn ăn, lưu trữ hash giao dịch trên Blockchain Polygon/Ethereum.");
        t1.setRequirements("Java Spring Boot, Solidity, Smart Contracts, MQTT, Docker");
        t1.setMaxStudents(3);
        t1.setTopicType(RegistrationPeriodType.KLTN);
        t1.setStatus(TopicStatus.APPROVED);
        t1.setDepartment(cnpm);
        t1.setPeriod(pKltn);
        t1.setCreatedBy(lec1);
        t1.setAdvisor1(lec1);
        t1.setAdvisor2(lec2);
        t1 = topicRepository.save(t1);

        Topic t2 = new Topic();
        t2.setTopicCode("KLTN26-002");
        t2.setTitle("Ứng dụng Trí tuệ Nhân tạo trong chẩn đoán hình ảnh X-quang phổi");
        t2.setDescription("Xây dựng mô hình Vision Transformer phân loại tổn thương phổi và phát hiện viêm phổi từ ảnh X-quang y tế.");
        t2.setRequirements("Python PyTorch, FastAPI, Spring Boot, TensorFlow");
        t2.setMaxStudents(3);
        t2.setTopicType(RegistrationPeriodType.KLTN);
        t2.setStatus(TopicStatus.APPROVED);
        t2.setDepartment(khmt);
        t2.setPeriod(pKltn);
        t2.setCreatedBy(lec2);
        t2.setAdvisor1(lec2);
        topicRepository.save(t2);

        Topic t3 = new Topic();
        t3.setTopicCode("TLCN26-001");
        t3.setTitle("Hệ sinh thái Quản lý Đồ án tốt nghiệp và Kết nối Tuyển dụng");
        t3.setDescription("Xây dựng web app quản lý toàn diện quy trình làm đồ án sinh viên và hỗ trợ doanh nghiệp tiếp cận sản phẩm đồ án.");
        t3.setRequirements("Spring Boot, Thymeleaf, MySQL, Spring Security");
        t3.setMaxStudents(3);
        t3.setTopicType(RegistrationPeriodType.TLCN);
        t3.setStatus(TopicStatus.APPROVED);
        t3.setDepartment(cnpm);
        t3.setPeriod(pTlcn);
        t3.setCreatedBy(lec1);
        t3.setAdvisor1(lec1);
        topicRepository.save(t3);

        Topic t4 = new Topic();
        t4.setTopicCode("NCKH26-001");
        t4.setTitle("Hệ thống phát hiện xâm nhập mạng (NIDS) sử dụng Machine Learning");
        t4.setDescription("Phân tích gói tin mạng thời gian thực, phát hiện bất thường bằng giải thuật XGBoost và Random Forest.");
        t4.setRequirements("Python Scapy, Spring Boot, Snort, Chart.js");
        t4.setMaxStudents(2);
        t4.setTopicType(RegistrationPeriodType.NCKH);
        t4.setStatus(TopicStatus.APPROVED);
        t4.setDepartment(attt);
        t4.setPeriod(pKltn);
        t4.setCreatedBy(rev1);
        t4.setAdvisor1(rev1);
        topicRepository.save(t4);

        // 6. Student Groups
        StudentGroup g1 = new StudentGroup("GRP-01", "Alpha Dev Team", svHuy, 3, "Nhóm nghiên cứu đề tài Blockchain & IoT");
        g1.setTopic(t1);
        g1.setMemberCount(3);
        g1.setStatus(GroupStatus.APPROVED);
        g1 = groupRepository.save(g1);

        groupMemberRepository.save(new GroupMember(g1, svHuy, MemberRole.LEADER));
        groupMemberRepository.save(new GroupMember(g1, svDo, MemberRole.MEMBER));
        groupMemberRepository.save(new GroupMember(g1, svBaoHuy, MemberRole.MEMBER));

        StudentGroup g2 = new StudentGroup("GRP-02", "Web Programming Crew", svQuangHuy, 3, "Nhóm đăng ký đề tài Quản lý đồ án");
        g2.setMemberCount(2);
        g2.setStatus(GroupStatus.PENDING);
        g2 = groupRepository.save(g2);

        groupMemberRepository.save(new GroupMember(g2, svQuangHuy, MemberRole.LEADER));
        groupMemberRepository.save(new GroupMember(g2, svMinh, MemberRole.MEMBER));

        // 7. Group Invitation
        invitationRepository.save(new GroupInvitation(g2, svQuangHuy, svAnh, "Mời bạn Minh Anh tham gia nhóm làm đề tài Web nhé!"));

        // 8. Topic Registration
        TopicRegistration reg1 = new TopicRegistration(g1, t1, "Nhóm Alpha Dev có kiến thức Spring Boot và Solidity vững, kính gửi thầy duyệt đề tài.");
        reg1.setStatus(GroupStatus.APPROVED);
        reg1.setSupervisorFeedback("Đề tài phù hợp năng lực nhóm. Thầy đồng ý hướng dẫn.");
        reg1.setReviewedAt(LocalDateTime.now().minusDays(5));
        registrationRepository.save(reg1);

        // 9. Progress Report
        ProgressReport r1 = new ProgressReport(g1, svHuy, "Báo cáo Đề cương chi tiết và Đặc tả yêu cầu SRS", "Đề cương",
                "Nhóm đã hoàn thành tài liệu SRS, ERD cơ sở dữ liệu và dựng khung mã nguồn ban đầu của hệ thống.",
                "https://github.com/alphadev/supply-chain-docs/releases/tag/v1.0-srs.pdf", "De_cuong_SRS_AlphaDev.pdf", 100);
        r1.setSupervisorScore(9.2);
        r1.setSupervisorFeedback("Đề cương chuẩn bị rất chu đáo và chi tiết. Tiếp tục triển khai phase 2.");
        r1.setReviewedAt(LocalDateTime.now().minusDays(2));
        reportRepository.save(r1);

        // 10. Council
        Council c1 = new Council("HD-CNPM-01", "Hội đồng Bảo vệ Khóa luận tốt nghiệp CNPM 01", cnpm, pKltn, LocalDate.now().plusDays(60), "08:30", "Phòng A1-402, Tòa nhà Trung tâm");
        c1 = councilRepository.save(c1);

        councilMemberRepository.save(new CouncilMember(c1, head, CouncilRole.CHAIR));
        councilMemberRepository.save(new CouncilMember(c1, lec2, CouncilRole.SECRETARY));
        councilMemberRepository.save(new CouncilMember(c1, rev1, CouncilRole.REVIEWER));

        // 11. Evaluation
        evaluationRepository.save(new Evaluation(c1, t1, rev1, EvaluationType.REVIEWER, 9.0,
                "Đề tài mang tính thời sự cao, sản phẩm chạy ổn định, mã nguồn tổ chức khoa học. Đạt điều kiện ra bảo vệ hội đồng."));
    }
}
