# Hệ thống quản lý đề tài sinh viên HCM-UTE

Đồ án môn **Lập trình Web** xây dựng hệ thống quản lý đề tài cho Trường Đại học Công nghệ Kỹ thuật TP. Hồ Chí Minh. Ứng dụng hỗ trợ xuyên suốt quy trình từ tạo nhóm, đăng ký đề tài, duyệt và phân công hướng dẫn đến nộp báo cáo, chấm điểm và công bố kết quả.

## Chức năng chính

- **Trưởng khoa:** quản lý tài khoản, bộ môn, đợt đăng ký, thông báo, hội đồng và kết quả.
- **Trưởng bộ môn/Giảng viên:** đề xuất và duyệt đề tài, phân công giảng viên hướng dẫn, quản lý nhóm sinh viên.
- **Sinh viên:** tạo nhóm tối đa 3 thành viên, mời thành viên, chọn nhóm trưởng, đăng ký đề tài và nộp báo cáo.
- **Hội đồng:** phân công phản biện, nhập điểm, tổng hợp và công bố kết quả.

## Thành viên

| MSSV | Họ và tên |
| --- | --- |
| 24110019 | Lê Minh Huy |
| 24110012 | Trần Hữu Thành Đô |
| 24110033 | Lê Đăng Minh |
| 24162043 | Lê Bảo Huy |
| 24162048 | Nguyễn Quang Huy |

## Công nghệ áp dụng

| Công nghệ | Mục đích sử dụng |
| --- | --- |
| Java 21, Spring Boot 3.5.16 | Xây dựng và khởi chạy ứng dụng web với Tomcat nhúng |
| Spring MVC, REST API, Thymeleaf | Controller, API và giao diện động cho sinh viên |
| Dependency Injection | Quản lý và tiêm các thành phần service, repository, security |
| Spring Data JPA, Hibernate | Ánh xạ entity, quan hệ và truy vấn dữ liệu |
| Transaction Management | Đảm bảo tính toàn vẹn cho các luồng đăng ký, duyệt và chấm điểm |
| Spring Security | Đăng nhập theo session, BCrypt, CSRF và phân quyền theo vai trò |
| Spring AOP | Theo dõi thời gian thực thi nghiệp vụ |
| Bean Validation | Kiểm tra dữ liệu đầu vào |
| HTML, CSS, JavaScript | Xây dựng giao diện quản trị, giảng viên và hội đồng |
| H2, MySQL Connector | H2 dùng cho demo; hỗ trợ cấu hình kết nối MySQL |
| JUnit 5, Mockito, MockMvc | Kiểm thử đơn vị, tích hợp và phân quyền |
| Maven Wrapper | Quản lý dependency, kiểm thử và đóng gói dự án |

## Cấu trúc dự án

```text
LT_WEBCK/
├── src/
│   ├── main/
│   │   ├── java/topicmanagement/
│   │   │   ├── config/        # Cấu hình ứng dụng
│   │   │   ├── security/      # Đăng nhập và phân quyền
│   │   │   ├── controller/    # MVC Controller và REST API
│   │   │   ├── service/       # Xử lý nghiệp vụ
│   │   │   ├── repository/    # Spring Data JPA
│   │   │   ├── entity/        # Mô hình dữ liệu
│   │   │   ├── dto/           # Dữ liệu request/response
│   │   │   ├── student/       # Nhóm sinh viên và báo cáo
│   │   │   └── council/       # Hội đồng, điểm và kết quả
│   │   └── resources/
│   │       ├── templates/      # Giao diện Thymeleaf sinh viên
│   │       ├── static/         # HTML, CSS, JavaScript và hình ảnh
│   │       └── application*.properties
│   └── test/java/              # Kiểm thử JUnit, Mockito và MockMvc
├── docs/                       # Tài liệu tích hợp và kiểm thử
├── pom.xml                     # Cấu hình Maven
├── mvnw / mvnw.cmd             # Maven Wrapper
└── run.ps1                     # Script chạy nhanh trên Windows
```

## Tài khoản demo

Mật khẩu chung: `Demo@12345`

| Tài khoản | Vai trò |
| --- | --- |
| `dean01` | Trưởng khoa / Quản trị |
| `hod01` | Trưởng bộ môn |
| `lecturer01` đến `lecturer05` | Giảng viên |
| `student01` đến `student06` | Sinh viên |
