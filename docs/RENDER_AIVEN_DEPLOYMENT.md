# Triển khai bài lab: Render + Aiven MySQL

Áp dụng cho Hệ thống quản lý đề tài sinh viên, Java 21, Spring Boot 3.5.16.
Tài liệu lab dùng ứng dụng Todo làm ví dụ; dự án này dùng `/login.html` và JAR `topic-management-1.0.0.jar`.

## 1. Các tệp đã chuẩn bị

| Tệp | Mục đích |
| --- | --- |
| `Dockerfile` | Build JAR bằng JDK 21, chạy bằng JRE 21 với tài khoản không phải root |
| `.dockerignore` | Loại dữ liệu cục bộ, thông tin bí mật và sản phẩm build khỏi Docker context |
| `render.yaml` | Cấu hình Web Service Docker, Free, Singapore, health check `/login.html` |
| `application-cloud.properties` | Dùng MySQL, biến môi trường, HTTPS proxy và connection pool nhỏ |
| `db/migration/cloud/V1__create_initial_schema.sql` | Tạo schema trực tiếp trong database được chọn ở JDBC URL |

Flyway chạy V1 -> V6 -> V7 -> V8; Hibernate chỉ `validate`. Không chạy `schema.sql`/`seed.sql` thủ công trên Aiven.
Profile cloud dành cho database MỚI, TRỐNG. Database cũ đã chạy V5 dùng profile `mysql` theo tài liệu cũ.
Không sửa migration V1 sau khi đã triển khai vì Flyway kiểm tra checksum.

## 2. Đăng ký Aiven và tạo MySQL

1. Mở <https://console.aiven.io/>, đăng ký/đăng nhập và hoàn tất xác minh nếu được yêu cầu.
2. Tạo service, chọn **MySQL** và gói **Free**. Không chọn gói trả phí hay tự nâng cấp.
3. Chọn khu vực gần Việt Nam nếu gói Free cho phép; nếu không có Singapore, dùng khu vực có sẵn trong gói Free.
4. Đặt tên gợi nhớ, ví dụ `hcmute-topic-db`; đợi service báo **Running**.
5. Trong Connection information, lấy Host, Port, User, Password và Database name (thường `defaultdb`).

Giữ mật khẩu trong dashboard. Không gửi vào chat, ảnh chụp, GitHub hay tài liệu nộp bài.
Để database mới trống; ứng dụng sẽ tự tạo bảng trong lần khởi động đầu tiên.

## 3. Tạo Web Service trên Render

1. Mở <https://dashboard.render.com/>, đăng ký/đăng nhập; liên kết GitHub và cấp quyền truy cập repository của bạn.
2. Chọn **New -> Web Service**, chọn repository `leminhhuy-ute/Web-Programming-WEPR330479-`.
3. Branch: `main`; Root Directory: **để trống** vì Dockerfile và pom.xml ở gốc repository.
4. Runtime/Language: **Docker**; Dockerfile path: `./Dockerfile`; region: **Singapore**; instance: **Free**.
5. Nếu có Docker Command, để trống để sử dụng ENTRYPOINT trong Dockerfile.
6. Trong Environment, thêm các biến dưới đây trước khi Deploy.

| Key | Value |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `cloud` |
| `DB_URL` | `jdbc:mysql://HOST:PORT/defaultdb?sslMode=REQUIRED&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh` |
| `DB_USERNAME` | User trong Aiven, thường `avnadmin` |
| `DB_PASSWORD` | Password trong Aiven, nhập trực tiếp vào dashboard |
| `MAIL_ENABLED` | `false` ban đầu; chỉ bật `true` sau khi xác thực người gửi SMTP |
| `TZ` | `Asia/Ho_Chi_Minh` |

Với Mailjet, đặt `SMTP_HOST=in-v3.mailjet.com`, `SMTP_PORT=2525`, `SMTP_USERNAME` là API key và `SMTP_PASSWORD` là Secret Key. Đặt `MAIL_FROM` thành địa chỉ trên tên miền bạn quản lý sau khi cấu hình SPF/DKIM trong Mailjet. Chỉ lưu thông tin xác thực trong Render Environment. Gmail có thể coi thư gửi qua Mailjet với địa chỉ cá nhân `@gmail.com` là giả mạo và chuyển vào Spam.

Thay HOST, PORT và database name đúng thông tin của Aiven. Port Aiven có thể khác 3306.
MySQL Connector/J dùng `sslMode=REQUIRED`, không dùng `sslmode=require` như ví dụ trong PDF.
`REQUIRED` bắt buộc mã hóa TLS nhưng không xác minh danh tính máy chủ; nếu cần xác minh CA/hostname, dùng `VERIFY_IDENTITY` và truststore chứa CA của Aiven.

Có thể dùng `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` thay cho ba biến DB_*; chỉ chọn một bộ để tránh nhầm.
Không cần đặt PORT; ứng dụng tự đọc `${PORT:8080}` do Render cung cấp.
Secure session cookie bật mặc định; ứng dụng nhận thông tin HTTPS từ reverse proxy của Render.

**Cách khác:** New -> Blueprint, chọn repository và `render.yaml`; nhập ba biến DB_* khi Render yêu cầu.
Chỉ tạo MỘT Web Service bằng một trong hai cách.

