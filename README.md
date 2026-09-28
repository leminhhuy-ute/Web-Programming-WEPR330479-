# Hệ thống quản lý đề tài sinh viên HCM-UTE

Ứng dụng quản lý quy trình đề tài môn học, NCKH, TLCN và KLTN của Khoa Công nghệ Thông tin: tạo đợt đăng ký, đề xuất và duyệt đề tài, lập nhóm sinh viên, phân công GVHD/GVPB, nộp báo cáo, chấm điểm và công bố kết quả.

## Công nghệ và cách đóng gói

- Java 21, Spring Boot 3.5.16 và Tomcat nhúng.
- Spring MVC, Spring Security, Spring Data JPA, Hibernate và Thymeleaf.
- HTML, CSS và JavaScript thuần với một hệ thống màu sắc/typography HCM-UTE dùng chung.
- H2 file cho demo; MySQL 8.0+ cho môi trường triển khai.
- Maven Wrapper; đầu ra là executable JAR, không phải WAR.

## Chức năng

### Trưởng khoa

- Quản lý tài khoản, vai trò, trạng thái và bộ môn.
- Tạo đợt đăng ký với hai khoảng thời gian riêng cho GV và SV.
- Quản lý thông báo, dashboard thống kê, hội đồng và công bố kết quả.

### Trưởng bộ môn và giảng viên

- Đề xuất/sửa đề tài trong thời gian GV đăng ký.
- Trưởng bộ môn duyệt đề tài thuộc bộ môn trước khi giai đoạn SV bắt đầu.
- Gán một hoặc hai GVHD và duyệt nhóm đăng ký.
- Tham gia hội đồng, nhập điểm và nhận xét nếu không hướng dẫn đề tài đó.

### Sinh viên

- Tạo nhóm, mời thành viên, chuyển nhóm trưởng; tối đa ba thành viên.
- Mỗi sinh viên chỉ thuộc một nhóm; mỗi nhóm chỉ giữ một đăng ký đề tài.
- Nhiều nhóm có thể đăng ký cùng một đề tài đã công bố.
- Chỉ nhóm trưởng được đăng ký đề tài và nộp PDF/DOCX tối đa 10 MB.
- Xem kết quả sau khi Chủ tịch tổng hợp và Trưởng khoa công bố.

### Hội đồng

- Từ ba đến năm giảng viên, đúng một Chủ tịch, một Thư ký và ít nhất một GVPB.
- GVHD không được nằm trong hội đồng chấm đề tài mình hướng dẫn.
- Chủ tịch tổng hợp trung bình cộng khi đủ điểm của mọi thành viên.

## Chạy nhanh bằng H2

Yêu cầu: JDK 21 trở lên. Tại thư mục dự án:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

Nếu Maven Wrapper không ghi được vào thư mục `.m2`, dùng Maven cài trên máy:

```powershell
mvn -B clean test
mvn spring-boot:run
```

Mở [http://127.0.0.1:8080/](http://127.0.0.1:8080/). Dữ liệu demo được lưu tại `data/integrated-project.mv.db`; dừng ứng dụng bằng `Ctrl+C`.

Mật khẩu chung của tài khoản demo: `Demo@12345`.

| Tài khoản | Vai trò |
| --- | --- |
| `dean01` | Trưởng khoa |
| `hod01` | Trưởng bộ môn |
| `lecturer01`–`lecturer05` | Giảng viên |
| `student01`–`student06` | Sinh viên |

## Build và chạy JAR

```powershell
.\mvnw.cmd clean package
java -jar .\target\topic-management-1.0.0.jar
```

## Chạy với MySQL

1. Từ thư mục dự án, mở MySQL CLI:

```powershell
mysql -u root -p
```

2. Trong dấu nhắc MySQL, nạp schema, dữ liệu demo và tạo tài khoản ứng dụng một lần (thay mật khẩu ví dụ bằng mật khẩu riêng):

```sql
SOURCE database/schema.sql;
SOURCE database/seed.sql;
CREATE USER 'topic_app'@'localhost' IDENTIFIED BY 'your-password';
GRANT ALL PRIVILEGES ON student_topic_management.* TO 'topic_app'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

Nếu tài khoản `topic_app` đã tồn tại, bỏ qua câu lệnh `CREATE USER` và chỉ cấp lại quyền khi cần.

3. Khai báo biến môi trường trong phiên PowerShell hiện tại:

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/student_topic_management?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh'
$env:DB_USERNAME = 'topic_app'
$env:DB_PASSWORD = 'your-password'
$env:SESSION_COOKIE_SECURE = 'false' # chỉ dùng khi chạy HTTP localhost
```

4. Chạy profile MySQL:

```powershell
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=mysql'
```

Khi triển khai HTTPS, không đặt `SESSION_COOKIE_SECURE=false`. Profile MySQL dùng `ddl-auto=validate`, vì vậy schema phải được tạo đầy đủ trước khi khởi động.

## Kiểm thử thủ công theo vai trò

1. `lecturer01`: đề xuất đề tài trong **Đợt đề xuất môn học — Demo**.
2. `hod01`: duyệt đề tài, chọn một hoặc hai GVHD trước khi giai đoạn SV bắt đầu.
3. Để thử ngay luồng sinh viên, `student01` tạo nhóm, mời mã SV `22110002`, rồi đăng ký đề tài `DEMO-SV-01` trong **Đợt sinh viên đăng ký — Demo**.
4. `student02` chấp nhận lời mời; `lecturer01` duyệt nhóm.
5. Nhóm trưởng nộp PDF/DOCX; thành viên kiểm tra lịch sử và tải tệp.
6. `dean01` lập hội đồng gồm Chủ tịch, Thư ký và ít nhất một Phản biện, sau đó phân công nhóm cùng GVPB.
7. Các thành viên hội đồng nhập điểm; Chủ tịch tổng hợp; Trưởng khoa công bố.
8. Sinh viên mở mục **Kết quả đánh giá**.

## Bảo mật

- BCrypt và nâng cấp hash legacy sau đăng nhập thành công.
- HTTP Session, đổi session ID sau đăng nhập, CSRF cookie/header.
- Phân quyền URL và kiểm tra quyền lại ở tầng nghiệp vụ.
- Khóa phiên đang mở ngay khi tài khoản bị khóa hoặc đổi vai trò.
- Giới hạn năm lần đăng nhập sai trong cửa sổ 15 phút trên mỗi nút ứng dụng.
- Kiểm tra chữ ký PDF và cấu trúc DOCX, giới hạn kích thước và độ nở ZIP.

Giới hạn đăng nhập hiện lưu trong bộ nhớ của từng instance. Triển khai nhiều instance nên chuyển bộ đếm sang Redis hoặc gateway tập trung; tệp tải lên production nên được quét malware bằng dịch vụ chuyên dụng.

## Tài liệu

- [Kiến trúc và quy tắc triển khai](IMPLEMENTATION_QH.md)
- [Quyết định tích hợp module](docs/INTEGRATION.md)
- [Kết quả kiểm tra](docs/QA.md)
