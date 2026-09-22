# Hệ thống quản lý đề tài sinh viên — bản tích hợp

Nhánh `tong-hop-du-an` ghép quản trị, giảng viên, sinh viên và hội đồng trên một ứng dụng Spring Boot, một cơ sở dữ liệu và một hệ thống đăng nhập.

> Nhóm đã chọn tiếp tục phát triển trên `tong-hop-du-an`. `origin/main` có một bản tích hợp khác tại `dda4b6d`; chưa merge hai bản. Không chép đè hoặc merge tự động hai cấu trúc.

## Chạy nhanh

Cài **JDK 21**, đặt `JAVA_HOME` đến thư mục JDK (không phải thư mục bin). Maven Wrapper tự tải Maven/dependency lần đầu.

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

Mở http://localhost:8080/. Hoặc chạy `.\run.ps1`; `.\run.ps1 -Test` kiểm thử; `.\run.ps1 -Build` đóng gói.
Nếu PowerShell chặn script, dùng trực tiếp `mvnw.cmd`, không cần tắt chính sách bảo mật.

```powershell
.\mvnw.cmd clean package
java -jar target/topic-management-1.0.0.jar
```

Mặc định chỉ lắng nghe `127.0.0.1`, dùng profile `demo`, H2 lưu tại `data/integrated-project.mv.db`. Giữ thư mục data để giữ dữ liệu. Không chạy đồng thời hai ứng dụng vào cùng tệp H2. Đổi cổng qua biến `PORT` nếu 8080 bị chiếm.

## Tài khoản demo

Chỉ dùng để chạy thử trên máy cá nhân. Mật khẩu chung: `Demo@12345`.

| Tài khoản | Vai trò |
| --- | --- |
| dean01 | Trưởng khoa / quản trị |
| hod01 | Trưởng bộ môn |
| lecturer01 … lecturer05 | Giảng viên |
| student01 … student06 | Sinh viên, MSSV 22110001 … 22110006 |

Dữ liệu mẫu chỉ tạo khi bảng users trống và profile demo đang bật; không thay đổi tài khoản sẵn có. Các đợt mẫu tính ngày tương đối tại lần khởi tạo đầu tiên; nếu đã hết hạn, sửa đợt bằng dean01.

## Chức năng

- Quản trị tài khoản, bộ môn, đợt đăng ký và thông báo theo đối tượng.
- Giảng viên đề xuất đề tài; trưởng bộ môn/khoa duyệt; phân công 1–2 GVHD; GVHD nhận hoặc từ chối nhóm.
- Sinh viên tạo nhóm tối đa 3 người, mời/xác nhận tham gia, chuyển trưởng nhóm, đăng ký đề tài và theo dõi phê duyệt.
- Chỉ trưởng nhóm được nộp báo cáo PDF/DOCX tối đa 10 MB; thành viên chỉ tải báo cáo nhóm mình.
- Hội đồng có 3–5 người, đúng một Chủ tịch và một Thư ký. Không phân công hội đồng chứa GVHD của đề tài.
- Thành viên chấm bằng danh tính đăng nhập; Chủ tịch tổng hợp trung bình cộng khi đủ phiếu; trưởng khoa công bố; sinh viên chỉ xem kết quả của nhóm mình sau công bố.
- Khóa tài khoản/đổi vai trò có tác dụng cả với phiên đang đăng nhập.

## Công nghệ theo nội dung môn học

| Công nghệ | Áp dụng thực tế |
| --- | --- |
| Java 21, Spring Boot 3.5.16 | Khởi chạy và cấu hình ứng dụng, Tomcat nhúng, profiles |
| Spring MVC | Controller, REST API, trang Thymeleaf sinh viên |
| Dependency Injection | Tiêm repository/service và các thành phần bảo mật |
| Spring Data JPA / Hibernate | Entity, quan hệ, truy vấn dữ liệu |
| Transaction Management | Tạo nhóm, nhận lời mời, đăng ký, chấm và công bố điểm |
| Spring Security | Session, BCrypt, phân quyền, CSRF, cập nhật quyền phiên |
| Spring AOP | Đo thời gian gọi nghiệp vụ ở mức DEBUG, không ghi mật khẩu/tệp/điểm |
| Bean Validation | Kiểm tra dữ liệu đầu vào |
| HTML, CSS, JavaScript | Trang quản trị/giảng viên/hội đồng dùng HTML + REST; sinh viên dùng Thymeleaf + REST |
| JUnit 5, Mockito, MockMvc | Kiểm thử đơn vị và tích hợp |
| H2 / MySQL connector | H2 cho demo và kiểm thử; cấu hình MySQL tùy chọn |

Không dùng JSP. Không thêm Spring Batch vì hiện không có nghiệp vụ xử lý lô cần thiết.

## Cấu trúc

```text
src/main/java/topicmanagement/
  config/ security/ controller/ service/ repository/ entity/ dto/
  student/                 # Nhóm, lời mời, báo cáo
  council/                 # Hội đồng, phân công, điểm
src/main/resources/
  templates/student.html   # Thymeleaf
  static/
    index.html login.html
    admin/ lecturer/ councils/
    css/ js/ images/
    assets/                # CSS/JS/ảnh của các module đã ghép
src/test/java/             # Kiểm thử đơn vị và tích hợp
```

HTML, CSS, JavaScript và ảnh được tách riêng. Không sửa giao diện trong `target/`, vì đây là kết quả build.

## MySQL — chưa xác minh trên máy chủ thật

Profile `mysql` nhận `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` và mặc định `ddl-auto=validate`. Chưa cung cấp migration sản xuất cho toàn bộ bản ghép; **không dùng schema.sql cũ để kết luận đã có đủ bảng**.

Để thử trên **cơ sở dữ liệu trống, riêng cho demo**, có thể bật `mysql,demo` và đặt `SPRING_JPA_HIBERNATE_DDL_AUTO=update` cho lần khởi tạo. Không làm điều này trên dữ liệu thật. Trước triển khai cần migration có phiên bản, tài khoản/mật khẩu riêng, HTTPS, giới hạn đăng nhập và kiểm thử MySQL.

## Kiểm thử và giới hạn

Xem [báo cáo kiểm tra](docs/QA.md) và [nguồn các module](docs/INTEGRATION.md).
Các tài liệu `README_QH.md`, `IMPLEMENTATION_QH.md`, `database/*.sql`, cấu hình Tomcat cũ là tham chiếu nhánh quản trị trước tích hợp, không phải hướng dẫn chạy bản hiện tại.

## Thành viên

| MSSV | Họ và tên |
| --- | --- |
| 24110019 | Lê Minh Huy |
| 24110012 | Trần Hữu Thành Đô |
| 24110033 | Lê Đăng Minh |
| 24162043 | Lê Bảo Huy |
| 24162048 | Nguyễn Quang Huy |
