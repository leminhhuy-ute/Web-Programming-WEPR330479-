# Hệ thống quản lý đề tài sinh viên HCM-UTE

## Giới thiệu

Dự án môn học **Lập trình Web (Web Programming - WEPR330479)** của Khoa Công nghệ Thông tin — Trường Đại học Công nghệ Kỹ thuật Thành phố Hồ Chí Minh (HCM-UTE).

Hệ thống nhằm số hóa và tự động hóa toàn bộ vòng đời quản lý đề tài học thuật của sinh viên qua các hình thức:
- **Môn học chuyên ngành (`COURSE`)**
- **Nghiên cứu khoa học sinh viên (`NCKH`)**
- **Tiểu luận chuyên ngành (`TLCN`)**
- **Khóa luận tốt nghiệp (`KLTN`)**

### Vòng đời quy trình đề tài:

$$\text{Tạo đợt đăng ký} \longrightarrow \text{Giảng viên đề xuất đề tài} \longrightarrow \text{Duyệt và công bố đề tài} \longrightarrow \text{Sinh viên tạo nhóm}$$
$$\longrightarrow \text{Đăng ký đề tài} \longrightarrow \text{Hướng dẫn} \longrightarrow \text{Nộp báo cáo} \longrightarrow \text{Thành lập hội đồng} \longrightarrow \text{Chấm điểm} \longrightarrow \text{Công bố kết quả}$$

---

## Chức năng chính

### Trưởng khoa (`ROLE_DEAN`)
- **Quản lý tài khoản**: Khởi tạo tài khoản người dùng, phân quyền vai trò, gán bộ môn chuyên môn, khóa/mở khóa tài khoản, đặt lại mật khẩu và giám sát bảo mật phiên đăng nhập.
- **Quản lý bộ môn**: Quản lý danh mục các bộ môn chuyên ngành trong Khoa CNTT.
- **Tạo đợt đăng ký**: Cấu hình các đợt đăng ký với 4 mốc thời gian tuần tự (cửa sổ giảng viên đề xuất và cửa sổ sinh viên đăng ký).
- **Quản lý hội đồng**: Thành lập hội đồng đánh giá (3–5 thành viên với các chức danh Chủ tịch, Thư ký, Phản biện, Ủy viên), phân công nhóm bảo vệ và chỉ định giảng viên phản biện.
- **Công bố kết quả**: Phê duyệt và công bố bảng điểm đánh giá của hội đồng cho sinh viên tra cứu.

### Trưởng bộ môn / Giảng viên (`ROLE_HEAD_OF_DEPT` / `ROLE_LECTURER`)
- **Đề xuất đề tài**: Giảng viên đề xuất đề tài thuộc bộ môn trong cửa sổ thời gian cho phép; Trưởng khoa có quyền đề xuất đề tài liên bộ môn.
- **Quản lý đề tài**: Trưởng bộ môn thẩm định, phê duyệt đề tài, phân công giảng viên hướng dẫn chính/phụ (`advisor1`, `advisor2`), hoặc từ chối đề tài kèm lý do phản hồi.
- **Hướng dẫn sinh viên**: Giảng viên hướng dẫn duyệt các nhóm sinh viên đăng ký đề tài của mình, theo dõi tiến độ và tải tệp báo cáo của nhóm.
- **Tham gia đánh giá**: Tham gia hội đồng chấm bảo vệ, nhập điểm số độc lập ($0.00 - 10.00$) và nhận xét chuyên môn (với điều kiện không hướng dẫn đề tài đó).

