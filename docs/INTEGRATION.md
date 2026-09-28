# Quyết định tích hợp module

## Mô hình dùng chung

Mọi module dùng chung các entity `User`, `Department`, `RegistrationPeriod`, `Topic`, `StudentGroup` và `GroupMember`. Không tồn tại kho tài khoản, đề tài hoặc nhóm riêng cho từng giao diện.

| Module | Trách nhiệm |
| --- | --- |
| Quản trị | Tài khoản, bộ môn, đợt đăng ký, thông báo, thống kê |
| Giảng viên | Đề xuất/duyệt đề tài, GVHD, duyệt nhóm |
| Sinh viên | Nhóm, lời mời, đăng ký, báo cáo, xem kết quả |
| Hội đồng | Thành viên, GVPB, phiếu điểm, tổng hợp, công bố |

## Hợp đồng tích hợp

- API xác thực dùng HTTP Session và trả `CurrentUser` thống nhất.
- Đề tài `APPROVED` là đề tài đã công bố cho sinh viên.
- Phê duyệt đề tài kết thúc trước thời điểm sinh viên bắt đầu đăng ký.
- Một đề tài không bị giữ độc quyền bởi nhóm đầu tiên; mỗi nhóm vẫn chỉ có một đăng ký đang hoạt động.
- Danh sách thành viên nhóm khóa sau khi gửi đăng ký.
- Báo cáo lưu theo từng lần nộp và chỉ nhóm trưởng được tạo bản mới.
- `Defense` nối nhóm với hội đồng; `Grade` lấy người chấm từ phiên đăng nhập, không tin `evaluatorId` từ client.
- Kết quả chỉ trả cho thành viên đúng nhóm sau khi được công bố.

## Quyết định triển khai

- Dùng Spring Boot executable JAR và Tomcat nhúng.
- H2 là chế độ demo mặc định; MySQL là profile triển khai.
- Không duy trì cấu hình WAR/Tomcat ngoài hoặc `legacy-tomcat`.
- `README.md` là tài liệu vận hành chuẩn duy nhất.

## Giới hạn có chủ đích

- Đề bài không cung cấp hạn nộp báo cáo sinh viên, nên hệ thống lưu lịch sử nhưng không gắn nhãn nộp muộn.
- Quy trình phúc khảo/mở khóa điểm chưa nằm trong yêu cầu.
- Quét malware production cần dịch vụ ngoài; kiểm tra hiện tại chỉ xác thực định dạng và giới hạn tài nguyên.
