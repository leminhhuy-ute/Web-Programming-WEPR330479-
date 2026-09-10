# HCM-UTE · Module Sinh viên & nhóm sinh viên

Phần làm riêng cho thành viên phụ trách sinh viên, triển khai từ bốn màn hình trong [dự án Stitch](https://stitch.withgoogle.com/projects/4612862007421922669?pli=1): Dashboard Sinh viên, Quản lý Nhóm Sinh viên, Kho Đề tài & Đăng ký, Tiến độ & Nộp báo cáo. Có thêm trang chi tiết đề tài đang thực hiện và đăng nhập để demo độc lập.

Giao diện tiếng Việt, màu #0072BC / navy, nền #F6F8FB, font Be Vietnam Pro, logo HCM-UTE. Backend lưu dữ liệu thật bằng Spring; không dùng localStorage làm dữ liệu nghiệp vụ. Font có system fallback khi offline.

## Công nghệ áp dụng

| Nhóm | Công nghệ | Cách dùng trong phần Sinh viên & nhóm sinh viên |
|---|---|---|
| Ngôn ngữ | Java 21 | Xây dựng backend, mô hình dữ liệu và xử lý quy tắc nghiệp vụ. |
| Nền tảng | Spring Boot 3.5.16 | Khởi tạo ứng dụng, quản lý cấu hình, dependency và máy chủ web nhúng. |
| Web | Spring MVC, REST API | Điều hướng trang, nhận request và cung cấp API `/api/student` cho giao diện. |
| Giao diện | Thymeleaf, HTML5, CSS3, JavaScript thuần | Render trang đăng nhập và khung ứng dụng; tạo giao diện responsive, modal, bộ lọc và gọi API. Không cần React hoặc Node.js. |
| Dependency Injection | Spring IoC | Inject service, repository, cấu hình bảo mật và `Clock` bằng constructor để dễ kiểm thử. |
| Dữ liệu | Spring Data JPA, Hibernate | Ánh xạ entity và thao tác sáu bảng qua `JpaRepository`, không viết lại CRUD thủ công. |
| Giao dịch | Spring Transaction | Dùng `@Transactional` và khóa bi quan khi nhận lời mời hoặc đăng ký đề tài, tránh vượt giới hạn thành viên/chỗ trống. |
| Bảo mật | Spring Security, BCrypt, Session, CSRF | Đăng nhập bằng MSSV, mã hóa mật khẩu, phân quyền `ROLE_STUDENT` và bảo vệ các request thay đổi dữ liệu. |
| Kiểm tra dữ liệu | Jakarta Bean Validation | Kiểm tra dữ liệu request trước khi đưa vào tầng nghiệp vụ. |
| Lập trình hướng khía cạnh | Spring AOP | Ghi log các thao tác sinh viên thành công mà không lặp mã ở từng chức năng. |
| Cơ sở dữ liệu demo | H2 file database | Chạy độc lập và giữ dữ liệu sau khi tắt/mở ứng dụng. |
| Cơ sở dữ liệu tích hợp | MySQL 8 Connector/J | Có cấu hình profile `mysql` và schema tham khảo để ghép vào đồ án chung. |
| Kiểm thử | JUnit 5, Spring Boot Test, Spring Security Test, MockMvc | Kiểm thử nghiệp vụ, phân quyền, CSRF, upload báo cáo và các trường hợp tranh chỗ đồng thời. |
| Build | Maven Wrapper | Tải dependency, chạy test và đóng gói JAR mà không cần cài Maven riêng. |

Spring Batch không được thêm vào module vì phần này không có tác vụ nhập/xử lý dữ liệu hàng loạt. Nếu sau này cần nhập danh sách sinh viên hoặc đề tài từ Excel/CSV với số lượng lớn thì có thể bổ sung Spring Batch.

## Cấu trúc thư mục giao diện

Dự án giữ cách tách HTML, CSS, JavaScript và Images giống bố cục website cơ bản trong hình tham khảo, đồng thời đặt chúng đúng vị trí mà Spring Boot nhận diện:

```text
src/main/resources/
├── templates/              # HTML do Thymeleaf render
│   ├── login.html
│   └── student.html
├── static/                 # Tài nguyên trình duyệt truy cập trực tiếp
│   ├── css/
│   │   └── app.css
│   ├── js/
│   │   └── app.js
│   └── images/
│       ├── logo.png
│       └── campus.jpg
├── application.properties
└── application-mysql.properties
```

Trong Spring MVC, HTML động nằm trong `templates` thay vì đặt `index.html` ở thư mục gốc. Controller trả về trang `login` hoặc `student`; các đường dẫn `/css`, `/js` và `/images` được Spring Boot phục vụ như tài nguyên tĩnh.

## Chạy trên máy của bạn

Mở PowerShell tại thư mục dự án:

```powershell
.\run.ps1
```

Mở http://localhost:8080. Dừng bằng Ctrl+C trong terminal đang chạy. `run.ps1` tự dùng JDK 21 đã đặt ở `.tools/jdk21` trên máy này. Khi gửi cho máy khác, cài JDK 21 rồi đặt JAVA_HOME. Không cần cài Maven riêng vì đã có `mvnw.cmd` / `mvnw`.

```powershell
# Trên máy khác, thay đường dẫn cho đúng JDK đã cài:
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-21'
.\mvnw.cmd spring-boot:run
```

Lần đầu cần Internet tải dependency. Java hệ thống của máy hiện tại là 26 nên dùng JDK 21 riêng để phù hợp Spring Boot 3.5. [Yêu cầu hệ thống Spring Boot](https://docs.spring.io/spring-boot/3.5/system-requirements.html).

## Tài khoản demo

Mật khẩu chung: `Student@123`. Chỉ có trong profile demo, không phải tài khoản trường.

| MSSV | Người dùng | Trạng thái ở database mới |
|---|---|---|
| 22110001 | Nguyễn Minh Anh | Chưa có nhóm, dùng thử tạo nhóm |
| 22110002 | Trần Ngọc Linh | Chưa có nhóm, nhận lời mời |
| 22110003 | Lê Hoàng Nam | Chưa có nhóm, nhận lời mời |
| 22110004 | Phạm Gia Huy | Nhóm trưởng Nhóm Thư viện số, đề tài đã APPROVED |
| 22110005 | Võ Thảo Nguyên | Thành viên Nhóm Thư viện số |
| 22110006 | Đặng Quang Minh | Chưa có nhóm, dùng thử giới hạn thành viên |

Dữ liệu được lưu qua các lần chạy nên trạng thái có thể đã thay đổi sau khi bạn thao tác hoặc sau kiểm tra giao diện. Tạo database demo riêng bằng cách đổi `spring.datasource.url`; không cần xóa database đang dùng.

## Chức năng hoàn thành

- Tạo nhóm; người tạo là nhóm trưởng đầu tiên.
- Mời theo MSSV; người nhận chấp nhận/từ chối; hiển thị lịch sử lời mời.
- Tối đa 3 thành viên; một sinh viên thuộc một nhóm.
- Chọn một thành viên làm nhóm trưởng; kiểm tra lại quyền ở backend.
- Tìm kiếm theo tên/mã đề tài/GV/công nghệ; lọc bộ môn, loại đề tài; xem mô tả và thời gian.
- Nhóm trưởng đăng ký một đề tài đã công bố, còn chỗ và đang mở; xem trạng thái chờ/duyệt/từ chối.
- Xem đề tài đang thực hiện và phản hồi của giảng viên.
- Nhóm trưởng nộp PDF/DOCX tối đa 10 MB sau APPROVED; lịch sử, ghi chú, đánh dấu nộp muộn và tải báo cáo trong nhóm.
- Đăng nhập/đăng xuất bằng Spring Security, BCrypt, session, CSRF.

Đề bài quy định tối đa **3 sinh viên trong một nhóm của hệ thống**. Đây là giới hạn khác với 5 người chia việc làm đồ án.

## Áp dụng các tính năng Spring trong slide

| Tính năng | Áp dụng trong mã |
|---|---|
| Dependency Injection | Constructor injection trong StudentService, StudentController, SecurityConfig; inject Clock để xử lý thời gian |
| AOP | AuditAspect ghi nhận tên hành động sau transaction thành công |
| Transaction Management | @Transactional và pessimistic lock chống vượt nhóm/chỗ đăng ký |
| Spring MVC | Controller trang Thymeleaf; REST controller cho module sinh viên |
| Integration | Thymeleaf, Bean Validation, Hibernate, H2; cấu hình MySQL để ghép bài nhóm |
| Spring Batch | Chưa cần: không có xử lý dữ liệu hàng loạt trong phần được phân công |
| Spring Data | Sáu JpaRepository, truy vấn và quản lý entity |
| Spring Security | Xác thực, ROLE_STUDENT, BCrypt, CSRF; thêm kiểm tra chủ nhóm trong service |

## Kiểm thử và đóng gói

```powershell
.\run.ps1 -Test
.\run.ps1 -Build
# Hoặc:
.\mvnw.cmd test
.\mvnw.cmd package
java -jar target/student-module-1.0.0.jar
```

10 kiểm thử tự động gồm quyền nhóm trưởng, giới hạn nhóm, xử lý lời mời, thời gian/công bố đề tài, đăng ký lại sau từ chối, hai nhóm tranh chỗ cuối, quyền nộp/tải báo cáo, định dạng/kích thước file, nộp muộn, đăng nhập và CSRF.

Luồng demo: 22110001 tạo nhóm → mời 22110002 → đăng xuất → 22110002 nhận lời mời → nhóm trưởng đăng ký đề tài → xem PENDING. Dùng 22110004 để demo nộp báo cáo ngay vì đề tài đã được duyệt sẵn. Việc xét duyệt thật do module giảng viên thực hiện khi ghép.

## Đọc mã và ghép với nhóm

- `Domain.java`, `Repositories.java`: dữ liệu và tầng truy cập JPA.
- `StudentService.java`: toàn bộ nghiệp vụ sinh viên/nhóm.
- `StudentController.java`: trang và API; `SecurityConfig.java`: bảo mật.
- `templates/`: các trang HTML động (`login.html`, `student.html`).
- `static/css/`: file định dạng giao diện.
- `static/js/`: JavaScript xử lý tương tác và gọi REST API.
- `static/images/`: logo và hình ảnh của trường.
- `DemoData.java`: dữ liệu mẫu độc lập, chỉ chạy ở profile demo.
- `docs/INTEGRATION.md`: API, schema, trạng thái và ranh giới với các thành viên khác.
- `docs/mysql-schema.sql`: schema MySQL tham khảo để chạy profile mysql.

Chưa triển khai chức năng của quản trị, giảng viên, hội đồng, điểm số. Tài khoản/đề tài/đợt thật cần được nối với dữ liệu chung khi ghép; hiện có seed demo để phần của bạn chạy độc lập. Cấu hình MySQL đã chuẩn bị nhưng chưa kiểm thử với MySQL server. Bản chạy mặc định bind localhost.
