# Báo cáo kiểm tra

Ngày cập nhật: 24/09/2026.

## Phạm vi

- Compile và test Java/Spring.
- Luồng quản trị, đề tài, nhóm sinh viên, báo cáo, hội đồng và kết quả.
- Phân quyền, CSRF, khóa tài khoản, giới hạn đăng nhập sai.
- Cú pháp JavaScript và khởi động ứng dụng H2.
- Đối chiếu schema MySQL với toàn bộ entity JPA.
- Kiểm tra giao diện desktop/mobile cho các vai trò chính.

## Lệnh kiểm tra

```powershell
mvn -B clean test
mvn -B package -DskipTests
```

Kiểm tra JavaScript:

```powershell
Get-ChildItem -Recurse -Filter *.js src/main/resources/static |
  ForEach-Object { node --check $_.FullName }
```

Kết quả gần nhất: build JAR thành công; `37/37` kiểm thử tự động đạt, không có lỗi hoặc kiểm thử bị bỏ qua; toàn bộ JavaScript vượt qua kiểm tra cú pháp. Giao diện đã được mở trực tiếp với dữ liệu H2 cho các khu vực trang chủ, đăng nhập, quản trị, giảng viên, sinh viên và hội đồng, bao gồm breakpoint màn hình nhỏ.

## Các tình huống quan trọng

- API ẩn danh trả 401; giảng viên không truy cập quản trị.
- Request thay đổi dữ liệu không có CSRF bị chặn.
- Phiên cũ mất quyền ngay khi tài khoản bị khóa hoặc đổi vai trò.
- Thứ tự thời gian đợt đăng ký và quy tắc COURSE/NCKH/TLCN/KLTN.
- Sinh viên chỉ thuộc một nhóm, nhóm tối đa ba thành viên, chỉ nhóm trưởng đăng ký/nộp báo cáo.
- Nhiều nhóm được phép đăng ký cùng đề tài.
- GVPB phải có vai trò phản biện; GVHD không được chấm đề tài mình.
- Đủ phiếu mới được Chủ tịch tổng hợp; chỉ Trưởng khoa công bố.
- Sinh viên ngoài nhóm không xem được báo cáo hoặc kết quả.

## Giới hạn kiểm thử

- MySQL cần tài khoản hợp lệ để chạy integration test trực tiếp; schema đã được đồng bộ thủ công với entity và profile dùng `validate` khi khởi động.
- Chưa có kiểm thử tải lớn, pentest độc lập hoặc dịch vụ quét malware.
- Rate limit hiện theo từng instance; triển khai cụm cần kho đếm dùng chung.