### Sinh viên (`ROLE_STUDENT`)
- **Tạo nhóm tối đa 3 thành viên**: Khởi tạo nhóm sinh viên mới, người tạo tự động là Nhóm trưởng. Mỗi sinh viên chỉ được thuộc đúng 1 nhóm trong một đợt đăng ký.
- **Mời thành viên**: Nhóm trưởng gửi lời mời tham gia qua Mã số sinh viên (MSSV); thành viên nhận lời mời có quyền Đồng ý hoặc Từ chối.
- **Chọn nhóm trưởng**: Nhóm trưởng có quyền chuyển giao vai trò trưởng nhóm cho thành viên khác trong nhóm.
- **Rời nhóm & Giải tán nhóm**: Thành viên có thể rời nhóm hoặc nhóm trưởng giải tán nhóm khi nhóm chưa đăng ký đề tài.
- **Đăng ký đề tài**: Nhóm trưởng đại diện nhóm chọn đề tài đã công bố trong danh mục để đăng ký. Nghiêm cấm hủy đăng ký khi nhóm đã nộp báo cáo hoặc đã có lịch bảo vệ.
- **Nộp báo cáo**: Nhóm trưởng nộp báo cáo tiến độ tuần tự theo 3 giai đoạn: **Đề cương** $\rightarrow$ **Giữa kỳ** $\rightarrow$ **Cuối kỳ** (hỗ trợ tệp PDF/DOCX tối đa 10 MB, kiểm tra tính hợp lệ cấu trúc tệp).
- **Xem kết quả**: Tra cứu bảng điểm tổng hợp và nhận xét chi tiết của từng thành viên hội đồng sau khi kết quả được công bố. Hỗ trợ tra cứu theo đợt hiện tại hoặc xem lại lịch sử các đợt đã qua.

### Mở rộng kết quả và thông báo
- **Chọn đợt rõ ràng** khi tạo nhóm; chặn đăng ký đề tài khác đợt và kiểm tra một sinh viên không có hai nhóm trong cùng đợt thực hiện đề tài.
- **Sửa/xóa hội đồng trên giao diện** cho trưởng khoa; khi sửa kiểm tra cả GVHD thứ nhất/thứ hai, phản biện đã phân công và ngày bảo vệ.
- **Email công bố điểm**: hàng đợi lưu trong CSDL, thử lại khi SMTP lỗi, trang theo dõi và gửi lại thư thất bại. Mặc định tắt gửi, bật qua biến môi trường. Xem [cấu hình SMTP/Brevo](docs/SMTP_SETUP.md).
- **Xuất Excel/PDF**: trưởng khoa xuất kết quả đã công bố theo đợt; sinh viên tải PDF kết quả của chính mình. PDF nhúng font tiếng Việt, tự xuống dòng và phân trang.

### Hội đồng (`Council & Defense`)
- **Phân công thành viên**: Cơ cấu hội đồng từ 3 đến 5 giảng viên (đúng 1 Chủ tịch, 1 Thư ký, ít nhất 1 Phản biện và các Ủy viên), tuyệt đối không bao gồm giảng viên hướng dẫn của đề tài để tránh xung đột lợi ích.
- **Nhập điểm**: Từng thành viên hội đồng nhập điểm độc lập kèm nhận xét chuyên môn.
- **Tổng hợp kết quả**: Chủ tịch hội đồng tổng hợp điểm trung bình cộng làm tròn 2 chữ số thập phân sau khi tất cả thành viên đã hoàn tất việc chấm điểm.

---

## Thành viên

| MSSV | Họ và tên |
| --- | --- |
| 24110019 | Lê Minh Huy |
| 24110012 | Trần Hữu Thành Đô |
| 24110033 | Lê Đăng Minh |
| 24162043 | Lê Bảo Huy |
| 24162048 | Nguyễn Quang Huy |

---

## Công nghệ áp dụng

