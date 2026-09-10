# Ghép module Sinh viên & nhóm sinh viên vào đồ án

Module dùng Java 21, Spring Boot 3.5.16 / Spring Framework 6.2, Spring MVC, JPA và Spring Security. Không phụ thuộc frontend React hoặc dịch vụ Node.

## Ranh giới trách nhiệm

Module này xử lý tạo nhóm, gửi/nhận lời mời, chọn nhóm trưởng, tra cứu đề tài, đăng ký đề tài, xem trạng thái phê duyệt, xem đề tài đang làm, nộp và tải báo cáo. Chỉ có giao diện của sinh viên.

Tài khoản đăng nhập và dữ liệu đề tài mẫu giúp chạy độc lập. Khi ghép nhóm, dùng tài khoản, đợt đăng ký và danh mục đề tài do các bạn phụ trách những phần đó cung cấp. Không có API sinh viên tự duyệt đề tài, tạo đợt, chỉnh giảng viên, chấm điểm hoặc quản trị tài khoản.

## Hợp đồng với các bạn trong nhóm

| Module cung cấp | Dữ liệu / trách nhiệm |
|---|---|
| Quản trị & tài khoản | Mã sinh viên duy nhất; họ tên, lớp; mật khẩu BCrypt hoặc cơ chế đăng nhập dùng chung; vai trò STUDENT. Principal.getName() phải trả về MSSV. |
| Quản trị đợt đăng ký | Thời gian SV được đăng ký và hạn báo cáo. Bản độc lập lưu snapshot opensAt/closesAt/reportDueAt trong Topic; khi ghép cần ánh xạ về đợt đăng ký thật. |
| Giảng viên & đề tài | Cấp Topic đã công bố: mã, tên, bộ môn, loại, mô tả, công nghệ, GVHD; capacity là số nhóm tối đa (khác 3 thành viên/nhóm). Giảng viên phê duyệt Registration. |
| Giảng viên phê duyệt | Chuyển PENDING thành APPROVED hoặc REJECTED và cập nhật feedback. Thực hiện dưới transaction và khóa bản đăng ký/nhóm tương ứng, kiểm tra quyền GV theo module của bạn phụ trách. |
| Tích hợp | Dùng SecurityFilterChain chung; giữ CSRF, session và phân quyền. Không bê thêm một cấu hình security xung đột vào ứng dụng chung. |

Trạng thái đăng ký: chưa có → PENDING → APPROVED hoặc REJECTED. Nhóm bị REJECTED được chọn lại và cập nhật bản đăng ký hiện có. Mỗi nhóm chỉ có một hàng đăng ký. Bản độc lập giới hạn một nhóm cho mỗi sinh viên trên toàn module, đúng đề bài hiện có; nếu nhóm thống nhất hỗ trợ nhiều đợt độc lập, cần thêm roundId cho membership/group/registration và điều chỉnh các unique constraint cùng lúc.

## API dùng session đăng nhập

Tất cả đường dẫn có tiền tố `/api/student`. Backend xác định người thao tác bằng Principal, không tin studentId truyền vào làm danh tính người gọi. `studentId` trong request chỉ là người được mời hoặc nhóm trưởng mới.

| Method | Đường dẫn | Nội dung |
|---|---|---|
| GET | /me | Sinh viên hiện tại, nhóm, lời mời, đăng ký, metadata báo cáo |
| GET | /topics?q=&department=&type= | Đề tài đã công bố, chỗ trống, trạng thái mở |
| POST | /groups | `{"name":"Nhóm Web"}` |
| POST | /groups/invitations | `{"studentId":"22110002"}` |
| POST | /invitations/{id}/response | `{"accept":true}` hoặc false |
| POST | /groups/leader | `{"studentId":"22110002"}` |
| POST | /registrations | `{"topicId":"SE26-001"}` |
| POST | /reports | multipart: file, stage (Đề cương/Giữa kỳ/Cuối kỳ), note |
| GET | /reports/{id}/download | Chỉ thành viên cùng nhóm được tải |

