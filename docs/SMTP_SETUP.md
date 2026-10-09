# Email thông báo điểm – cấu hình và kiểm thử

Chọn **Brevo** cho đề tài: gói miễn phí hiện có 300 email/ngày. Tạo người gửi và xác minh địa chỉ gửi trong Brevo, sau đó lấy **SMTP login** và **SMTP key** ở mục SMTP & API. SMTP key khác API key.

Nguồn chính thức: [gói dịch vụ](https://help.brevo.com/hc/en-us/articles/208589409-About-Brevo-s-pricing-plans), [cấu hình SMTP](https://developers.brevo.com/docs/smtp-integration).

## Bật gửi thư bằng PowerShell

```powershell
$env:SMTP_HOST = "smtp-relay.brevo.com"
$env:SMTP_PORT = "587"
$env:SMTP_USERNAME = "SMTP login trong Brevo"
$env:SMTP_PASSWORD = "SMTP key trong Brevo"
$env:MAIL_FROM = "địa chỉ người gửi đã xác minh"
$env:MAIL_ENABLED = "true"
.\mvnw.cmd spring-boot:run
```

Thiết lập các biến này ở môi trường chạy ứng dụng. `.env.example` chỉ là mẫu; Spring Boot không tự đọc `.env`. Không commit thông tin đăng nhập. Dùng port 587 với STARTTLS; timeout kết nối/đọc/ghi 3 giây.

## Quy trình

1. Chủ tịch tổng hợp điểm, trưởng khoa công bố kết quả.
2. Cùng giao dịch công bố, hệ thống lưu một email cho từng thành viên nhóm. Gọi công bố lần nữa không tạo thêm email.
3. Khi `MAIL_ENABLED=true`, tác vụ nền lấy tối đa 20 thư mỗi lượt, gửi tuần tự, 30 giây giữa các lượt. Cấu hình mặc định tắt gửi nhưng vẫn lưu hàng đợi.
4. SMTP lỗi: thử lại sau 1, 2, 4, 8 phút; sau lần thứ 5 chuyển Thất bại. Trưởng khoa xem `/admin/email-notifications.html` và chọn Gửi lại.
5. Thư đã gửi không được gửi lại bằng nút Gửi lại. Hàng đợi tồn tại qua lần khởi động lại nếu dùng cơ sở dữ liệu file/MySQL.

SMTP không bảo đảm gửi đúng một lần: nếu máy dừng sau khi SMTP nhận thư nhưng trước khi giao dịch ghi SENT hoàn tất, thư có thể được gửi lại. Trạng thái SENT có nghĩa máy chủ SMTP chấp nhận thư, không xác nhận sinh viên đã nhận/đọc thư.

## Cơ sở dữ liệu MySQL

Schema đầy đủ hiện có bảng `email_notifications`. Với DB hiện hành đã tương ứng V5, profile `mysql` tự baseline ở V5 và chạy migration V6 bằng Flyway. Bản migration chạy trong ứng dụng nằm tại `src/main/resources/db/migration/mysql/`; bản SQL tiện tra cứu nằm ở `database/migration/`. Các V2–V5 cũ vẫn được giữ làm lịch sử và không được chạy lại tự động trên schema đầy đủ. DB cũ chưa đạt V5 cần áp dụng đúng các nâng cấp còn thiếu trước khi chạy bản mới. Profile demo/test H2 dùng Hibernate và tắt Flyway.

Kiểm thử tự động sử dụng MailSender giả lập, không gửi email ra ngoài. Việc gửi thực tế qua Brevo cần bạn cấu hình tài khoản và biến môi trường trên máy chạy.