| Thành phần / Công nghệ | Phiên bản / Thư viện | Mô tả chi tiết chức năng |
| :--- | :--- | :--- |
| **Ngôn ngữ nền tảng** | Java 21 LTS | Nền tảng thực thi chính, tận dụng các tính năng mới của Java hiện đại |
| **Khung ứng dụng (Framework)** | Spring Boot 3.5.16 | Khung ứng dụng chính, quản lý vòng đời và tiêm phụ thuộc (Dependency Injection), tích hợp máy chủ Tomcat 10 nhúng |
| **Tầng điều hướng (Controller)** | Spring MVC & REST | Xử lý điều hướng giao diện HTML và cung cấp hệ thống API RESTful trao đổi dữ liệu JSON |
| **Tầng nghiệp vụ (Service)** | Spring Service Layer | Đóng gói toàn bộ quy tắc nghiệp vụ, máy trạng thái đào tạo, kiểm soát hạn chót và xử lý ngoại lệ tập trung |
| **Tầng dữ liệu (Repository)** | Spring Data JPA / Hibernate | Tầng truy xuất dữ liệu, ánh xạ đối tượng quan hệ (ORM), quản lý khóa lạc quan (`@Version`) và giao dịch cơ sở dữ liệu (`@Transactional`) |
| **Bảo mật & Phân quyền (Security)** | Spring Security 6 | Xác thực người dùng, băm mật khẩu BCrypt, phòng chống tấn công CSRF, kiểm soát phiên, chống brute-force và phân quyền truy cập theo vai trò (RBAC) |
| **Cơ sở dữ liệu Production** | MySQL Server 8.0+ | Hệ quản trị cơ sở dữ liệu quan hệ chính thức, lưu trữ bền vững với Storage Engine InnoDB và bảng mã `utf8mb4_unicode_ci` |
| **Cơ sở dữ liệu Demo & Test** | H2 Database | Cơ sở dữ liệu nhúng (file-based và in-memory), phục vụ khởi chạy demo tức thì và chạy bộ kiểm thử tự động |
| **Quản lý Migration Schema** | Flyway | Quản lý lịch sử và tự động áp dụng các tập lệnh nâng cấp cấu trúc cơ sở dữ liệu V6 ở profile MySQL; baseline V5 trên schema hiện hành, không chạy lại các SQL V2–V5 cũ |
| **Giao diện người dùng (Frontend)** | Thymeleaf, HTML5, CSS3, JS | Giao diện web responsive, kết nối Fetch API, chuẩn nhận diện màu sắc và kiểu chữ của Trường ĐH Sư phạm Kỹ thuật TP.HCM |
| **Công cụ đóng gói (Build Tool)** | Apache Maven 3.9+ | Quản lý thư viện phụ thuộc và đóng gói toàn bộ dự án thành tệp thực thi duy nhất (Executable Fat JAR) |

---

## Cấu trúc dự án

```text
Web-Programming-WEPR330479--main/
├── .github/                         # Quy trình tự động hóa GitHub Actions
├── .mvn/                            # Cấu hình bộ nạp Maven Wrapper
├── database/                        # Cơ sở dữ liệu và kịch bản nâng cấp
│   ├── migration/                   # SQL lịch sử V2..V5 và bản V6 để tra cứu
│   ├── schema.sql                   # Cấu trúc cơ sở dữ liệu chuẩn đầy đủ cho MySQL
│   └── seed.sql                     # Dữ liệu mẫu (tài khoản demo, bộ môn, đề tài mẫu)
├── docs/                            # Toàn bộ tài liệu kỹ thuật hệ thống (Tiếng Việt)
│   ├── API_DOCUMENTATION.md         # Danh mục chi tiết các endpoint REST API và mã lỗi
│   ├── BUSINESS_WORKFLOW.md         # Quy trình nghiệp vụ chi tiết và máy trạng thái
│   ├── DATABASE_DESIGN.md           # Thiết kế cơ sở dữ liệu, sơ đồ ERD và ràng buộc
│   ├── DEPLOYMENT_GUIDE.md          # Hướng dẫn triển khai Production (Linux, Nginx SSL, MySQL)
│   ├── FINAL_VERIFICATION_REPORT.md # Báo cáo nghiệm thu, kiểm thử hồi quy 54/54 test
│   ├── MANUAL_TESTING_SCENARIO.md   # Cẩm nang kịch bản kiểm thử thủ công từng bước
│   ├── SYSTEM_ARCHITECTURE.md       # Kiến trúc phân tầng, chuỗi lọc bảo mật và xử lý tệp
│   └── SYSTEM_OVERVIEW.md           # Tổng quan hệ thống, mục tiêu và các nhóm người dùng
├── src/
│   ├── main/
│   │   ├── java/topicmanagement/   # Mã nguồn ứng dụng Java
│   │   │   ├── config/              # Cấu hình hệ thống, chuỗi lọc bảo mật và Web MVC
│   │   │   ├── controller/          # Xử lý request MVC và REST API
│   │   │   ├── council/             # Quản lý hội đồng, lịch bảo vệ và chấm điểm
│   │   │   ├── dto/                 # Các đối tượng truyền tải dữ liệu yêu cầu và phản hồi
│   │   │   ├── export/              # Xuất bảng điểm Excel và PDF tiếng Việt
│   │   │   ├── notification/        # Hàng đợi email, gửi SMTP và thử lại
│   │   │   ├── entity/              # Các thực thể dữ liệu ánh xạ bảng cơ sở dữ liệu
│   │   │   ├── enums/               # Các kiểu liệt kê vai trò, trạng thái và loại đợt
│   │   │   ├── repository/          # Tầng giao tiếp cơ sở dữ liệu Spring Data JPA
│   │   │   ├── security/            # Xác thực đăng nhập và phân quyền (Security Provider, Filter)
│   │   │   ├── service/             # Tầng dịch vụ xử lý logic nghiệp vụ cốt lõi
│   │   │   └── student/             # Quản lý nhóm sinh viên, đăng ký đề tài và nộp báo cáo
│   │   └── resources/
│   │       ├── application.properties # Cấu hình ứng dụng mặc định (H2 demo)
│   │       ├── application-mysql.properties # Cấu hình ứng dụng với MySQL Production
│   │       ├── static/              # Tệp tĩnh: CSS, JavaScript, hình ảnh, phông chữ
│   │       └── templates/           # Giao diện HTML Thymeleaf (đăng nhập, quản trị, sinh viên)
│   └── test/java/topicmanagement/   # 80 kiểm thử thường lệ + 1 kiểm tra MySQL trên CI
├── .gitignore                       # Danh sách tệp loại trừ không đưa lên Git
├── mvnw / mvnw.cmd                  # Công cụ thực thi Maven Wrapper (Linux / Windows)
├── pom.xml                          # Khai báo phụ thuộc và cấu hình build Maven
└── README.md                        # Tài liệu hướng dẫn chính của dự án (Tệp này)
```

