package topicmanagement.config;

import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import topicmanagement.entity.*;
import topicmanagement.enums.*;
import topicmanagement.repository.*;
import topicmanagement.council.*;

/** Persistent academic demo data runner for local / testing profiles. */
@Component
@Profile("demo")
public class DemoData implements CommandLineRunner {
    private final UserRepository users;
    private final DepartmentRepository departments;
    private final RegistrationPeriodRepository periods;
    private final TopicRepository topics;
    private final StudentGroupRepository groups;
    private final GroupMemberRepository groupMembers;
    private final CouncilRepository councils;
    private final AnnouncementRepository announcements;
    private final PasswordEncoder passwords;

    public DemoData(UserRepository users, DepartmentRepository departments,
            RegistrationPeriodRepository periods, TopicRepository topics,
            StudentGroupRepository groups, GroupMemberRepository groupMembers,
            CouncilRepository councils, AnnouncementRepository announcements,
            PasswordEncoder passwords) {
        this.users = users;
        this.departments = departments;
        this.periods = periods;
        this.topics = topics;
        this.groups = groups;
        this.groupMembers = groupMembers;
        this.councils = councils;
        this.announcements = announcements;
        this.passwords = passwords;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (users.count() > 0) return;

        // 1. Departments
        Department cnpm = createDept("CNPM", "Công nghệ phần mềm", "Bộ môn Kỹ thuật phần mềm & hệ thống thông minh");
        Department httt = createDept("HTTT", "Hệ thống thông tin", "Bộ môn Cơ sở dữ liệu và hệ thống thông tin quản lý");
        Department khdl = createDept("KHDL", "Khoa học dữ liệu", "Bộ môn Trí tuệ nhân tạo và Khoa học dữ liệu");
        Department mmt = createDept("MMT", "Mạng máy tính và truyền thông", "Bộ môn Mạng máy tính và An toàn thông tin");

        // 2. Users
        User dean = account("dean01", "DEAN01", "PGS. TS. Nguyễn Văn Thành", "dean01@dean.hcmute.edu.vn", Role.DEAN, null);
        User hod = account("hod01", "HOD01", "TS. Nguyễn Hoàng Long", "hod01@headdepartment.hcmute.edu.vn", Role.HEAD_OF_DEPT, cnpm);
        User lec1 = account("lecturer01", "GV001", "TS. Trần Hoàng Nam", "lecturer01@lecturer.hcmute.edu.vn", Role.LECTURER, cnpm);
        User lec2 = account("lecturer02", "GV002", "ThS. Đặng Thị Kim Ngân", "lecturer02@lecturer.hcmute.edu.vn", Role.LECTURER, cnpm);
        User lec3 = account("lecturer03", "GV003", "TS. Lê Văn Tuấn", "lecturer03@lecturer.hcmute.edu.vn", Role.LECTURER, httt);
        User lec4 = account("lecturer04", "GV004", "ThS. Phạm Ngọc Bích", "lecturer04@lecturer.hcmute.edu.vn", Role.LECTURER, khdl);
        User lec5 = account("lecturer05", "GV005", "TS. Võ Minh Trí", "lecturer05@lecturer.hcmute.edu.vn", Role.LECTURER, mmt);

        User stu1 = account("student01", "22110001", "Nguyễn Minh Tuấn", "student01@student.hcmute.edu.vn", Role.STUDENT, cnpm);
        User stu2 = account("student02", "22110002", "Trần Thu Hà", "student02@student.hcmute.edu.vn", Role.STUDENT, cnpm);
        User stu3 = account("student03", "22110003", "Lê Hoàng Nam", "student03@student.hcmute.edu.vn", Role.STUDENT, httt);
        User stu4 = account("student04", "22110004", "Phạm Ngọc Anh", "student04@student.hcmute.edu.vn", Role.STUDENT, httt);
        User stu5 = account("student05", "22110005", "Đỗ Quốc Bảo", "student05@student.hcmute.edu.vn", Role.STUDENT, mmt);
        User stu6 = account("student06", "22110006", "Vũ Mai Phương", "student06@student.hcmute.edu.vn", Role.STUDENT, mmt);

        // 3. Registration Periods
        LocalDateTime now = LocalDateTime.now();
        RegistrationPeriod p1 = new RegistrationPeriod();
        p1.setName("Đợt đề xuất đề tài môn học Học kỳ I năm học 2026-2027");
        p1.setType(RegistrationPeriodType.COURSE);
        p1.setLecturerStartAt(now.minusDays(7));
        p1.setLecturerEndAt(now.plusDays(7));
        p1.setStudentStartAt(now.plusDays(8));
        p1.setStudentEndAt(now.plusDays(21));
        periods.save(p1);

        RegistrationPeriod p2 = new RegistrationPeriod();
        p2.setName("Đợt đăng ký đề tài sinh viên Học kỳ I năm học 2026-2027");
        p2.setType(RegistrationPeriodType.COURSE);
        p2.setLecturerStartAt(now.minusDays(21));
        p2.setLecturerEndAt(now.minusDays(8));
        p2.setStudentStartAt(now.minusDays(7));
        p2.setStudentEndAt(now.plusDays(14));
        periods.save(p2);

        RegistrationPeriod p3 = new RegistrationPeriod();
        p3.setName("Đợt bảo vệ Khóa luận tốt nghiệp năm học 2026-2027");
        p3.setType(RegistrationPeriodType.KLTN);
        p3.setLecturerStartAt(now.minusDays(30));
        p3.setLecturerEndAt(now.minusDays(15));
        p3.setStudentStartAt(now.minusDays(14));
        p3.setStudentEndAt(now.minusDays(2));
        p3.setReviewDeadline(now.plusDays(12));
        p3.setDefenseDate(now.toLocalDate().plusDays(15));
        periods.save(p3);

        // 4. Topics
        Topic t1 = createTopic("DT-CNPM-202601",
                "Xây dựng hệ thống quản lý đề tài và học vụ tích hợp",
                "Xây dựng ứng dụng Spring Boot & web hiện đại quản lý vòng đời đề tài và phân công hội đồng trong khoa CNTT.",
                "Java 21, Spring Boot, Spring Data JPA, H2/MySQL, HTML, CSS, JavaScript",
                3, RegistrationPeriodType.COURSE, TopicStatus.APPROVED, cnpm, p1, lec1, lec1);

        Topic t2 = createTopic("DT-CNPM-202602",
                "Xây dựng cổng thông tin và hoạt động ngoại khóa dành cho sinh viên HCMUTE",
                "Thiết kế và triển khai cổng thông tin điện tử kết nối hoạt động Đoàn - Hội và nghiên cứu khoa học sinh viên.",
                "Spring Boot, REST API, Responsive Web Design, Bootstrap / Vanilla CSS",
                3, RegistrationPeriodType.COURSE, TopicStatus.APPROVED, cnpm, p2, lec1, lec1);

        Topic t3 = createTopic("DT-CNPM-202603",
                "Nghiên cứu ứng dụng Deep Learning trong nhận diện và phân loại bệnh lý mắt",
                "Sử dụng mạng nơ-ron tích chập (CNN) và Vision Transformer để hỗ trợ chẩn đoán hình ảnh nhãn khoa.",
                "Python, PyTorch, FastAPI, JavaScript, kiến thức nền tảng về Deep Learning",
                2, RegistrationPeriodType.COURSE, TopicStatus.PENDING, cnpm, p1, lec2, null);

        Topic t4 = createTopic("DT-HTTT-202601",
                "Xây dựng hệ thống hoạch định nguồn lực doanh nghiệp (ERP) cho chuỗi bán lẻ",
                "Ứng dụng kiến trúc Microservices trong quản lý kho hàng, điểm bán lẻ và báo cáo kinh doanh thông minh (BI).",
                "Java, Spring Cloud, PostgreSQL / MySQL, Docker, React / HTML5",
                3, RegistrationPeriodType.COURSE, TopicStatus.APPROVED, httt, p1, lec3, lec3);

        Topic t5 = createTopic("DT-KHDL-202601",
                "Phân tích cảm xúc và dự báo xu hướng dư luận mạng xã hội bằng mô hình ngôn ngữ lớn (LLM)",
                "Thu thập dữ liệu tiếng Việt đa chiều, tinh chỉnh mô hình nền tảng (LLM fine-tuning) để phân loại cảm xúc tự động.",
                "Python, Transformers, NLP tiếng Việt, LangChain, Web Dashboard",
                2, RegistrationPeriodType.COURSE, TopicStatus.APPROVED, khdl, p1, lec4, lec4);

        Topic t6 = createTopic("DT-MMT-202601",
                "Hệ thống phát hiện xâm nhập mạng (NIDS) thông minh ứng dụng Machine Learning",
                "Phân tích lưu lượng gói tin mạng trong thời gian thực nhằm phát hiện các cuộc tấn công DDoS, quét cổng và mã độc.",
                "Python, C++, Network Security, Wireshark, Scikit-learn",
                2, RegistrationPeriodType.COURSE, TopicStatus.APPROVED, mmt, p1, lec5, lec5);

        // 5. Student Groups
        StudentGroup g1 = new StudentGroup();
        g1.setGroupCode("GRP-202601");
        g1.setGroupName("Nhóm Nghiên cứu Kỹ thuật Phần mềm UTE");
        g1.setTopic(t2);
        g1.setPeriod(p2);
        g1.setLeader(stu1);
        g1.setMemberCount(2);
        g1.setStatus(GroupStatus.APPROVED);
        g1.setRegisteredAt(now.minusDays(5));
        groups.save(g1);

        GroupMember gm1 = new GroupMember(g1, stu1, "LEADER");
        GroupMember gm2 = new GroupMember(g1, stu2, "MEMBER");
        groupMembers.save(gm1);
        groupMembers.save(gm2);

        // 6. Councils & Members
        Council council = new Council("HD-CNTT-01",
                "Hội đồng Đánh giá Khóa luận Tốt nghiệp CNTT 01",
                now.plusDays(15), "A1-302");
        council.setStatus(CouncilStatus.READY);
        councils.save(council);

        council.getMembers().add(new CouncilMember(council, dean, CouncilRole.CHAIRPERSON));
        council.getMembers().add(new CouncilMember(council, hod, CouncilRole.SECRETARY));
        council.getMembers().add(new CouncilMember(council, lec5, CouncilRole.REVIEWER));
        council.getMembers().add(new CouncilMember(council, lec4, CouncilRole.MEMBER));
        council.getMembers().add(new CouncilMember(council, lec3, CouncilRole.MEMBER));
        councils.save(council);

        // 7. Announcements
        createAnnouncement("Kế hoạch đề xuất đề tài học vụ năm học 2026-2027",
                "Kính gửi quý Thầy Cô và các bạn Sinh viên, Khoa CNTT chính thức mở cổng tiếp nhận đề xuất và đăng ký đề tài cho năm học mới.",
                AnnouncementAudience.ALL, dean);
        createAnnouncement("Hướng dẫn dành cho giảng viên",
                "Quý Thầy Cô vui lòng kiểm tra đợt đăng ký và cơ cấu bộ môn trước khi gửi đề xuất đề tài. Mỗi giảng viên hướng dẫn không vượt quá định mức tối đa.",
                AnnouncementAudience.LECTURER, dean);
        createAnnouncement("Hướng dẫn dành cho sinh viên",
                "Nhóm trưởng đại diện gửi đăng ký đề tài, theo dõi kết quả duyệt từ GVHD và nộp báo cáo tiến độ đúng thời hạn quy định.",
                AnnouncementAudience.STUDENT, dean);
    }

