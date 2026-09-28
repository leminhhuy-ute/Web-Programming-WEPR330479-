# HCM-UTE Student Topic Management System
### Hệ thống Quản lý Đề tài Nghiên cứu & Khóa luận Tốt nghiệp Sinh viên

[![Java 21](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.16-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Tests](https://img.shields.io/badge/Tests-54%2F54%20Passed-success.svg)](docs/FINAL_VERIFICATION_REPORT.md)
[![Database](https://img.shields.io/badge/Database-MySQL%208.0%20%7C%20H2-blue.svg)](database/schema.sql)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20Layered-purple.svg)](docs/SYSTEM_ARCHITECTURE.md)

---

## 1. Giới thiệu tổng quan (Project Overview)

**Hệ thống Quản lý Đề tài Sinh viên HCM-UTE** (`vn.edu.hcmute:topic-management`) là nền tảng quản lý học thuật trực tuyến toàn diện được thiết kế và xây dựng dành riêng cho **Khoa Công nghệ Thông tin - Trường Đại học Sư phạm Kỹ thuật TP. Hồ Chí Minh (HCM-UTE)**.

Hệ thống số hóa và tự động hóa toàn bộ quy trình đề tài của sinh viên qua các hình thức:
- **Môn học chuyên ngành (`COURSE`)**
- **Nghiên cứu khoa học sinh viên (`NCKH`)**
- **Tiểu luận chuyên ngành (`TLCN`)**
- **Khóa luận tốt nghiệp (`KLTN`)**

Nền tảng kết nối chặt chẽ bốn nhóm đối tượng: **Ban Chủ nhiệm Khoa (Trưởng khoa)**, **Trưởng Bộ môn**, **Giảng viên (Hướng dẫn / Phản biện / Hội đồng)**, và **Sinh viên**.

---

## 2. Tính năng chính (Key Features)

### 2.1 Quản trị & Điều phối đợt đăng ký (Trưởng khoa - `ROLE_DEAN`)
- **Quản lý người dùng & phân quyền**: Tạo tài khoản, gán bộ môn, khóa/mở khóa tài khoản, reset mật khẩu, giám sát lịch sử đổi mật khẩu (`password_changed_at`).
- **Cấu hình đợt đăng ký hai cửa sổ thời gian**:
  $$\text{GV bắt đầu} < \text{GV kết thúc} < \text{SV bắt đầu} < \text{SV kết thúc}$$
- **Quản lý đề tài liên bộ môn**: Khả năng đề xuất đề tài liên ngành hoặc cấp khoa.
- **Thành lập hội đồng chấm điểm**: Lập hội đồng từ 3 đến 5 giảng viên với đầy đủ chức danh (Chủ tịch, Thư ký, Phản biện, Ủy viên), phân công nhóm và phản biện.
- **Công bố kết quả**: Phê duyệt và công bố bảng điểm đánh giá của hội đồng cho sinh viên.

### 2.2 Đề xuất & Phê duyệt đề tài (Trưởng bộ môn & Giảng viên)
- **Đề xuất đề tài**: Giảng viên đề xuất đề tài theo chuyên ngành trong cửa sổ thời gian quy định.
- **Phê duyệt đề tài**: Trưởng bộ môn kiểm tra tính khả thi, duyệt đề tài và gán 1–2 GVHD. Đề tài bị từ chối sẽ kèm lý do phản hồi rõ ràng.
- **Duyệt nhóm đăng ký**: Đánh giá và chấp thuận các nhóm sinh viên đăng ký đề tài của bộ môn.

### 2.3 Quản lý nhóm & Đăng ký đề tài (Sinh viên - `ROLE_STUDENT`)
- **Quản lý nhóm tự chủ**: Tạo nhóm, sinh mã nhóm duy nhất, gửi lời mời theo MSSV (tối đa 3 sinh viên/nhóm), chuyển quyền nhóm trưởng, rời nhóm hoặc giải tán nhóm trước khi đăng ký đề tài.
- **Phạm vi nhóm theo đợt (`Period-Scoped`)**: Mỗi sinh viên chỉ thuộc đúng một nhóm trong cùng một đợt đăng ký.
- **Đăng ký đề tài**: Nhóm trưởng duyệt danh mục và gửi yêu cầu đăng ký đề tài trực tuyến.
- **Bảo vệ hủy đăng ký**: Nghiêm cấm hủy đăng ký khi nhóm đã nộp báo cáo hoặc đã được lên lịch chấm hội đồng.

### 2.4 Nộp báo cáo & Xác thực tài liệu (Progress Reports)
- **Tiến trình báo cáo 3 giai đoạn tuyến tính**:
  $$\text{Đề cương} \longrightarrow \text{Giữa kỳ} \longrightarrow \text{Cuối kỳ}$$
- **Xác thực cấu trúc tệp an toàn**:
  - Hỗ trợ định dạng `.pdf` và `.docx` dung lượng tối đa 10 MB.
  - Kiểm tra chữ ký byte (`%PDF-`), điểm kết thúc (`%%EOF`), và cấu trúc đối tượng (`obj`) nhằm ngăn chặn tải tệp giả mạo.

### 2.5 Hội đồng đánh giá & Tổng hợp điểm số (Defenses & Councils)
- **Ngăn ngừa xung đột lợi ích (Conflict of Interest)**: GVHD không được tham gia hội đồng chấm đề tài do chính mình hướng dẫn.
- **Đánh giá độc lập**: Từng thành viên hội đồng chấm điểm ($0.00 - 10.00$) và nhập nhận xét chuyên môn độc lập.
- **Tổng hợp tự động**: Chủ tịch hội đồng chỉ được phép tổng hợp khi tất cả thành viên đã hoàn tất chấm điểm; điểm chung cuộc là trung bình cộng làm tròn 2 chữ số thập phân.
- **Truy vấn kết quả đa đợt**: Hỗ trợ sinh viên tra cứu kết quả theo đợt hiện tại hoặc chọn xem lại lịch sử các đợt đã hoàn thành.

---

## 3. Công nghệ sử dụng (Technology Stack)

| Thành phần | Công nghệ / Thư viện | Phiên bản / Chi tiết |
| :--- | :--- | :--- |
| **Ngôn ngữ nền tảng** | Java | OpenJDK 21 LTS |
| **Backend Framework** | Spring Boot | 3.5.16 (Tomcat 10 nhúng) |
| **Bảo mật & Xác thực** | Spring Security | 6.x (BCrypt, CSRF, Session Rotation, Rate Limiting) |
| **Truy xuất dữ liệu (ORM)** | Spring Data JPA / Hibernate | 6.x (Optimistic Locking, Cascading) |
| **Cơ sở dữ liệu Production**| MySQL Server | 8.0+ (InnoDB, UTF-8 Multi-byte `utf8mb4_unicode_ci`) |
| **Cơ sở dữ liệu Demo / Test**| H2 Database | Embedded file & in-memory database |
| **Quản lý Migration** | Flyway | Migrations `V2` đến `V5` |
| **Giao diện người dùng** | Thymeleaf, HTML5, CSS3, JS | Giao diện Responsive theo hệ nhận diện HCM-UTE |
| **Công cụ đóng gói & Build** | Apache Maven | Maven Wrapper (`mvnw`, `mvnw.cmd`) |
| **Kiểm thử tự động** | JUnit 5 Jupiter, Mockito | 54 bài kiểm thử hồi quy tự động (100% Pass) |

---

## 4. Cấu trúc thư mục dự án (Project Structure)

```text
Web-Programming-WEPR330479--main/
├── .github/                         # GitHub Actions workflows & automation
├── .mvn/                            # Maven wrapper configuration
├── config/                          # Configuration templates
├── database/                        # Database scripts & versioning
│   ├── migration/                   # Flyway incremental migration scripts (V2..V5)
│   ├── schema.sql                   # Full baseline MySQL database schema
│   └── seed.sql                     # Demonstration seed data (accounts & topics)
├── docs/                            # Standardized project documentation
│   ├── API_DOCUMENTATION.md         # Full REST API endpoints & payload schemas
│   ├── BUSINESS_WORKFLOW.md         # Business logic state machines & lifecycle rules
│   ├── DATABASE_DESIGN.md           # ERD, database tables, constraints & migrations
│   ├── DEPLOYMENT_GUIDE.md          # Production deployment guide (Nginx, systemd, MySQL)
│   ├── FINAL_VERIFICATION_REPORT.md # Audit verification report, fixes & test metrics
│   ├── MANUAL_TESTING_SCENARIO.md   # Step-by-step end-to-end QA testing guide
│   ├── PROJECT_OVERVIEW.md          # In-depth architectural & business overview
│   └── SYSTEM_ARCHITECTURE.md       # Layered design, filter chain & security design
├── src/
│   ├── main/
│   │   ├── java/topicmanagement/   # Application source code
│   │   │   ├── config/              # Spring Security & Web MVC configurations
│   │   │   ├── controller/          # REST & MVC controllers
│   │   │   ├── council/             # Council, defense & grade evaluation module
│   │   │   ├── dto/                 # Request & Response data transfer objects
│   │   │   ├── entity/              # JPA domain entities
│   │   │   ├── repository/          # Spring Data JPA repositories
│   │   │   ├── security/            # Authentication providers & session filters
│   │   │   ├── service/             # Business domain services
│   │   │   └── student/             # Student group, report & registration module
│   │   └── resources/
│   │       ├── application.properties # Main application configuration
│   │       ├── application-mysql.properties # Production MySQL profile
│   │       ├── static/              # CSS, JS, fonts, brand logos
│   │       └── templates/           # Thymeleaf HTML views (login, admin, lecturer, etc.)
│   └── test/java/topicmanagement/   # Regression & integration test suites (54 tests)
├── .gitignore                       # Git ignore rules (clean repository)
├── mvnw / mvnw.cmd                  # Maven wrapper executables (Linux / Windows)
├── pom.xml                          # Maven project dependencies & build plugins
└── README.md                        # Project release documentation (this file)
```

---

## 5. Ma trận phân quyền (Role & Permissions Matrix)

| Chức năng | Trưởng khoa (`DEAN`) | Trưởng bộ môn (`HEAD_OF_DEPT`) | Giảng viên (`LECTURER`) | Sinh viên (`STUDENT`) |
| :--- | :---: | :---: | :---: | :---: |
| Quản lý người dùng & bộ môn | :white_check_mark: | :x: | :x: | :x: |
| Quản lý đợt đăng ký | :white_check_mark: | :x: | :x: | :x: |
| Đề xuất đề tài | :white_check_mark: (Tất cả BM) | :white_check_mark: (Trong BM) | :white_check_mark: (Trong BM) | :x: |
| Phê duyệt đề tài & gán GVHD | :white_check_mark: | :white_check_mark: | :x: | :x: |
| Tạo nhóm & mời thành viên | :x: | :x: | :x: | :white_check_mark: |
| Đăng ký đề tài | :x: | :x: | :x: | :white_check_mark: (Nhóm trưởng) |
| Duyệt nhóm đăng ký đề tài | :white_check_mark: | :white_check_mark: | :white_check_mark: (GVHD) | :x: |
| Nộp báo cáo tiến độ (PDF/DOCX) | :x: | :x: | :x: | :white_check_mark: (Nhóm trưởng) |
| Thành lập hội đồng & phân công | :white_check_mark: | :x: | :x: | :x: |
| Chấm điểm hội đồng | :white_check_mark:* | :white_check_mark:* | :white_check_mark:* | :x: |
| Tổng hợp điểm hội đồng | :white_check_mark: (Chủ tịch HĐ) | :white_check_mark: (Chủ tịch HĐ) | :white_check_mark: (Chủ tịch HĐ) | :x: |
| Công bố kết quả chính thức | :white_check_mark: | :x: | :x: | :x: |
| Xem kết quả đánh giá | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |

*\* Chỉ khi là thành viên được phân công trong hội đồng chấm và không phải là GVHD của đề tài.*

---

## 6. Tài khoản kiểm thử Demo (Demo Credentials)

Tất cả các tài khoản demo được cấu hình sẵn trong `database/seed.sql` với mật khẩu thống nhất:

> **Mật khẩu chung cho tất cả tài khoản:** `Demo@12345`

| Vai trò | Tên đăng nhập | Họ và tên | Đơn vị / Chức vụ |
| :--- | :--- | :--- | :--- |
| **Trưởng khoa** | `dean01` | PGS. TS. Nguyễn Văn Thành | Toàn quyền Khoa CNTT |
| **Trưởng bộ môn** | `hod01` | TS. Nguyễn Hoàng Long | Trưởng Bộ môn Công nghệ Phần mềm (CNPM) |
| **Giảng viên** | `lecturer01` | TS. Trần Hoàng Nam | Bộ môn Công nghệ Phần mềm (CNPM) |
| **Giảng viên** | `lecturer02` | ThS. Đặng Thị Kim Ngân | Bộ môn Công nghệ Phần mềm (CNPM) |
| **Giảng viên** | `lecturer03` | TS. Lê Văn Tuấn | Bộ môn Hệ thống Thông tin (HTTT) |
| **Giảng viên** | `lecturer04` | ThS. Phạm Ngọc Bích | Bộ môn Khoa học Dữ liệu (KHDL) |
| **Giảng viên** | `lecturer05` | TS. Võ Minh Trí | Bộ môn Mạng máy tính & An ninh mạng (MMT) |
| **Sinh viên** | `student01` | Nguyễn Minh Tuấn | MSSV: `22110001` (CNPM - Nhóm trưởng) |
| **Sinh viên** | `student02` | Trần Thu Hà | MSSV: `22110002` (CNPM - Thành viên) |
| **Sinh viên** | `student03` | Lê Hoàng Nam | MSSV: `22110003` (HTTT) |

---

## 7. Hướng dẫn chạy nhanh (Quick Start with H2)

Ứng dụng hỗ trợ chế độ demo không cần cài đặt MySQL, sử dụng cơ sở dữ liệu H2 tự động khởi tạo.

### 7.1 Yêu cầu môi trường
- **Java**: OpenJDK 21 trở lên (`java -version`).
- **Maven**: Đã tích hợp sẵn Maven Wrapper trong dự án.

### 7.2 Lệnh khởi chạy
Tại thư mục gốc của dự án:

#### Trên Windows (PowerShell / Command Prompt):
```powershell
# Chạy toàn bộ kiểm thử
.\mvnw.cmd clean test

# Khởi chạy ứng dụng
.\mvnw.cmd spring-boot:run
```

#### Trên Linux / macOS:
```bash
# Cấp quyền thực thi và chạy
chmod +x ./mvnw
./mvnw clean test
./mvnw spring-boot:run
```

### 7.3 Truy cập ứng dụng
Sau khi console hiển thị `Started TopicManagementApplication in ... seconds`:
- **Trang chủ / Đăng nhập**: [http://localhost:8080/login.html](http://localhost:8080/login.html)
- Sử dụng bất kỳ tài khoản demo ở bảng trên với mật khẩu `Demo@12345`.
- Nhấn `Ctrl + C` tại cửa sổ dòng lệnh để dừng ứng dụng.

---

## 8. Hướng dẫn chạy với MySQL (Production Profile)

### 8.1 Thiết lập cơ sở dữ liệu MySQL
1. Khởi động MySQL Server (phiên bản 8.0+).
2. Tạo database và nạp schema chuẩn:
   ```bash
   mysql -u root -p
   ```
   Trong MySQL prompt:
   ```sql
   SOURCE database/schema.sql;
   SOURCE database/seed.sql;
   CREATE USER 'topic_app'@'localhost' IDENTIFIED BY 'Demo@12345';
   GRANT ALL PRIVILEGES ON student_topic_management.* TO 'topic_app'@'localhost';
   FLUSH PRIVILEGES;
   EXIT;
   ```

### 8.2 Cấu hình biến môi trường
Thiết lập các biến kết nối trong phiên làm việc:

#### Windows PowerShell:
```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/student_topic_management?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh"
$env:DB_USERNAME = "topic_app"
$env:DB_PASSWORD = "Demo@12345"
$env:SESSION_COOKIE_SECURE = "false" # Đặt true nếu chạy trên HTTPS
```

#### Linux / macOS:
```bash
export DB_URL="jdbc:mysql://localhost:3306/student_topic_management?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh"
export DB_USERNAME="topic_app"
export DB_PASSWORD="Demo@12345"
export SESSION_COOKIE_SECURE="false"
```

### 8.3 Khởi chạy với Profile MySQL
```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mysql
```

---

## 9. Đóng gói & Chạy file JAR độc lập (Standalone Execution)

Đóng gói ứng dụng thành file thực thi duy nhất (Executable Fat JAR):

```powershell
# Đóng gói sản phẩm
.\mvnw.cmd clean package

# Khởi chạy file JAR
java -jar .\target\topic-management-1.0.0.jar
```

Ứng dụng khởi chạy máy chủ nhúng Tomcat trên cổng `8080` mà không cần bất kỳ cài đặt web server bên ngoài nào.

---

## 10. Kiểm thử tự động (Automated Testing)

Toàn bộ quy trình nghiệp vụ và các bản vá an ninh được bảo vệ bởi bộ test hồi quy tự động:

```powershell
.\mvnw.cmd clean test
```

### Kết quả kiểm thử:
```text
[INFO] Results:
[INFO] Tests run: 54, Failures: 0, Errors: 0, Skipped: 0
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

Các kịch bản kiểm thử trọng yếu bao gồm:
1. `testScenario1_RegistrationCancellationLifecycle_BlockedAfterReportOrDefense`: Ngăn hủy đăng ký khi đã có báo cáo hoặc hội đồng.
2. `testScenario2_DefenseReportLinkage_CancelledRegistrationDoesNotLeak`: Đảm bảo không rò rỉ dữ liệu của đăng ký đã hủy.
3. `testScenario3_GroupMembershipScopedPerRegistrationPeriod`: Giới hạn một sinh viên chỉ thuộc một nhóm trong một đợt.
4. `testScenario4_TopicApprovalRejectsInactiveCreatorOrInvalidSupervisor`: Từ chối duyệt đề tài nếu người tạo không hoạt động hoặc GVHD sai bộ môn.
5. `testScenario5_CaseInsensitiveUsernameAndEmailUniqueness`: Đảm bảo tính duy nhất không phân biệt hoa thường của username/email.
6. `testScenario6_PasswordChangeInvalidatesSessionViaAccountRefreshFilter`: Tự động hủy phiên đăng nhập khi mật khẩu bị thay đổi.
7. `testScenario7_ReportSubmissionEnforcesStageProgressionAndPdfStructure`: Kiểm tra tuần tự nộp báo cáo và tính toàn vẹn cấu trúc file PDF.
8. `testScenario8_RegistrationPeriodUpdateForbidsChangingTypeWhenTopicsExist`: Ngăn sửa đổi loại đợt khi đã có đề tài phát sinh.
9. `testScenario9_CouncilCreationRejectsNullMemberWithBadRequest`: Bắt lỗi dữ liệu thành viên hội đồng rỗng.
10. `testScenario10_DeanCrossDepartmentTopic_CouncilCrud_GroupOperations`: Kiểm tra phân quyền Trưởng khoa liên bộ môn, CRUD hội đồng, và thao tác nhóm.
11. `testScenario11_MultiPeriodStudentResultSelection`: Ưu tiên đợt đang hoạt động và hỗ trợ tra cứu chính xác kết quả các đợt lịch sử.

---

## 11. Mục lục tài liệu kỹ thuật chi tiết (Documentation Index)

Chi tiết triển khai, thiết kế và hướng dẫn được lưu trữ trong thư mục [`docs/`](docs/):

- 📘 **[Tổng quan dự án (Project Overview)](docs/PROJECT_OVERVIEW.md)**: Giới thiệu mục tiêu, đối tượng sử dụng và phạm vi giải pháp.
- 🏛️ **[Kiến trúc hệ thống (System Architecture)](docs/SYSTEM_ARCHITECTURE.md)**: Mô hình kiến trúc phân tầng, luồng xử lý và cơ chế bảo mật.
- 🗄️ **[Thiết kế Cơ sở dữ liệu (Database Design)](docs/DATABASE_DESIGN.md)**: Sơ đồ ERD chi tiết, mô tả cấu trúc bảng, ràng buộc toàn vẹn và migration.
- 🔄 **[Quy trình nghiệp vụ (Business Workflow)](docs/BUSINESS_WORKFLOW.md)**: Chi tiết vòng đời đợt đăng ký, duyệt đề tài, nộp báo cáo và chấm điểm hội đồng.
- 🔌 **[Tài liệu REST API (API Documentation)](docs/API_DOCUMENTATION.md)**: Danh mục đầy đủ các REST endpoints, payload mẫu và mã lỗi HTTP.
- 🧪 **[Kịch bản kiểm thử thủ công (Manual Testing Scenario)](docs/MANUAL_TESTING_SCENARIO.md)**: Cẩm nang hướng dẫn kiểm thử thủ công từng bước từ đăng nhập đến công bố kết quả.
- 📋 **[Báo cáo nghiệm thu & xác minh (Final Verification Report)](docs/FINAL_VERIFICATION_REPORT.md)**: Tổng kết danh sách lỗi đã khắc phục, thay đổi schema và số liệu kiểm thử.
- 🚀 **[Hướng dẫn triển khai Production (Deployment Guide)](docs/DEPLOYMENT_GUIDE.md)**: Hướng dẫn cấu hình Linux systemd, reverse proxy Nginx với HTTPS và sao lưu MySQL.

---

## 12. Lưu ý vận hành & An toàn bảo mật

1. **Mật khẩu kiểm thử**: Hệ thống lưu giữ mật khẩu `Demo@12345` phục vụ mục đích chấm điểm và đánh giá đề tài học thuật. Khi đưa vào môi trường Production thực tế, quản trị viên cần chạy script reset mật khẩu và đổi khóa bảo mật.
2. **Bảo vệ Cookie**: Mặc định chế độ HTTP demo tắt `SESSION_COOKIE_SECURE`. Khi chạy qua Reverse Proxy với SSL/TLS, bắt buộc bật `SESSION_COOKIE_SECURE=true`.
3. **Giới hạn số lần đăng nhập sai**: Hệ thống giới hạn 5 lần đăng nhập thất bại trong 15 phút trên mỗi IP/Username (`LoginAttemptService`) để phòng chống tấn công brute-force.