---

## Tài khoản demo

Sử dụng các tài khoản demo được cung cấp để kiểm thử các vai trò khác nhau trong hệ thống (các tài khoản này được thiết lập sẵn trong `database/seed.sql` nhằm phục vụ mục đích đánh giá và kiểm thử học vụ):

> **Mật khẩu chung cho tất cả tài khoản demo:** `Demo@12345`

| Tài khoản | Vai trò | Mục đích kiểm thử |
| :--- | :--- | :--- |
| `dean01` | **Trưởng khoa** (`ROLE_DEAN`) | Quản trị toàn khoa, tạo đợt đăng ký, đề xuất đề tài liên bộ môn, lập hội đồng, phân công phản biện, công bố điểm |
| `hod01` | **Trưởng bộ môn** (`ROLE_HEAD_OF_DEPT`) | Duyệt đề tài bộ môn CNPM, phân công GVHD, duyệt nhóm sinh viên đăng ký đề tài |
| `lecturer01` | **Giảng viên** (`ROLE_LECTURER`) | Giảng viên bộ môn CNPM, đề xuất đề tài, duyệt nhóm, hướng dẫn đề tài demo, chấm điểm hội đồng |
| `lecturer02` | **Giảng viên** (`ROLE_LECTURER`) | Giảng viên bộ môn CNPM, tham gia phản biện và chấm điểm hội đồng |
| `lecturer03` | **Giảng viên** (`ROLE_LECTURER`) | Giảng viên bộ môn HTTT, tham gia hội đồng chấm điểm bảo vệ |
| `lecturer04` | **Giảng viên** (`ROLE_LECTURER`) | Giảng viên bộ môn KHDL, tham gia hội đồng chấm điểm bảo vệ |
| `lecturer05` | **Giảng viên** (`ROLE_LECTURER`) | Giảng viên bộ môn MMT, kiểm thử phân quyền và khả năng tham gia hội đồng |
| `student01` | **Sinh viên** (`ROLE_STUDENT`) | Nhóm trưởng nhóm demo `GRP-202601`, kiểm thử nộp báo cáo tiến độ và xem bảng điểm kết quả |
| `student02` | **Sinh viên** (`ROLE_STUDENT`) | Thành viên nhóm `GRP-202601`, kiểm thử phân quyền thành viên thường (không thể nộp báo cáo hay đăng ký thay nhóm trưởng) |
| `student03` | **Sinh viên** (`ROLE_STUDENT`) | Sinh viên tự do (chưa có nhóm), kiểm thử quy trình tạo nhóm mới từ đầu, mời thành viên |
| `student04` | **Sinh viên** (`ROLE_STUDENT`) | Sinh viên tự do, kiểm thử quy trình nhận lời mời và chấp nhận tham gia nhóm |
| `student05` | **Sinh viên** (`ROLE_STUDENT`) | Sinh viên tự do, kiểm thử mời thành viên thứ 3 |
| `student06` | **Sinh viên** (`ROLE_STUDENT`) | Sinh viên tự do, kiểm thử từ chối lời mời hoặc nhóm vượt quá giới hạn 3 người |

