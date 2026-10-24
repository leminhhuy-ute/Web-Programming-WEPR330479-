# Email thông báo điểm – cấu hình và vận hành

Ứng dụng gửi **điểm thật** qua hàng đợi sau khi Trưởng khoa công bố kết quả đã tổng hợp. Mỗi sinh viên nhận một email tại địa chỉ trong tài khoản; hệ thống không tạo điểm hoặc người nhận mẫu.

Mailjet SMTP dùng host `in-v3.mailjet.com`, API key làm tên đăng nhập SMTP, Secret Key làm mật khẩu. Có thể dùng cổng `2525` với STARTTLS. Xác minh người gửi trong Mailjet trước khi gửi.

**Lưu ý quan trọng về Gmail:** xác minh một địa chỉ `@gmail.com` trong Mailjet không đồng nghĩa với quyền gửi thư từ miền `gmail.com`. Gmail yêu cầu người gửi không giả mạo địa chỉ Gmail; nếu Mailjet gửi thư có `From` cá nhân Gmail, thư có thể bị đánh dấu Spam hoặc từ chối. Để giảm nguy cơ, cần dùng địa chỉ trên tên miền mình sở hữu và cấu hình SPF/DKIM theo các giá trị Mailjet cung cấp; thêm DMARC để bảo vệ miền. Không có cách bảo đảm tuyệt đối email luôn vào Inbox.

Nguồn chính thức: [Mailjet SMTP configuration](https://dev.mailjet.com/docs/smtp-relay/configuration), [xác thực tên miền SPF/DKIM của Mailjet](https://documentation.mailjet.com/hc/en-us/articles/360049641733-Authenticating-Domains-with-SPF-and-DKIM-A-Complete-Guide), [hướng dẫn người gửi của Gmail](https://support.google.com/mail/answer/81126?hl=vi).

## Bật gửi thư bằng PowerShell

```powershell
$env:MAIL_HOST = "in-v3.mailjet.com"
$env:MAIL_PORT = "2525"
$env:MAIL_USERNAME = "API key Mailjet"
$env:MAIL_PASSWORD = "Secret Key Mailjet"
$env:MAIL_AUTH = "true"
$env:MAIL_STARTTLS = "true"
$env:MAIL_FROM = "notifications@ten-mien-cua-ban.vn"
$env:MAIL_ENABLED = "true"
.\mvnw.cmd spring-boot:run
```

Thiết lập các biến này ở môi trường chạy ứng dụng. `.env.example` chỉ là mẫu; Spring Boot không tự đọc `.env`. Không commit thông tin đăng nhập. Mailjet hỗ trợ port 2525 với STARTTLS; timeout kết nối/đọc/ghi 3 giây. Các tên `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`, `SMTP_AUTH` và `SMTP_STARTTLS` cũ vẫn là giá trị dự phòng nếu chưa đặt biến `MAIL_*` tương ứng.

Trong cấu hình production, đặt `SPRING_PROFILES_ACTIVE=cloud` để tránh nạp tài khoản/đề tài mẫu. `.env.example` đang để `cloud` làm cấu hình khuyến nghị; chỉ thêm profile demo khi chủ động muốn dữ liệu minh họa trên một cơ sở dữ liệu trống.

## Quy trình

1. Chủ tịch hội đồng tổng hợp điểm; Trưởng khoa công bố kết quả đã tổng hợp.
2. Cùng giao dịch công bố, hệ thống lưu một email cho từng thành viên nhóm. Gọi công bố lần nữa không tạo thêm email.
3. Khi `MAIL_ENABLED=true`, tác vụ nền lấy tối đa 20 thư mỗi lượt, gửi tuần tự, 30 giây giữa các lượt. Cấu hình mặc định tắt gửi nhưng vẫn lưu hàng đợi.
4. SMTP lỗi: thử lại sau 1, 2, 4, 8 phút; sau lần thứ 5 chuyển Thất bại. Trưởng khoa xem `/admin/email-notifications.html` và chọn Gửi lại.
5. Thư đã gửi không được gửi lại bằng nút Gửi lại. Hàng đợi tồn tại qua lần khởi động lại nếu dùng cơ sở dữ liệu file/MySQL.

SMTP không bảo đảm gửi đúng một lần: nếu máy dừng sau khi SMTP nhận thư nhưng trước khi giao dịch ghi SENT hoàn tất, thư có thể được gửi lại. Trạng thái SENT có nghĩa máy chủ SMTP chấp nhận thư, không xác nhận sinh viên đã nhận/đọc thư.

## Cơ sở dữ liệu MySQL

Schema bootstrap có bảng `email_notifications`. Với DB hiện hành đã tương ứng V5, profile `mysql` baseline ở V5 và chạy V6–V13 bằng Flyway trong `src/main/resources/db/migration/mysql/`. Bản V6 tiện tra cứu nằm ở `database/migration/`; V2–V5 là lịch sử và không được chạy lại tự động trên schema bootstrap. DB cũ chưa đạt V5 cần áp dụng đúng các nâng cấp còn thiếu trước khi chạy bản mới. Profile demo/test H2 dùng Hibernate và tắt Flyway.

Kiểm thử tự động sử dụng MailSender giả lập, không gửi email ra ngoài. Việc gửi thực tế cần cấu hình tài khoản SMTP và biến môi trường trên máy chạy.
