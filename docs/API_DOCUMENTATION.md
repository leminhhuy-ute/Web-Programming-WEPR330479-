# Tài liệu REST API: Hệ thống Quản lý Đề tài Sinh viên HCM-UTE

## 1. Tổng quan & Quy ước kỹ thuật

Hệ thống cung cấp giao diện lập trình ứng dụng RESTful an toàn qua tiền tố `/api/**`. Toàn bộ dữ liệu trao đổi sử dụng định dạng JSON chuẩn và phản hồi theo các mã trạng thái HTTP tiêu chuẩn.

### 1.1 Địa chỉ cơ sở & Tiêu đề bảo mật
- **Địa chỉ cơ sở (Base URL)**: `http://localhost:8080/api` (hoặc domain/port được cấu hình triển khai)
- **Định dạng dữ liệu**: `application/json` (hoặc `multipart/form-data` đối với API nộp tệp báo cáo)
- **Token chống CSRF**: Các yêu cầu thay đổi trạng thái (POST, PUT, DELETE) bắt buộc gửi kèm token CSRF qua tiêu đề HTTP `X-XSRF-TOKEN` hoặc trường form `_csrf`.
- **Cookie phiên làm việc**: `JSESSIONID` (HttpOnly, SameSite=Lax).

### 1.2 Cấu trúc phản hồi chuẩn (Standard JSON Response)
```json
{
  "success": true,
  "message": "Mô tả kết quả thực hiện / Thông báo người dùng",
  "data": { ... }
}
```

### 1.3 Bảng mã trạng thái HTTP tiêu chuẩn
| Mã HTTP | Ý nghĩa | Tình huống kích hoạt |
| :--- | :--- | :--- |
| `200 OK` | Yêu cầu thành công | Thực hiện thành công các thao tác GET, PUT, POST |
| `201 Created` | Đã khởi tạo tài nguyên | Khởi tạo thành công đợt đăng ký, tài khoản, v.v. |
| `400 Bad Request` | Dữ liệu không hợp lệ | Thiếu trường bắt buộc, sai mốc thời gian, vi phạm quy tắc validation |
| `401 Unauthorized` | Chưa xác thực | Phiên đăng nhập hết hạn hoặc chưa cung cấp cookie phiên |
| `403 Forbidden` | Không có quyền | Truy cập trái vai trò hoặc can thiệp đề tài sai bộ môn |
| `404 Not Found` | Không tìm thấy | Không tồn tại ID đối tượng hoặc đường dẫn không hợp lệ |
| `409 Conflict` | Vi phạm quy tắc nghiệp vụ | Trùng tên đăng nhập/email, xóa đợt có đề tài, hủy đăng ký khi đã nộp báo cáo |
| `500 Internal Error` | Lỗi máy chủ nội bộ | Ngoại lệ hệ thống chưa được kiểm soát |

---

## 2. Nhóm API Xác thực & Phiên làm việc (`/api/auth`)

### 2.1 Đăng nhập hệ thống
- **Endpoint**: `POST /api/auth/login`
- **Quyền truy cập**: Công khai (Public)
- **Payload yêu cầu**:
  ```json
  {
    "username": "dean01",
    "password": "Demo@12345"
  }
  ```
- **Phản hồi thành công** (`200 OK`):
  ```json
  {
    "success": true,
    "message": "Đăng nhập thành công.",
    "data": {
      "id": 1,
      "userCode": "GV0001",
      "username": "dean01",
      "fullName": "PGS. TS. Nguyễn Văn Thành",
      "email": "dean01@hcmute.edu.vn",
      "role": "DEAN",
      "departmentId": 1
    }
  }
  ```

### 2.2 Đăng xuất hệ thống
- **Endpoint**: `POST /api/auth/logout`
- **Quyền truy cập**: Người dùng đã xác thực
- **Phản hồi** (`200 OK`): Hủy phiên làm việc và xóa cookie `JSESSIONID`.

### 2.3 Lấy thông tin tài khoản hiện hành
- **Endpoint**: `GET /api/auth/me`
- **Quyền truy cập**: Người dùng đã xác thực
- **Phản hồi** (`200 OK`): Trả về thông tin chi tiết của đối tượng `CurrentUser`.

### 2.4 Khởi tạo Token CSRF
- **Endpoint**: `GET /api/auth/csrf`
- **Quyền truy cập**: Công khai / Đã xác thực

---

## 3. Nhóm API Quản trị Người dùng (`/api/users`)

*Thẩm quyền: Chỉ dành cho `ROLE_DEAN`.*

| Phương thức | Endpoint | Mô tả chức năng |
| :--- | :--- | :--- |
| `GET` | `/api/users` | Lấy danh sách tài khoản kèm bộ lọc `keyword`, `role`, `departmentId`, `status`. |
| `GET` | `/api/users/{id}` | Lấy thông tin chi tiết của một tài khoản theo ID. |
| `POST` | `/api/users` | Tạo tài khoản mới. Kiểm tra trùng lặp không phân biệt hoa thường và bắt buộc bộ môn cho giảng viên. |
| `PUT` | `/api/users/{id}` | Cập nhật thông tin tài khoản (họ tên, email, bộ môn). |
| `PUT` | `/api/users/{id}/status` | Đổi trạng thái tài khoản (`ACTIVE`, `INACTIVE`, `LOCKED`). |
| `PUT` | `/api/users/{id}/password` | Đặt lại mật khẩu tài khoản (8–72 ký tự) và ghi nhận thời điểm đổi để hủy phiên cũ. |