*Ghi chú quan trọng: Các tài khoản demo tồn tại phục vụ mục đích kiểm thử và đánh giá học thuật. Người dùng sử dụng các tài khoản được cung cấp ở trên và nhập thông tin qua biểu mẫu đăng nhập tiêu chuẩn (giao diện không sử dụng các nút điền nhanh tài khoản để đảm bảo tính an toàn).*

---

## Hướng dẫn chạy dự án

### Yêu cầu môi trường
- **Java Development Kit (JDK)**: Phiên bản OpenJDK 21 trở lên (`java -version`).
- **Apache Maven**: Đã tích hợp sẵn Maven Wrapper trong dự án (không cần cài thêm).

### Khởi chạy nhanh (Chế độ Demo H2)
Tại thư mục gốc của dự án:

#### Trên Windows (PowerShell / Command Prompt):
```powershell
# Chạy kiểm thử tự động
.\mvnw.cmd clean test

# Khởi chạy ứng dụng
.\mvnw.cmd spring-boot:run
```

#### Trên Linux / macOS:
```bash
chmod +x ./mvnw
./mvnw clean test
./mvnw spring-boot:run
```

### Đóng gói và chạy file JAR độc lập:
```powershell
.\mvnw.cmd clean package
java -jar .\target\topic-management-1.0.0.jar
```

