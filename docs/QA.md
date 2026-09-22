# Báo cáo kiểm tra — 22/09/2026

Phạm vi: nhánh tong-hop-du-an, không phải main mới tại dda4b6d.

## Đã kiểm tra

- Bộ kiểm thử gồm 40 test (JUnit/Mockito/MockMvc), chạy trên H2 riêng biệt.
- Maven package thành công với JDK 21, tạo executable JAR.
- Khởi động Spring Boot với Tomcat nhúng và đọc các trang bằng trình duyệt thật.
- Đăng nhập giảng viên, tạo đề tài qua giao diện, xác nhận đề tài trong danh sách.
- Đăng nhập sinh viên, tạo nhóm qua hộp thoại và đăng xuất.
- Đăng nhập trưởng khoa, mở trang hội đồng, tạo hội đồng 3 người qua giao diện.
- Kiểm tra bố cục hội đồng/giảng viên ở viewport nhỏ: chiều rộng nội dung không vượt viewport.
- Trang chủ hiển thị ảnh trường nguyên tỷ lệ, không phủ chữ lên ảnh.
- Kiểm tra cú pháp JavaScript bằng node --check.

## Lỗi đã vá

- API chưa đăng nhập trả 302 thay vì 401.
- Tài khoản bị khóa hoặc đổi vai trò nhưng phiên cũ còn quyền.
- Truy cập bộ môn ngoài phiên dữ liệu dễ lỗi lazy loading.
- Sửa đề tài đã duyệt; truy cập nhóm không do mình hướng dẫn.
- Truy vấn nhóm bị mất kết quả khi GVHD thứ hai trống.
- Form cho phép 10 sinh viên thay vì tối đa 3.
- Lỗi hợp đồng JavaScript giữa giao diện quản trị và giảng viên.
- Escape nội dung khi đưa dữ liệu đề tài/nhóm vào HTML.
- Thông báo lỗi dữ liệu JSON sai định dạng và tài nguyên không tồn tại.
- Khóa cạnh tranh khi đăng ký nhóm/đề tài, optimistic version khi cập nhật.
- Không đổi GVHD sau khi phân công hội đồng; khóa cùng đề tài khi phân công.
- Nhãn trạng thái tiếng Việt, tương phản sidebar và responsive giảng viên.

## Kiểm thử xuyên module

Tạo nhóm → mời thành viên → đăng ký → GVHD duyệt → nhóm trưởng nộp PDF → thành viên tải báo cáo.

Phân công hội đồng → chặn người không có quyền/GVHD chấm → nhập điểm 3 thành viên → chỉ Chủ tịch tổng hợp → khoa công bố → chỉ nhóm liên quan xem kết quả.

## Chưa được chứng minh

- Chưa kiểm thử trên MySQL thật, tải lớn, hoặc triển khai Internet.
- Không phải kiểm toán bảo mật độc lập. Chưa có rate limiting đăng nhập, quét malware tài liệu hay quy trình phúc khảo.
- Kiểm tra PDF hiện nhận diện chữ ký định dạng; không thay thế bộ xác thực/quét nội dung đầy đủ.
- Kiểm thử tự động không có nghĩa mọi tình huống và mọi kích thước màn hình đều không còn lỗi.
- Chưa đánh giá hoặc merge bản main mới xuất hiện trong lúc tích hợp.