## 4. Kiểm tra sau khi Deploy

1. Trong Logs, tìm Flyway migrate thành công V1, V6, V7, V8 và `Started TopicManagementApplication`.
2. Đợi trạng thái Render **Live**. Mở URL HTTPS do Render cấp, ví dụ `https://TEN-SERVICE.onrender.com/login.html`.
3. Production dùng `cloud` và không tạo tài khoản mẫu. Chỉ thêm `cloud-demo` khi cố ý triển khai một DB trống dành riêng cho demo; profile này tạo 13 tài khoản mẫu, ví dụ `dean01`, `hod01`, `lecturer01`, `student01`, mật khẩu ban đầu `Demo@12345`.
4. Đăng nhập Trưởng khoa, tạo một thông báo thử; đăng xuất, đăng nhập Sinh viên và kiểm tra hiển thị.
5. Khởi động lại/redeploy Web Service, đăng nhập lại và kiểm tra thông báo vẫn còn. Phiên đăng nhập được lưu trong bộ nhớ nên cần đăng nhập lại sau restart.
6. Mở URL trên điện thoại để kiểm tra HTTPS và giao diện. Không đưa mật khẩu DB vào ảnh minh chứng.

Dữ liệu và byte nội dung tệp báo cáo nằm trong MySQL Aiven, không phụ thuộc ổ đĩa tạm của Render.
DemoData không nạp lại hoặc đặt lại dữ liệu khi users đã có bản ghi.
Nếu trước đây đã chạy `cloud-demo`, bỏ profile này sẽ ngăn nạp mẫu lần sau nhưng không xóa dữ liệu mẫu đã có trong Aiven. Kiểm tra và thay các tài khoản/email mẫu bằng dữ liệu được phép dùng trước khi gửi thông báo thật. Tài khoản mẫu chỉ phục vụ demo; không dùng mật khẩu mẫu cho dữ liệu thực.

Render Free có thể ngủ sau khoảng 15 phút không truy cập, lần mở lại sẽ chậm.
Render Free chặn các cổng SMTP 25/465/587. Mailjet hỗ trợ cổng 2525, nhưng cần xác minh khả năng kết nối từ dịch vụ và cấu hình người gửi; Gmail SMTP dùng port 587 sẽ không gửi được từ gói Free. Chỉ bật `MAIL_ENABLED` khi người gửi đã được xác thực.

## 5. Build và kiểm thử cục bộ

```powershell
.\mvnw.cmd clean verify
java -jar target/topic-management-1.0.0.jar
```

Lệnh chạy JAR mặc định dùng H2 local, kiểm tra được đóng gói. Profile cloud phải có thông tin MySQL.

Nếu có Docker Engine đang chạy:

```powershell
docker build -t hcmute-topic-management .
docker run --rm -p 8080:8080 hcmute-topic-management
```

Container lệnh trên dùng H2 tạm để kiểm tra ảnh Docker. Dữ liệu H2 mất khi xóa container; bản Render phải dùng profile cloud và DB_* như mục 3.
GitHub Actions kiểm tra cả nâng cấp MySQL V5 cũ và tạo database MySQL trống cho cloud, tính không nạp lại dữ liệu mẫu và đăng nhập.

## 6. Lỗi thường gặp

| Lỗi | Cách xử lý |
| --- | --- |
| Access denied | Kiểm tra DB_USERNAME và DB_PASSWORD trong Render |
| Communications link failure | Kiểm tra Host, Port của Aiven, service Running và quyền truy cập mạng |
| Driver rejects URL / thiếu JDBC URL | DB_URL phải bắt đầu `jdbc:mysql://`, không dán URI `mysql://` |
| SSL handshake | Kiểm tra `sslMode=REQUIRED`, đúng endpoint MySQL và đúng port |
| Non-empty schema but no schema history table | cloud cần DB mới trống; dừng lại, không xóa dữ liệu hoặc sửa baseline tùy tiện |
| Hibernate thiếu bảng/cột | Kiểm tra active profiles và log Flyway; không đổi ddl-auto thành update để che lỗi |
| Web hoạt động nhưng không đăng nhập được | Dùng HTTPS công khai của Render; xem secure cookie và profile đang chạy |
| DB trống nhưng không có tài khoản | Kiểm tra có profile `cloud-demo`; profile cloud đơn lẻ chỉ tạo schema |
| Không nhận Gmail | Render Free chặn SMTP; xem giải thích ở mục 4 |

## 7. Minh chứng nộp bài và dọn tài nguyên

Chuẩn bị ảnh: build JAR thành công, Aiven Running, Render Live, website HTTPS và dữ liệu vẫn còn sau redeploy.
Không xóa dịch vụ trước buổi demo. Sau khi được chấm xong, sao lưu dữ liệu cần giữ và tự xóa Render Web Service/Aiven service nếu không còn dùng.

Tham khảo: <https://render.com/docs/docker>, <https://render.com/docs/free>, <https://aiven.io/docs/products/mysql/concepts/mysql-free-tier>, <https://dev.mysql.com/doc/connector-j/en/connector-j-reference-using-ssl.html>.