---

## 4. Nhóm API Quản trị Đợt đăng ký (`/api/admin/registration-periods`)

*Thẩm quyền: Chỉ dành cho `ROLE_DEAN`.*

| Phương thức | Endpoint | Mô tả chức năng |
| :--- | :--- | :--- |
| `GET` | `/api/admin/registration-periods` | Lấy danh sách các đợt đăng ký kèm bộ lọc `keyword` và `type`. |
| `GET` | `/api/admin/registration-periods/{id}` | Lấy thông tin chi tiết của một đợt đăng ký. |
| `POST` | `/api/admin/registration-periods` | Khởi tạo đợt đăng ký mới. Kiểm tra tính hợp lệ của 4 mốc thời gian. |
| `PUT` | `/api/admin/registration-periods/{id}` | Cập nhật thông tin đợt. Chặn đổi loại đợt nếu đã có đề tài phát sinh. |
| `DELETE` | `/api/admin/registration-periods/{id}` | Xóa đợt đăng ký. Trả về `409 Conflict` nếu đợt đã có đề tài hoặc nhóm sinh viên. |

---

## 5. Nhóm API Quản lý Đề tài (`/api/topics` & `/api/department/topics`)

| Phương thức | Endpoint | Quyền truy cập | Mô tả chức năng |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/topics` | Đã xác thực | Danh sách đề tài lọc theo đợt, bộ môn, hình thức và trạng thái. |
| `GET` | `/api/topics/{id}` | Đã xác thực | Xem chi tiết nội dung và yêu cầu của đề tài. |
| `POST` | `/api/topics` | `LECTURER`, `DEAN` | Đề xuất đề tài mới. Trưởng khoa có quyền đề xuất liên bộ môn. |
| `PUT` | `/api/topics/{id}` | `LECTURER`, `DEAN` | Chỉnh sửa đề tài trong cửa sổ thời gian đề xuất của giảng viên. |
| `DELETE` | `/api/topics/{id}` | `LECTURER`, `DEAN` | Xóa đề tài đề xuất khi chưa được phê duyệt. |
| `POST` | `/api/department/topics/{id}/approve` | `HEAD_OF_DEPT` | Duyệt đề tài bộ môn và phân công 1–2 GVHD. |
| `POST` | `/api/department/topics/{id}/reject` | `HEAD_OF_DEPT` | Từ chối đề tài kèm lý do phản hồi. |

---

## 6. Nhóm API Nhóm sinh viên & Báo cáo tiến độ (`/api/student`)

*Thẩm quyền: Dành riêng cho `ROLE_STUDENT`.*

### 6.1 Quản lý Nhóm sinh viên
| Phương thức | Endpoint | Mô tả chức năng |
| :--- | :--- | :--- |
| `GET` | `/api/student/me` | Lấy trạng thái tổng hợp (thông tin cá nhân, nhóm hiện tại, đăng ký hiện tại, lời mời). |
| `POST` | `/api/student/groups` | Tạo nhóm sinh viên mới, người tạo là Nhóm trưởng. Body: `{"name": "Nhóm Kỹ thuật 01", "periodId": 2}`; `periodId` tùy chọn để tương thích client cũ. |
| `POST` | `/api/student/groups/invitations` | Nhóm trưởng gửi lời mời thành viên qua MSSV. Body: `{"studentId": "22110002"}` |
| `POST` | `/api/student/invitations/{id}/response` | Thành viên phản hồi lời mời gia nhập nhóm. Body: `{"accept": true}` |
| `POST` | `/api/student/groups/leader` | Nhóm trưởng chuyển quyền lãnh đạo cho thành viên khác. Body: `{"studentId": "22110002"}` |
| `POST` | `/api/student/groups/leave` | Thành viên rời nhóm (chỉ cho phép khi chưa đăng ký đề tài). |
| `POST` | `/api/student/groups/members/remove` | Nhóm trưởng xóa thành viên khỏi nhóm (chỉ cho phép khi chưa đăng ký đề tài). Body: `{"studentId": "22110002"}` |
| `POST` | `/api/student/groups/disband` | Nhóm trưởng giải tán nhóm (chỉ cho phép khi chưa đăng ký đề tài). |

### 6.2 Đăng ký Đề tài & Nộp Báo cáo
| Phương thức | Endpoint | Mô tả chức năng |
| :--- | :--- | :--- |
| `GET` | `/api/student/topics` | Tra cứu danh mục đề tài đã công bố để sinh viên lựa chọn. |
| `GET` | `/api/student/topics/page` | Tra cứu đề tài phân trang kèm từ khóa, bộ môn, loại hình. |
| `POST` | `/api/student/registrations` | Nhóm trưởng gửi yêu cầu đăng ký đề tài. Body: `{"topicId": "1"}` |
| `POST` | `/api/student/registrations/cancel` | Hủy đăng ký đề tài. Bị chặn (`409`) nếu đã có báo cáo hoặc lịch bảo vệ. Body: `{"note": "Lý do hủy"}` |
| `POST` | `/api/student/reports` | Nộp tệp báo cáo tiến độ đa phần (multipart: `stage`, `note`, `file`). |
| `GET` | `/api/student/reports/{id}/download` | Tải về tệp báo cáo đính kèm đã nộp. |
| `GET` | `/api/student/result?periodId={id}` | Tra cứu bảng điểm và nhận xét của hội đồng bảo vệ. Hỗ trợ chọn đợt qua `periodId`. |

---

## 7. Nhóm API Quản lý Hội đồng & Đánh giá bảo vệ (`/api/councils`)

*Thẩm quyền: `ROLE_DEAN`, `ROLE_HEAD_OF_DEPT`, `ROLE_LECTURER`.*

| Phương thức | Endpoint | Thẩm quyền | Mô tả chức năng |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/councils` | Giảng viên | Lấy danh sách hội đồng, danh sách đề tài bảo vệ và nhóm đủ điều kiện. |
| `GET` | `/api/councils/page` | Giảng viên | Lấy danh sách hội đồng phân trang. |
| `POST` | `/api/councils` | `DEAN` | Thành lập hội đồng bảo vệ (3–5 thành viên: Chủ tịch, Thư ký, Phản biện, Ủy viên). |
| `PUT` | `/api/councils/{id}` | `DEAN` | Cập nhật thông tin hội đồng và điều chỉnh thành viên tại chỗ. |
| `DELETE` | `/api/councils/{id}` | `DEAN` | Xóa hội đồng. Bị chặn (`409`) nếu đã phân công nhóm bảo vệ. |
| `POST` | `/api/councils/assignments` | `DEAN` | Phân công nhóm sinh viên cho hội đồng và chỉ định phản biện. Chặn nếu xung đột lợi ích GVHD. |
| `POST` | `/api/councils/defenses/{id}/grade` | Thành viên HĐ | Nhập điểm đánh giá (0.00–10.00) và nhận xét cá nhân. |
| `POST` | `/api/councils/defenses/{id}/finalize` | `CHAIRPERSON` | Chủ tịch tổng hợp điểm trung bình cộng khi toàn bộ thành viên đã nhập điểm. |
| `POST` | `/api/councils/defenses/{id}/publish` | `DEAN` | Trưởng khoa chính thức phê duyệt công bố điểm cho sinh viên. |


