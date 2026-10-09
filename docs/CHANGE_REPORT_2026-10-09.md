# Những thay đổi so với dự án gốc – 09/10/2026

## Phạm vi và bản đối chiếu

- Bản gốc đối chiếu: commit `1ff6ab140e3188c7335a2a56911f067cf9549373` trên GitHub; 194 tệp được theo dõi của bản tải ban đầu khớp bản này sau khi bỏ khác biệt xuống dòng Windows/Linux.
- Thư mục dự án đã cập nhật: `D:\Web-Programming-WEPR330479--main\Web-Programming-WEPR330479--main`.
- Bản checkout có Git dùng để commit/push: `D:\Web-Programming-WEPR330479--main\github-work`.
- Repository: [Web-Programming-WEPR330479-](https://github.com/leminhhuy-ute/Web-Programming-WEPR330479-). [Xem toàn bộ diff từ bản gốc đến main](https://github.com/leminhhuy-ute/Web-Programming-WEPR330479-/compare/1ff6ab140e3188c7335a2a56911f067cf9549373...main).
- Thực hiện sửa hai lỗi đã xác nhận và cải tiến ba mục rubric: Kiến trúc; Chức năng cơ bản/nâng cao; Sáng tạo/mở rộng. Giữ quy tắc nhóm sinh viên **tối đa 3 người** theo đề bài bạn cung cấp.
- Theo yêu cầu của bạn, không chỉnh báo cáo Word và không kiểm tra/triển khai lại hosting. Tập trung mã nguồn, GitHub và bản chạy cục bộ.

## 1. Hai lỗi đã sửa

### Lỗi 1: Nhóm được tạo ở đợt A nhưng đăng ký đề tài của đợt B

**Trước:** Tạo nhóm tự chọn đợt đang mở gần nhất. Khi có hai đợt mở trùng thời gian, API chỉ kiểm tra cửa sổ đăng ký của đề tài, không so đợt của nhóm. Vì vậy nhóm/đề tài/thành viên có thể thuộc các đợt khác nhau; dữ liệu cũ còn có thể cho một sinh viên hai nhóm trong cùng đợt thực hiện đề tài.

**Sau:** `TopicRegistrationService` đối chiếu đợt nhóm, đề tài và từng thành viên; kiểm tra các nhóm khác của sinh viên bằng cả đợt thành viên, đợt nhóm và đợt đề tài thực tế. Sai đợt hoặc trùng nhóm trong đợt trả HTTP 409. Nhóm cũ chưa có đợt được gắn đợt đề tài cùng bản ghi thành viên khi đăng ký hợp lệ. Tra cứu kết quả dữ liệu cũ ưu tiên đợt thực tế của đề tài.

**Kiểm chứng:** Kiểm thử tích hợp bao phủ đợt trùng lịch, nhóm một sinh viên, dữ liệu cũ thiếu đợt và trùng thành viên. API cục bộ đã trả 409 cho kịch bản lỗi. Giao diện mới cho chọn đợt khi tạo nhóm và chỉ cho đăng ký đề tài cùng đợt nhóm.

### Lỗi 2: Sửa hội đồng đã phân công có thể đưa GVHD vào hội đồng

**Trước:** Phân công ban đầu chặn GVHD, nhưng sửa hội đồng sau phân công có thể đưa GVHD vào. Khi chấm điểm, GVHD lại bị chặn nên hội đồng không thể đủ điểm để tổng hợp. Người phản biện được phân công cũng có thể bị đổi vai trò/bỏ khỏi danh sách.

**Sau:** Khi sửa, hệ thống kiểm tra mọi đề tài đã phân công và toàn bộ danh sách thành viên đề xuất; chặn cả GVHD1 và GVHD2, giữ người phản biện được phân công đúng vai trò REVIEWER và kiểm tra ngày bảo vệ theo đợt. Sử dụng khóa cơ sở dữ liệu khi sửa/phân công/chấm điểm. Sửa phòng hợp lệ trước khi chấm điểm vẫn được phép.

**Kiểm chứng:** Các kiểm thử hồi quy chặn từng GVHD, đổi vai trò phản biện, ngày bảo vệ sai; chấp nhận sửa phòng hợp lệ. API cục bộ đã trả 409 khi thêm GVHD và 200 khi đổi phòng hợp lệ.

## 2. Các cải tiến đã hoàn thành

| Phần | So với bản gốc | Cách dùng / bằng chứng |
| --- | --- | --- |
| Chọn đợt tạo nhóm | Có danh sách đợt kèm trạng thái mở/đã tham gia; chọn đợt trực tiếp thay vì chỉ dùng đợt mới nhất. | Sinh viên → Nhóm → Tạo nhóm. API thêm `periodId` tùy chọn, vẫn tương thích client cũ. |
| Kết quả theo đợt | Có bộ chọn các đợt chính sinh viên đã tham gia; không trả điểm nhóm khác, đợt khác hoặc điểm chưa công bố. | Sinh viên → Kết quả. GET `/api/student/result-periods`. |
| Thông tin đề tài | Hiển thị đợt và số sinh viên tối đa theo `maxStudents`; hạn báo cáo lấy từ mốc đợt. | Chi tiết đề tài và nhóm; sửa DTO trước đây trả capacity cố định 1. |
| Quản lý hội đồng | Thêm sửa và xóa trên giao diện, điền sẵn thành viên/vai trò/thời gian; xóa có xác nhận và tuân thủ ràng buộc server. | Trưởng khoa → Hội đồng & kết quả. |
| Email công bố điểm | Thêm hàng đợi bền vững trong CSDL, một thư mỗi sinh viên cho một kết quả, cùng giao dịch công bố. Worker gửi SMTP và thử lại có thời gian chờ; thư lỗi có thể được đưa vào hàng đợi lại. | Trưởng khoa → Email thông báo điểm. Mặc định tắt gửi, xem phần SMTP dưới đây. |
| Excel | Xuất bảng kết quả đã công bố theo đợt, mỗi sinh viên một dòng; mã SV là chuỗi, điểm là số, có bộ lọc và cố định hàng tiêu đề. Nội dung bắt đầu bằng `=` vẫn là chuỗi. | Trưởng khoa → Xuất Excel / PDF. Apache POI 5.5.1. |
| PDF | Xuất bảng kết quả quản trị và kết quả cá nhân có điểm thành phần/nhận xét. Nhúng Noto Sans có giấy phép OFL, hỗ trợ tiếng Việt, xuống dòng dài, phân trang và số trang. | Trưởng khoa → Xuất Excel / PDF; Sinh viên → Kết quả → Tải kết quả PDF. Apache PDFBox 3.0.8. |
| Kiến trúc/phân quyền | Bật method security, dùng `@PreAuthorize` tại các controller chính; giữ kiểm tra sở hữu, bộ môn, thành viên/chủ tịch hội đồng tại service. Sửa JSON 403 bị mất dấu tiếng Việt. | Kiểm thử gọi trực tiếp bean controller cũng bị chặn đúng quyền; API quản trị chặn sinh viên. |
| Migration | Thêm Flyway thật trong dependency và profile MySQL; baseline schema hiện hành V5 và chạy V6 tạo bảng email có unique/FK/index. H2 demo/test tiếp tục dùng Hibernate. | Kiểm thử migration H2 chế độ MySQL và CI MySQL 8.4; xem lưu ý schema bên dưới. |
| Tương thích MySQL | Sửa ánh xạ nhật ký dài và nội dung tệp báo cáo để kiểu JDBC khớp LONGTEXT/LONGBLOB trong schema, lỗi chỉ được phát hiện khi bật kiểm tra MySQL thật. | CI khởi động toàn bộ ứng dụng với `ddl-auto=validate`; đọc lại nhật ký dài có tiếng Việt. |
| Kiểm thử/CI | Từ 54 kiểm thử gốc lên 80 kiểm thử thường lệ và một kiểm tra MySQL riêng. Thêm GitHub Actions chạy Java 21, MySQL 8.4, kiểm tra JS và lưu kết quả build/test. | [CI đã đạt cho bản mã a99619b](https://github.com/leminhhuy-ute/Web-Programming-WEPR330479-/actions/runs/37881841569). |
| Tài liệu | Cập nhật README/API cho chức năng và cách chạy thực tế; thêm hướng dẫn SMTP cùng file tổng hợp này. | README, `docs/API_DOCUMENTATION.md`, `docs/SMTP_SETUP.md`. |

Các phần này bổ sung bằng chứng cho những yêu cầu rubric về Spring MVC/JPA/Security/Validation/IoC, phân quyền chi tiết, email từng sinh viên, tra cứu đúng đợt và xuất Excel/PDF. Không thể suy ra điểm tối đa chỉ từ mã nguồn: email qua nhà cung cấp thật chưa được cấu hình, còn phần trình bày/vấn đáp và báo cáo nằm ngoài lần chỉnh sửa này.

## 3. Kết quả kiểm chứng

### Kiểm thử tự động

- Trên máy: **80 đạt, 0 lỗi**; thống kê JUnit `Tests run: 81, Failures: 0, Errors: 0, Skipped: 1`. Bài bỏ qua là `MySqlMigrationSmokeTest` vì không cấu hình MySQL cục bộ.
- CI Java 21: `verify`, 80 bài thường lệ, đóng gói JAR và cú pháp 11 file JS đều đạt.
- CI MySQL 8.4: bài MySQL riêng đã đạt; Flyway V5 → V6, không chạy lại migration và schema Hibernate hợp lệ. Tổng cộng **81 bài kiểm thử khác nhau đã được chạy thành công qua hai môi trường**.
- Gửi SMTP cục bộ kiểm tra subject/body tiếng Việt; các kiểm thử khác bao phủ tắt gửi, thành công, không gửi lại thư SENT, chờ retry, thất bại sau 5 lần và gửi lại thư FAILED.
- Đọc lại Excel kiểm tra kiểu chuỗi/số và mã có số 0 đầu; đọc lại PDF kiểm tra tiếng Việt, nhận xét cuối cùng và số trang. PDF nhiều trang đã được render để kiểm tra bố cục.

### Kiểm tra ứng dụng chạy thật

19 kiểm tra API trên H2 in-memory riêng biệt đã đạt, gồm: từ chối đăng ký khác đợt; từ chối thêm GVHD vào hội đồng; sửa phòng hợp lệ; quyền công bố điểm; tổng hợp 8.00; công bố hai lần vẫn chỉ hai email cho nhóm hai sinh viên; Excel/PDF đúng đợt; không cho sinh viên ngoài nhóm tải kết quả. Không gửi email ra ngoài trong các kiểm tra này.

Đã kiểm tra bằng trình duyệt: bộ chọn đợt kết quả, sửa hội đồng, trang lọc xuất kết quả và nút tải Excel, trang hàng đợi email. Sau cập nhật, ứng dụng tại **http://localhost:8080** đã khởi động lại và kiểm tra đăng nhập, API quản trị, hội đồng, danh sách đợt và đợt kết quả sinh viên đều trả 200.

Dữ liệu H2 hiện có được giữ nguyên. Bản sao lưu trước nâng cấp nằm tại:
`D:\Web-Programming-WEPR330479--main\Web-Programming-WEPR330479--main\database\backups\pre-improvements-20261009\integrated-project.mv.db`.

Các bài kiểm tra ghi dữ liệu dùng CSDL in-memory riêng; không tự xóa hay sửa hàng loạt những bản ghi sai đã tạo trước khi vá lỗi. Nếu từng tạo dữ liệu sai trong lúc test bản cũ, cần xử lý bản ghi đó riêng; kiểm tra mới ngăn tạo thêm dữ liệu sai và tra cứu dữ liệu cũ theo đợt đề tài thực tế.

## 4. Commit đã push

Tất cả commit sau sửa danh tính đều dùng:
- Tên tác giả và người tạo: **Lê Minh Huy**.
- GitHub: **leminhhuy-ute**.
- Email liên kết tài khoản: `100986379+leminhhuy-ute@users.noreply.github.com`.

| Commit | Nội dung |
| --- | --- |
| `1a58418` | fix: chan dang ky khac dot va xung dot GVHD khi sua hoi dong |
| `c182567` | feat: chon dot tao nhom va tra cuu ket qua theo dot |
| `dc5fa8e` | feat: sua va xoa hoi dong truc tiep tren giao dien |
| `968e4fd` | feat: thong bao ket qua qua SMTP voi hang doi va thu lai |
| `3bbfc99` | feat: xuat bang diem Excel va PDF ket qua tieng Viet |
| `e97d4db` | refactor: phan quyen API bang PreAuthorize va sua JSON UTF-8 |
| `6eea042` | ci: kiem thu Java 21 va migration tren MySQL 8.4 |
| `a99619b` | fix: dong bo kieu du lieu nhat ky va bao cao voi MySQL |

File này và cập nhật README/API được commit riêng cuối cùng. Các hash cũ của ba commit đầu (`373a021`, `d197e02`, `e7bda53`) đã được thay bằng ba hash trong bảng; nội dung mã không đổi khi sửa danh tính.

Không còn commit có tác giả/người tạo Codex trong các nhánh/tag đã kiểm tra; GitHub xác nhận cả tác giả và người tạo của 9 commit mới là `leminhhuy-ute`. Riêng thống kê Contributors đang cập nhật không đồng nhất: có lần API chỉ trả hai tài khoản của nhóm, nhưng lần kiểm tra cuối vẫn trả lại dữ liệu cũ có Codex. Vì vậy chưa khẳng định Codex đã biến mất trên giao diện Contributors. [GitHub ghi nhận thống kê có thể cần khoảng 24 giờ](https://docs.github.com/en/repositories/viewing-activity-and-data-for-your-repository/viewing-a-projects-contributors#contributor-data-is-stale-after-history-changes); nếu vẫn sai sau thời gian này, chủ repo có thể yêu cầu GitHub Support kiểm tra. Không có thao tác xóa trực tiếp cache Contributors được tài liệu này cung cấp.

## 5. SMTP đề xuất và phần cần cấu hình

Đề xuất **Brevo**, gói miễn phí hiện có **300 email/ngày**, phù hợp để demo thông báo điểm của đề tài. [Thông tin gói](https://help.brevo.com/hc/en-us/articles/208589409-About-Brevo-s-pricing-plans), [SMTP chính thức](https://developers.brevo.com/docs/smtp-integration).

| Biến môi trường | Giá trị |
| --- | --- |
| `MAIL_ENABLED` | Mặc định `false`; chỉ bật `true` khi đã cấu hình gửi. |
| `SMTP_HOST` | `smtp-relay.brevo.com` |
| `SMTP_PORT` | `587` |
| `SMTP_USERNAME` | SMTP login trong tài khoản Brevo. |
| `SMTP_PASSWORD` | SMTP key của Brevo, khác API key. |
| `MAIL_FROM` | Địa chỉ người gửi đã xác minh trong Brevo. |
| `SMTP_AUTH`, `SMTP_STARTTLS` | Mặc định `true`. |

`.env.example` là mẫu tham khảo; Spring Boot không tự đọc `.env`. Thiết lập biến trong PowerShell hoặc môi trường chạy ứng dụng, rồi khởi động lại. Xem đầy đủ `docs/SMTP_SETUP.md`. Hiện tại chưa có tài khoản/khóa SMTP thực tế, nên thư chỉ được lưu ở trạng thái chờ; chức năng gửi thật sẽ hoạt động khi bạn cấu hình.

Worker lấy tối đa 20 thư/lượt, chạy cách nhau 30 giây; timeout kết nối/đọc/ghi 3 giây. Lỗi thử lại sau 1, 2, 4, 8 phút; thất bại ở lần thứ 5. SENT nghĩa SMTP đã chấp nhận thư, không xác nhận sinh viên đọc thư. SMTP có thể tạo thư trùng nếu máy dừng đúng lúc đã gửi nhưng chưa ghi trạng thái SENT.

MySQL đang dùng schema đầy đủ V5 được Flyway baseline V5 và chạy V6. CSDL cũ chưa đạt V5 phải nâng cấp đúng các SQL còn thiếu trước. Nạp `database/schema.sql` khi tạo DB mới; bản schema này đã có bảng email, V6 dùng IF NOT EXISTS. Hai bản SQL V6 (trong database và resource ứng dụng) hiện giống nhau.

## 6. Cách test nhanh bản đã cập nhật

1. Mở **http://localhost:8080/login.html**. Tài khoản demo trong README, mật khẩu `Demo@12345`.
2. Sinh viên: Tạo nhóm → chọn đợt còn mở chưa tham gia → kiểm tra tên đợt trên nhóm/đề tài. Thử đăng ký khác đợt bằng API sẽ trả 409; giao diện chặn nút cho trường hợp này.
3. Trưởng khoa: Hội đồng & kết quả → Sửa. Với hội đồng đã phân công, chọn GVHD vào danh sách sẽ bị từ chối; thay phòng hợp lệ trước khi chấm điểm vẫn lưu được.
4. Cho thành viên hợp lệ nhập điểm → Chủ tịch tổng hợp → Trưởng khoa công bố. Vào Email thông báo điểm để xem mỗi sinh viên có một thư chờ.
5. Trưởng khoa: Xuất Excel / PDF → chọn đợt → tải tệp. Chỉ các kết quả đã công bố xuất hiện.
6. Sinh viên trong nhóm đã được công bố: Kết quả → chọn đợt → tải PDF. Sinh viên ngoài nhóm không tải được kết quả đó.
7. Chạy bộ kiểm thử: `mvnw.cmd test`; đóng gói: `mvnw.cmd verify`. Dừng ứng dụng chạy trực tiếp từ JAR trước khi đóng gói lại chính tệp JAR đó trên Windows.

## 7. Toàn bộ tệp thay đổi so với bản gốc

Tổng: **68 tệp**, gồm **28 tệp mới**, **40 tệp sửa**. Ký hiệu `A` = thêm; `M` = sửa. Không bao gồm dữ liệu chạy, logs, target, thư kiểm thử đã tải hoặc file sao lưu.

```text
A  .env.example
A  .github/workflows/ci.yml
M  README.md
A  database/migration/V6__add_result_email_outbox.sql
M  database/schema.sql
M  docs/API_DOCUMENTATION.md
A  docs/CHANGE_REPORT_2026-10-09.md
A  docs/SMTP_SETUP.md
M  pom.xml
M  src/main/java/topicmanagement/TopicManagementApplication.java
M  src/main/java/topicmanagement/config/DemoData.java
M  src/main/java/topicmanagement/config/SecurityConfig.java
M  src/main/java/topicmanagement/controller/ApiAdminDashboardController.java
M  src/main/java/topicmanagement/controller/ApiAdvisorQuotaController.java
M  src/main/java/topicmanagement/controller/ApiAnnouncementController.java
M  src/main/java/topicmanagement/controller/ApiDepartmentController.java
M  src/main/java/topicmanagement/controller/ApiDepartmentTopicController.java
M  src/main/java/topicmanagement/controller/ApiRegistrationPeriodController.java
M  src/main/java/topicmanagement/controller/ApiStudentGroupController.java
M  src/main/java/topicmanagement/controller/ApiTopicController.java
M  src/main/java/topicmanagement/controller/ApiTopicRegistrationController.java
M  src/main/java/topicmanagement/controller/ApiUserController.java
M  src/main/java/topicmanagement/controller/ReportAccessController.java
M  src/main/java/topicmanagement/council/CouncilController.java
M  src/main/java/topicmanagement/council/CouncilManagementService.java
M  src/main/java/topicmanagement/council/CouncilRepository.java
M  src/main/java/topicmanagement/council/CouncilService.java
M  src/main/java/topicmanagement/council/DefenseRepository.java
M  src/main/java/topicmanagement/council/DefenseService.java
M  src/main/java/topicmanagement/council/GradeRepository.java
M  src/main/java/topicmanagement/council/GradeService.java
M  src/main/java/topicmanagement/dto/response/StudentStateResponse.java
M  src/main/java/topicmanagement/entity/AuditLog.java
A  src/main/java/topicmanagement/export/PdfReport.java
A  src/main/java/topicmanagement/export/ResultExportController.java
A  src/main/java/topicmanagement/export/ResultExportService.java
A  src/main/java/topicmanagement/notification/EmailDeliveryService.java
A  src/main/java/topicmanagement/notification/EmailNotification.java
A  src/main/java/topicmanagement/notification/EmailNotificationController.java
A  src/main/java/topicmanagement/notification/EmailNotificationRepository.java
A  src/main/java/topicmanagement/notification/EmailWorker.java
A  src/main/java/topicmanagement/notification/ResultNotificationService.java
M  src/main/java/topicmanagement/repository/GroupMemberRepository.java
M  src/main/java/topicmanagement/service/StudentResultService.java
M  src/main/java/topicmanagement/service/TopicRegistrationService.java
M  src/main/java/topicmanagement/student/Report.java
M  src/main/java/topicmanagement/student/StudentController.java
M  src/main/java/topicmanagement/student/StudentService.java
M  src/main/resources/application-mysql.properties
M  src/main/resources/application.properties
A  src/main/resources/db/migration/mysql/V6__add_result_email_outbox.sql
A  src/main/resources/fonts/NotoSans-Bold.ttf
A  src/main/resources/fonts/NotoSans-Regular.ttf
A  src/main/resources/fonts/OFL.txt
A  src/main/resources/static/admin/email-notifications.html
A  src/main/resources/static/admin/exports.html
M  src/main/resources/static/assets/js/admin-shell.js
A  src/main/resources/static/assets/js/email-notifications.js
A  src/main/resources/static/assets/js/result-exports.js
M  src/main/resources/static/councils/index.html
M  src/main/resources/static/js/app.js
M  src/main/resources/static/js/councils.js
A  src/test/java/topicmanagement/EmailDeliveryTest.java
A  src/test/java/topicmanagement/EmailMigrationTest.java
A  src/test/java/topicmanagement/LocalSmtpTest.java
A  src/test/java/topicmanagement/MySqlMigrationSmokeTest.java
A  src/test/java/topicmanagement/PeriodAndCouncilRegressionTest.java
A  src/test/java/topicmanagement/export/ExportRenderingTest.java
```
