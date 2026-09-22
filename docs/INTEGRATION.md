# Nguồn và quyết định tích hợp

## Nhánh đầu vào

| Module | Nhánh | Commit tham chiếu |
| --- | --- | --- |
| Quản trị | feature/quanghuy2603 | a507b7c |
| Giảng viên | feature-baohuy2603 | 0b142a5 |
| Sinh viên | sinhvien&nhomsinhvien | f505f3e |
| Hội đồng | feature/phan-bien-hoi-dong-cham-diem | 74fb122 |

Nhánh tổng hợp được tạo từ main tại 5f936d8. Các commit nguồn được giữ nguyên trên GitHub; không rebase hay force-push các nhánh của thành viên.

Ngày 22/09/2026, fetch lại thấy main đã tiến tới dda4b6d với một kiến trúc tích hợp khác (package vn.edu.hcmute.topicmanagement). Bản này dùng package topicmanagement. Không bật component-scan cả hai hoặc sao chép hai cấu trúc vào chung một ứng dụng: sẽ trùng nghiệp vụ, endpoint và bảng.

Người dùng đã chọn tiếp tục bản tong-hop-du-an đang kiểm thử. Main và các nhánh thành viên được giữ nguyên.

## Cách ghép

- Giữ mô hình User, Department, RegistrationPeriod từ quản trị; dùng Topic, StudentGroup, GroupMember của giảng viên.
- Chuyển giao diện tĩnh từ webapp vào resources/static để đóng gói JAR. Bản webapp trùng đã bỏ; có thể lấy lại từ các commit nguồn.
- Tái sử dụng trang sinh viên và kiểm tra tệp báo cáo; viết lại lớp kết nối với các entity chung thay vì tạo kho tài khoản/nhóm riêng.
- Tái sử dụng mô hình Council, CouncilMember, CouncilRole, CouncilStatus; thay Lecturer riêng bằng User chung.
- Defense gắn với nhóm; Grade lấy người chấm từ phiên đăng nhập, không nhận evaluatorId từ client.
- Điểm phải được Chủ tịch tổng hợp và khoa công bố trước khi trả về API sinh viên.

## Chính sách bổ sung cần nhóm thống nhất

- Bản demo hiện giữ tối đa một nhóm đang chờ/được duyệt cho một đề tài. Đây là quyết định triển khai, không phải ràng buộc được nêu rõ trong đề bài.
- Hội đồng được chọn không chứa bất kỳ GVHD nào của đề tài. Đây là cách phòng xung đột chấm điểm đơn giản; nếu muốn cho GVHD dự hội đồng nhưng không chấm cần mở rộng luật tổng hợp.
- reviewDeadline của đợt là hạn GVPB nộp điểm, không dùng làm hạn sinh viên nộp báo cáo. Hạn báo cáo sinh viên hiện hiển thị “Chưa thiết lập”.
- Không tự thay đổi điểm đã tổng hợp; quy trình mở khóa/phúc khảo chưa triển khai.
- Hồ sơ sinh viên hiển thị bộ môn; chưa có entity lớp học riêng.