    private Department createDept(String code, String name, String desc) {
        Department d = new Department();
        d.setCode(code);
        d.setName(name);
        d.setDescription(desc);
        return departments.save(d);
    }

    private User account(String username, String code, String name, String email, Role role, Department department) {
        User u = new User();
        u.setUserCode(code);
        u.setUsername(username);
        u.setEmail(email);
        u.setFullName(name);
        u.setRole(role);
        u.setStatus(UserStatus.ACTIVE);
        u.setDepartment(department);
        u.setPasswordHash(passwords.encode("Demo@12345"));
        return users.save(u);
    }

    private Topic createTopic(String code, String title, String desc, String req,
            int maxStudents, RegistrationPeriodType type, TopicStatus status,
            Department dept, RegistrationPeriod period, User createdBy, User adv1) {
        Topic t = new Topic();
        t.setTopicCode(code);
        t.setTitle(title);
        t.setDescription(desc);
        t.setRequirements(req);
        t.setMaxStudents(maxStudents);
        t.setTopicType(type);
        t.setStatus(status);
        t.setDepartment(dept);
        t.setPeriod(period);
        t.setCreatedBy(createdBy);
        t.setAdvisor1(adv1);
        return topics.save(t);
    }

    private void createAnnouncement(String title, String content, AnnouncementAudience aud, User author) {
        Announcement a = new Announcement();
        a.setTitle(title);
        a.setContent(content);
        a.setAudience(aud);
        a.setCreatedBy(author);
        announcements.save(a);
    }
}