POST cần header CSRF lấy từ meta `_csrf_header` và `_csrf` của trang `/student`. Thiếu session: 401 cho API; thiếu CSRF hoặc quyền: 403; vi phạm quy tắc: 400; dữ liệu xung đột: 409; quá dung lượng: 413. Lỗi nghiệp vụ trả JSON `{ "message": "…" }`.

## Quy tắc và tính đồng thời

- `StudentService` là nơi chứa quy tắc nghiệp vụ, không đặt quy tắc chỉ trong JavaScript.
- Tạo nhóm khóa hàng Student để tránh hai request tạo hai nhóm cho cùng người.
- Nhận lời mời khóa Student và Group, kiểm tra lại nhóm hiện tại và giới hạn 3 thành viên trong transaction.
- Đăng ký khóa Group rồi Topic; tính số chỗ tính cả PENDING và APPROVED. Có kiểm thử hai nhóm tranh chỗ cuối cùng.
- Khi đã PENDING hoặc APPROVED, không thêm thành viên; vẫn được chuyển quyền nhóm trưởng trong nội bộ nhóm.
- Nộp báo cáo yêu cầu APPROVED và đúng nhóm trưởng. Tệp chỉ PDF/DOCX tối đa 10 MB, kiểm tra chữ ký PDF/cấu trúc DOCX. Kiểm tra định dạng không thay cho antivirus khi triển khai thật.
- Báo cáo lưu BLOB trong database để metadata và nội dung commit cùng một transaction, không lộ đường dẫn tệp công khai. Mỗi lần nộp là một bản mới.
- AOP ghi tên hành động thành công, không ghi mật khẩu/nội dung báo cáo. Đây là ví dụ logging cross-cutting, chưa phải kho audit chuyên dụng.

## Cơ sở dữ liệu

Mặc định H2 lưu file `data/student-module.mv.db`, không mất dữ liệu khi tắt ứng dụng. Profile demo chỉ seed nếu bảng sinh viên đang rỗng. Có sáu bảng `sv_student`, `sv_group`, `sv_invitation`, `sv_topic`, `sv_registration`, `sv_report`.

Có sẵn driver và cấu hình MySQL 8. Tạo database UTF-8 và áp dụng `docs/mysql-schema.sql` trên database rỗng, đặt `DB_URL`, `DB_USER`, `DB_PASSWORD`, rồi chạy `SPRING_PROFILES_ACTIVE=mysql`. Profile này validate schema và không tạo tài khoản demo. Để thử dữ liệu mẫu trên một database MySQL riêng, dùng `mysql,demo`. MySQL chưa được chạy thử trên máy này; kiểm thử tự động dùng H2. Khi ghép schema chung, ưu tiên chỉnh Entity và repository để ánh xạ bảng của nhóm thay vì tạo bảng trùng.

Thông tin GVHD trong bản độc lập là chuỗi hiển thị; danh sách 1–2 GVHD thật thuộc module giảng viên. Dữ liệu ngày trong demo được tính từ lần chạy đầu, không tự gia hạn khi khởi động lại. Phần lịch bảo vệ, điểm số, dashboard giảng viên/quản trị thuộc nhiệm vụ các bạn khác nên không thêm menu giả trong giao diện này.

## Tài liệu Spring để thuyết trình

- Dependency Injection: https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html
- Transaction: https://docs.spring.io/spring-framework/reference/data-access/transaction.html
- Spring Data JPA: https://spring.io/guides/gs/accessing-data-jpa/
- Spring MVC: https://docs.spring.io/spring-framework/reference/web/webmvc.html
- Spring Security: https://docs.spring.io/spring-security/reference/servlet/index.html
- AOP: https://docs.spring.io/spring-framework/reference/core/aop.html

Spring Batch phù hợp tác vụ nhập danh sách lớn hoặc xử lý hàng loạt. Module này chưa có yêu cầu đó, nên không thêm Batch chỉ để đủ tám ô trong slide.
