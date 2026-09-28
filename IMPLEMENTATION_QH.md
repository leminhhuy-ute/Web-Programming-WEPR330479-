# Kiến trúc và quy tắc triển khai

## Kiến trúc

```text
Browser
  → HTML/CSS/JavaScript + Fetch API
  → Spring MVC REST/Thymeleaf
  → Service + policy nghiệp vụ
  → Spring Data JPA/Hibernate
  → H2 demo hoặc MySQL
```

Ứng dụng có một điểm khởi động `TopicManagementApplication`, một security filter chain và được đóng gói thành executable JAR.

## Phân quyền

| Khu vực | Vai trò |
| --- | --- |
| `/admin/**`, `/api/admin/**` | `DEAN` |
| `/lecturer/**`, `/api/lecturer/**` | `DEAN`, `HEAD_OF_DEPT`, `LECTURER` |
| `/councils/**`, `/api/councils/**` | `DEAN`, `HEAD_OF_DEPT`, `LECTURER` |
| `/student`, `/api/student/**` | `STUDENT` |

Service tiếp tục kiểm tra bộ môn, quyền GVHD, thành viên hội đồng và vai trò Chủ tịch/GVPB; không chỉ dựa vào URL.

## Vòng đời đợt đăng ký

```text
GV bắt đầu < GV kết thúc < SV bắt đầu < SV kết thúc
```

- `COURSE`, `NCKH`: không dùng hạn GVPB hoặc ngày hội đồng.
- `TLCN`: bắt buộc hạn GVPB sau khi SV kết thúc.
- `KLTN`: bắt buộc hạn GVPB và ngày hội đồng sau hạn GVPB.
- Đề tài được tạo/sửa trong cửa sổ GV.
- Bộ môn không được thay đổi phê duyệt từ lúc cửa sổ SV bắt đầu.
- Nhóm đăng ký trong cửa sổ SV.

## Ràng buộc dữ liệu chính

- Một đề tài thuộc đúng một bộ môn và một đợt, có 1–2 GVHD sau khi công bố.
- Nhóm có 1–3 sinh viên, đúng một nhóm trưởng; sinh viên chỉ thuộc một nhóm.
- Một nhóm giữ một đăng ký; nhiều nhóm có thể chọn cùng đề tài.
- Hội đồng có 3–5 thành viên, đúng một Chủ tịch, một Thư ký, ít nhất một Phản biện.
- Hội đồng chấm không chứa GVHD; GVPB được chọn từ thành viên mang vai trò `REVIEWER`.
- Chủ tịch chỉ tổng hợp khi đủ điểm của mọi thành viên; điểm cuối là trung bình cộng làm tròn hai chữ số.

## Cơ sở dữ liệu

- Demo mặc định: H2 file, Hibernate `update` để khởi chạy nhanh.
- MySQL: `database/schema.sql` là schema đầy đủ; `database/seed.sql` chỉ dùng local/demo.
- Profile `mysql` dùng `ddl-auto=validate`, không tự sửa production schema.
- Tên database chuẩn: `student_topic_management`.

## Bảo mật

- BCrypt, CSRF, session ID rotation và cookie HttpOnly/SameSite.
- Cookie Secure mặc định bật trong profile MySQL.
- Kiểm tra lại trạng thái và vai trò tài khoản trên mỗi request đã xác thực.
- Giới hạn đăng nhập sai tại `LoginAttemptService`.
- Upload chỉ nhận PDF có signature hoặc DOCX có cấu trúc bắt buộc, tối đa 10 MB.

## Giao diện

Các khu vực dùng chung `assets/css/style.css` làm design token: Be Vietnam Pro, màu HCM-UTE, radius, border, shadow, button và form. `admin.css`, `lecturer.css` và `workspace.css` chỉ bổ sung layout theo vai trò.

## Build

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd clean package
java -jar .\target\topic-management-1.0.0.jar
```