### Truy cập hệ thống:
Sau khi ứng dụng khởi chạy thành công:
- Truy cập trang chủ / đăng nhập: **[http://localhost:8080/](http://localhost:8080/)** hoặc **[http://localhost:8080/login.html](http://localhost:8080/login.html)**
- Sử dụng bất kỳ tài khoản demo ở bảng trên với mật khẩu `Demo@12345`.
- Nhấn `Ctrl + C` tại cửa sổ dòng lệnh để dừng máy chủ.

### Khởi chạy với Cơ sở dữ liệu MySQL:
1. Mở MySQL CLI và nạp dữ liệu:
   ```sql
   SOURCE database/schema.sql;
   SOURCE database/seed.sql;
   CREATE USER 'topic_app'@'localhost' IDENTIFIED BY 'Demo@12345';
   GRANT ALL PRIVILEGES ON student_topic_management.* TO 'topic_app'@'localhost';
   FLUSH PRIVILEGES;
   ```
2. Cấu hình biến môi trường và chạy profile MySQL:
   ```powershell
   $env:DB_URL = "jdbc:mysql://localhost:3306/student_topic_management?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh"
   $env:DB_USERNAME = "topic_app"
   $env:DB_PASSWORD = "Demo@12345"
   $env:SESSION_COOKIE_SECURE = "false"
   .\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mysql
   ```

---

## Kiểm thử

Hệ thống được kiểm thử tự động toàn diện qua bộ kiểm thử hồi quy:

```powershell
.\mvnw.cmd test
```

### Kết quả kiểm thử:
```text
[INFO] Results:
[INFO] Tests run: 81, Failures: 0, Errors: 0, Skipped: 1
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```
**80 kiểm thử thường lệ đều đạt**. Kiểm tra MySQL được bỏ qua khi máy cục bộ chưa cấu hình MySQL; job CI riêng chạy kiểm tra này trên MySQL 8.4, kiểm chứng Flyway V5 → V6, `ddl-auto=validate` cho toàn bộ entity và lưu nhật ký dài có tiếng Việt. GitHub Actions còn chạy `verify` trên Java 21, đóng gói JAR và kiểm tra cú pháp 11 file JavaScript. Các kiểm thử mới bao phủ hai lỗi hồi quy, chọn đợt, phân quyền tại method, hàng đợi email/thử lại, SMTP cục bộ, Excel và PDF nhiều trang. [Xem CI](https://github.com/leminhhuy-ute/Web-Programming-WEPR330479-/actions).

---

## Tài liệu

Tài liệu kỹ thuật chuyên sâu được duy trì đầy đủ tại thư mục [`docs/`](docs/):

- 📘 **[Tổng quan hệ thống (SYSTEM_OVERVIEW.md)](docs/SYSTEM_OVERVIEW.md)**: Mục tiêu học thuật, phạm vi đề tài, nhóm người dùng và các phân hệ chức năng.
- 🏛️ **[Kiến trúc hệ thống (SYSTEM_ARCHITECTURE.md)](docs/SYSTEM_ARCHITECTURE.md)**: Kiến trúc phân tầng, chuỗi bộ lọc bảo mật, xử lý ngoại lệ và an toàn tệp tải lên.
- 🗄️ **[Thiết kế Cơ sở dữ liệu (DATABASE_DESIGN.md)](docs/DATABASE_DESIGN.md)**: Sơ đồ ERD quan hệ, mô tả cấu trúc bảng, ràng buộc khóa và lịch sử migration Flyway.
- 🔄 **[Quy trình nghiệp vụ (BUSINESS_WORKFLOW.md)](docs/BUSINESS_WORKFLOW.md)**: Máy trạng thái, chu trình 4 mốc thời gian của đợt, nộp báo cáo 3 giai đoạn và chấm điểm hội đồng.
- 🔌 **[Tài liệu REST API (API_DOCUMENTATION.md)](docs/API_DOCUMENTATION.md)**: Danh mục toàn bộ các endpoint REST, cấu trúc payload mẫu và bảng mã lỗi HTTP.
- 🧪 **[Kịch bản kiểm thử thủ công (MANUAL_TESTING_SCENARIO.md)](docs/MANUAL_TESTING_SCENARIO.md)**: Cẩm nang hướng dẫn kiểm thử thủ công chi tiết từng bước từ đăng nhập đến công bố kết quả.
- 📋 **[Báo cáo nghiệm thu & Xác minh (FINAL_VERIFICATION_REPORT.md)](docs/FINAL_VERIFICATION_REPORT.md)**: Báo cáo khắc phục các lỗi audit, kiểm thử hồi quy 54/54 test và cải tiến giao diện.
- 📝 **[Thay đổi so với bản gốc ngày 09/10/2026](docs/CHANGE_REPORT_2026-10-09.md)**: Lỗi đã sửa, tính năng mới, commit, kiểm chứng và hướng dẫn test.
- ✉️ **[Cấu hình SMTP/Brevo](docs/SMTP_SETUP.md)**: Biến môi trường, hàng đợi email và migration V6.
- 🚀 **[Hướng dẫn triển khai Production (DEPLOYMENT_GUIDE.md)](docs/DEPLOYMENT_GUIDE.md)**: Hướng dẫn cài đặt máy chủ Linux, dịch vụ systemd, reverse proxy Nginx SSL và sao lưu dữ liệu MySQL.

---

## Lưu ý

1. **Bảo tồn tài khoản Demo**: Nhằm phục vụ mục đích đánh giá và chấm điểm học phần Lập trình Web, tài khoản demo cùng mật khẩu mặc định `Demo@12345` được lưu giữ trong cơ sở dữ liệu để kiểm thử các vai trò khác nhau qua form đăng nhập tiêu chuẩn (đã gỡ bỏ toàn bộ các nút bấm điền nhanh trên giao diện để sẵn sàng phát hành).
2. **Khởi tạo cơ sở dữ liệu**: Khi triển khai trên môi trường MySQL thực tế, bắt buộc thực thi tập lệnh [database/schema.sql](database/schema.sql) trước khi khởi động ứng dụng vì profile `mysql` sử dụng chế độ `ddl-auto=validate`.
3. **Migration dữ liệu**: Profile MySQL dùng Flyway tại `src/main/resources/db/migration/mysql/`. DB hiện hành phải tương ứng V5 trước khi baseline và chạy V6; V2–V5 trong `database/migration/` được giữ làm lịch sử, không tự chạy lại trên schema đầy đủ. Xem [hướng dẫn migration và SMTP](docs/SMTP_SETUP.md).