## 8. API chọn đợt, xuất kết quả và hàng đợi email (bổ sung 09/10/2026)

| Phương thức | Endpoint | Quyền | Hành vi |
| --- | --- | --- | --- |
| GET | `/api/student/periods` | STUDENT | Các đợt để tạo nhóm, kèm `open` và `joined`. |
| GET | `/api/student/result-periods` | STUDENT | Chỉ các đợt sinh viên đã tham gia nhóm. |
| GET | `/api/student/result.pdf?periodId=2` | STUDENT | PDF kết quả đã công bố của chính sinh viên. Đợt không tham gia hoặc chưa công bố trả 400. Không truyền đợt dùng cùng cách chọn như API kết quả hiện tại. |
| GET | `/api/admin/exports/results.xlsx?periodId=2` | DEAN | Excel kết quả đã công bố; mỗi sinh viên một dòng, điểm kiểu số. |
| GET | `/api/admin/exports/results.pdf?periodId=2` | DEAN | PDF kết quả đã công bố theo đợt. |
| GET | `/api/admin/email-notifications?page=0&size=20&status=PENDING` | DEAN | `{enabled,page}` trong `ApiResponse.data`. Có thể bỏ `status`; kích thước trang giới hạn 100. |
| POST | `/api/admin/email-notifications/{id}/retry` | DEAN | Chỉ đưa thư FAILED vào hàng đợi; SENT/PENDING trả 400. Không gửi thư trực tiếp trong request. |

Xuất kết quả quản trị bỏ `periodId` để lấy tất cả đợt; ID không tồn tại trả 400. Tệp tải về có `Content-Disposition: attachment` và `Cache-Control: no-store`.

Công bố điểm giờ tạo hàng đợi email trong cùng giao dịch; công bố lại không tạo thư trùng. SMTP gửi ở tác vụ nền khi `MAIL_ENABLED=true`. Xem [SMTP_SETUP.md](SMTP_SETUP.md).

Đăng ký đề tài khác đợt của nhóm trả 409. Sửa hội đồng đã phân công sẽ từ chối đưa GVHD vào hội đồng, bỏ/thay vai trò phản biện đã phân công hoặc đổi ngày không khớp ngày bảo vệ của đợt. Các kiểm tra quyền tại service theo người sở hữu, bộ môn và vai trò hội đồng tiếp tục áp dụng cùng `@PreAuthorize` tại API.
