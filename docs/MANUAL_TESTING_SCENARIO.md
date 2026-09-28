# TÀI LIỆU KỊCH BẢN KIỂM THỬ THỦ CÔNG TOÀN TRÌNH
## HỆ THỐNG QUẢN LÝ ĐỀ TÀI VÀ HỌC VỤ SINH VIÊN (HCMUTE STUDENT TOPIC MANAGEMENT SYSTEM)

---

## MỤC LỤC
1. [Tổng quan hệ thống (System Overview)](#1-tổng-quan-hệ-thống-system-overview)
2. [Chuẩn bị môi trường kiểm thử (Test Environment Preparation)](#2-chuẩn-bị-môi-trường-kiểm-thử-test-environment-preparation)
3. [Danh sách tài khoản kiểm thử (Test Accounts)](#3-danh-sách-tài-khoản-kiểm-thử-test-accounts)
4. [Kịch bản kiểm thử luồng Đợt đăng ký (Registration Period Workflow)](#4-kịch-bản-kiểm-thử-luồng-đợt-đăng-ký-registration-period-workflow)
5. [Kịch bản kiểm thử luồng Giảng viên Đề xuất đề tài (Lecturer Topic Workflow)](#5-kịch-bản-kiểm-thử-luồng-giảng-viên-đề-xuất-đề-tài-lecturer-topic-workflow)
6. [Kịch bản kiểm thử luồng Duyệt đề tài & Phân công GVHD (Topic Approval and Publishing Workflow)](#6-kịch-bản-kiểm-thử-luồng-duyệt-đề-tài--phân-công-gvhd-topic-approval-and-publishing-workflow)
7. [Kịch bản kiểm thử luồng Quản lý Nhóm sinh viên (Student Group Workflow)](#7-kịch-bản-kiểm-thử-luồng-quản-lý-nhóm-sinh-viên-student-group-workflow)
8. [Kịch bản kiểm thử luồng Đăng ký đề tài (Topic Registration Workflow)](#8-kịch-bản-kiểm-thử-luồng-đăng-ký-đề-tài-topic-registration-workflow)
9. [Kịch bản kiểm thử luồng Nộp Báo cáo tiến độ (Report Submission Workflow)](#9-kịch-bản-kiểm-thử-luồng-nộp-báo-cáo-tiến-độ-report-submission-workflow)
10. [Kịch bản kiểm thử luồng Hội đồng Đánh giá & Phân công (Council & Defense Workflow)](#10-kịch-bản-kiểm-thử-luồng-hội-đồng-đánh-giá--phân-công-council--defense-workflow)
11. [Kịch bản kiểm thử luồng Chấm điểm Bảo vệ (Grading Workflow)](#11-kịch-bản-kiểm-thử-luồng-chấm-điểm-bảo-vệ-grading-workflow)
12. [Kịch bản kiểm thử luồng Tổng hợp & Công bố kết quả (Finalize & Publish Workflow)](#12-kịch-bản-kiểm-thử-luồng-tổng-hợp--công-bố-kết-quả-finalize--publish-workflow)
13. [Danh mục kiểm thử Bảo mật & Phân quyền (Security & Authorization Checklist)](#13-danh-mục-kiểm-thử-bảo-mật--phân-quyền-security--authorization-checklist)
14. [Checklist kiểm tra trực tiếp Cơ sở dữ liệu (Database Verification Checklist)](#14-checklist-kiểm-tra-trực-tiếp-cơ-sở-dữ-liệu-database-verification-checklist)
15. [Kịch bản nghiệm thu toàn trình hoàn chỉnh (Complete End-to-End Acceptance Checklist)](#15-kịch-bản-nghiệm-thu-toàn-trình-hoàn-chỉnh-complete-end-to-end-acceptance-checklist)
16. [Mẫu phiếu ghi nhận lỗi chuẩn (Bug Recording Template)](#16-mẫu-phiếu-ghi-nhận-lỗi-chuẩn-bug-recording-template)

---

## 1. TỔNG QUAN HỆ THỐNG (SYSTEM OVERVIEW)

### 1.1. Mục đích hệ thống
Hệ thống Quản lý Đề tài Sinh viên (Student Topic Management System) được xây dựng nhằm chuẩn hóa và tự động hóa toàn bộ vòng đời quản lý đề tài học vụ tại Khoa Công nghệ Thông tin - Trường Đại học Sư phạm Kỹ thuật TP.HCM (HCMUTE), bao gồm:
* Khởi tạo và thiết lập các đợt đăng ký đề tài (Môn học, NCKH, Tiểu luận chuyên ngành, Khóa luận tốt nghiệp).
* Quy trình đề xuất, thẩm định và phê duyệt danh mục đề tài của giảng viên.
* Quy trình tạo nhóm sinh viên, mời thành viên, đăng ký đề tài và phân bổ hạn ngạch hướng dẫn.
* Quản lý tiến độ thực hiện đề tài thông qua các cột mốc nộp báo cáo (Đề cương, Giữa kỳ, Cuối kỳ).
* Thành lập hội đồng đánh giá bảo vệ, phân công phản biện, chấm điểm phân quyền, tổng hợp điểm tự động và công bố kết quả minh bạch tới sinh viên.

### 1.2. Các tác nhân và vai trò trong hệ thống (System Actors & Roles)
| Vai trò (Role Enum) | Tên vai trò | Phạm vi trách nhiệm chính |
| :--- | :--- | :--- |
| **`DEAN`** | Trưởng khoa | Quản trị cao nhất: Thiết lập đợt đăng ký, quản lý người dùng, bộ môn, duyệt đề tài toàn khoa, thành lập hội đồng bảo vệ, phân công hội đồng, tổng hợp và công bố kết quả chính thức. |
| **`HEAD_OF_DEPT`** | Trưởng bộ môn | Quản lý học thuật cấp Bộ môn: Thẩm định và phê duyệt đề tài của giảng viên trong bộ môn phụ trách, phân công Giảng viên hướng dẫn 1 (GVHD 1) và Giảng viên hướng dẫn 2 (GVHD 2). Tham gia hội đồng chấm điểm. |
| **`LECTURER`** | Giảng viên | Đề xuất đề tài nghiên cứu, theo dõi và phê duyệt/từ chối nhóm sinh viên đăng ký đề tài của mình, giám sát tiến độ và tải báo cáo định kỳ của nhóm, tham gia hội đồng chấm điểm với vai trò Chủ tịch, Thư ký, Phản biện hoặc Ủy viên. |
| **`STUDENT`** | Sinh viên | Tạo nhóm nghiên cứu (tối đa 3 thành viên), mời thành viên cùng lớp, đăng ký đề tài công bố trong đợt, nộp báo cáo tiến độ theo 3 giai đoạn bắt buộc, theo dõi phản hồi và tra cứu kết quả điểm đánh giá. |

### 1.3. Sơ đồ luồng nghiệp vụ khép kín (End-to-End Workflow Flow)

```
[ BƯỚC 1: TRƯỞNG KHOA (DEAN) ]
  └── Tạo Đợt đăng ký (Registration Period: lecturerStartAt -> studentEndAt -> defenseDate)
         │
         ▼
[ BƯỚC 2: GIẢNG VIÊN (LECTURER) ]
  └── Đề xuất Đề tài mới (Topic: PENDING) trong thời hạn của Giảng viên
         │
         ▼
[ BƯỚC 3: TRƯỞNG BỘ MÔN (HOD) / TRƯỞNG KHOA (DEAN) ]
  └── Thẩm định đề tài, Phân công GVHD1 & GVHD2 -> Phê duyệt (APPROVED) hoặc Từ chối (REJECTED)
         │
         ▼
[ BƯỚC 4: SINH VIÊN (STUDENT) ]
  ├── 4.1. Nhóm trưởng tạo Nhóm sinh viên (1/3 thành viên)
  ├── 4.2. Mời thành viên bằng MSSV -> Thành viên chấp nhận (Tối đa 3 thành viên)
  └── 4.3. Tìm kiếm trong Kho đề tài -> Đăng ký đề tài (Topic Registration: PENDING)
         │
         ▼
[ BƯỚC 5: GIẢNG VIÊN HƯỚNG DẪN (ADVISOR) ]
  └── Duyệt nhận nhóm sinh viên (APPROVED) hoặc Từ chối (REJECTED có lý do)
         │
         ▼
[ BƯỚC 6: NHÓM SINH VIÊN (STUDENT LEADER) ]
  └── Nộp báo cáo tuần tự: [Đề cương] ➔ [Giữa kỳ] ➔ [Cuối kỳ] (File PDF/DOCX <= 10MB)
         │
         ▼
[ BƯỚC 7: TRƯỞNG KHOA (DEAN) ]
  ├── 7.1. Thành lập Hội đồng đánh giá (3–5 thành viên: Chủ tịch, Thư ký, Phản biện, Ủy viên)
  └── 7.2. Phân công Nhóm đề tài & Giảng viên phản biện (GVPB không trùng GVHD)
         │
         ▼
[ BƯỚC 8: HỘI ĐỒNG BẢO VỆ (COUNCIL MEMBERS & REVIEWER) ]
  └── Từng thành viên hội đồng nhập điểm đánh giá (0.00 – 10.00) và nhận xét chi tiết
         │
         ▼
[ BƯỚC 9: TỔNG HỢP VÀ CÔNG BỐ (DEAN / CHAIRPERSON) ]
  ├── 9.1. Tổng hợp điểm hội đồng (Tính điểm trung bình cộng, khóa chỉnh sửa điểm)
  └── 9.2. Công bố kết quả chính thức (Published)
         │
         ▼
[ BƯỚC 10: SINH VIÊN (STUDENT) ]
  └── Tra cứu bảng điểm tổng kết chính thức và phiếu đánh giá của từng giảng viên
```

---

## 2. CHUẨN BỊ MÔI TRƯỜNG KIỂM THỬ (TEST ENVIRONMENT PREPARATION)

### 2.1. Yêu cầu hệ thống
* **Hệ điều hành:** Windows 10/11, macOS hoặc Linux.
* **JDK:** Java Development Kit phiên bản 21 trở lên (`java -version`).
* **Trình duyệt khuyến nghị:** Google Chrome, Microsoft Edge hoặc Mozilla Firefox bản mới nhất (có mở công cụ Developer Tools / F12 để kiểm tra Network & Console).
* **Công cụ hỗ trợ (tùy chọn):** Postman / cURL để kiểm thử trực tiếp các endpoint API.

### 2.2. Lệnh khởi chạy ứng dụng
Mở terminal tại thư mục gốc dự án: `D:\HocFrontEnd\Web-Programming-WEPR330479--main` và thực hiện:

```powershell
# Chạy với Maven Wrapper trên Windows PowerShell
.\mvnw.cmd spring-boot:run

# Hoặc chạy kiểm thử tự động toàn bộ trước khi test thủ công
.\mvnw.cmd clean test
```

### 2.3. Cấu hình Cơ sở dữ liệu
* **Mặc định (In-Memory H2 Database):** Ứng dụng tự động khởi tạo lược đồ bảng từ `schema.sql` và dữ liệu mẫu từ `seed.sql`. Dữ liệu sẽ tự làm mới về trạng thái ban đầu mỗi lần restart server.
  * Đường dẫn H2 Console (nếu bật): `http://localhost:8080/h2-console`
  * JDBC URL: `jdbc:h2:mem:topicdb`
* **Môi trường MySQL (Production/Staging):** Kích hoạt bằng cờ profile:
  ```powershell
  .\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mysql
  ```
  Tập lệnh khởi tạo: `database/schema.sql` và `database/seed.sql`.

### 2.4. Địa chỉ truy cập ứng dụng
* Cổng mặc định: `8080`
* Địa chỉ trang chủ: `http://localhost:8080/`
* Trang đăng nhập: `http://localhost:8080/login.html`
* Trang quản trị Hội đồng: `http://localhost:8080/councils/index.html`

> [!NOTE]
> Mọi tài khoản demo trong hệ thống đều sử dụng chung mật khẩu mặc định: **`Demo@12345`**.

---

## 3. DANH SÁCH TÀI KHOẢN KIỂM THỬ (TEST ACCOUNTS)

Dưới đây là danh mục tài khoản định sẵn trong `database/seed.sql` phục vụ kiểm thử:

| Username | Password | Tên hiển thị | Vai trò (Role) | Đơn vị / Bộ môn | Mục đích kiểm thử chính |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **`dean01`** | `Demo@12345` | PGS. TS. Nguyễn Văn Thành | **DEAN** | Toàn khoa CNTT | Quản lý đợt đăng ký, tạo hội đồng, phân công phản biện, tổng hợp & công bố kết quả. |
| **`hod01`** | `Demo@12345` | TS. Nguyễn Hoàng Long | **HEAD_OF_DEPT** | Công nghệ phần mềm (CNPM) | Duyệt đề tài bộ môn CNPM, phân công GVHD1/GVHD2, tham gia hội đồng chấm điểm. |
| **`lecturer01`** | `Demo@12345` | TS. Trần Hoàng Nam | **LECTURER** | Công nghệ phần mềm (CNPM) | Đề xuất đề tài, duyệt nhóm sinh viên, xem và tải báo cáo của nhóm đề tài `DT-CNPM-202602`. |
| **`lecturer02`** | `Demo@12345` | ThS. Đặng Thị Kim Ngân | **LECTURER** | Công nghệ phần mềm (CNPM) | Giảng viên có đề tài `DT-CNPM-202603` đang ở trạng thái `PENDING`. |
| **`lecturer03`** | `Demo@12345` | TS. Lê Văn Tuấn | **LECTURER** | Hệ thống thông tin (HTTT) | Đề xuất đề tài bộ môn HTTT, tham gia hội đồng bảo vệ. |
| **`lecturer04`** | `Demo@12345` | ThS. Phạm Ngọc Bích | **LECTURER** | Khoa học dữ liệu (KHDL) | Đề xuất đề tài KHDL, thành viên hội đồng bảo vệ (`MEMBER`). |
| **`lecturer05`** | `Demo@12345` | TS. Võ Minh Trí | **LECTURER** | Mạng máy tính & TT (MMT) | Giảng viên phản biện trong hội đồng `HD-CNTT-01` (`REVIEWER`). |
| **`student01`** | `Demo@12345` | Nguyễn Minh Tuấn | **STUDENT** | Lớp CNPM (MSSV: 22110001) | Nhóm trưởng nhóm `GRP-202601` đã đăng ký đề tài `DT-CNPM-202602`. Dùng kiểm thử nộp báo cáo và xem kết quả. |
| **`student02`** | `Demo@12345` | Trần Thu Hà | **STUDENT** | Lớp CNPM (MSSV: 22110002) | Thành viên nhóm `GRP-202601`. Dùng kiểm thử phân quyền thành viên (không được nộp báo cáo/đăng ký thay nhóm trưởng). |
| **`student03`** | `Demo@12345` | Lê Hoàng Nam | **STUDENT** | Lớp HTTT (MSSV: 22110003) | Sinh viên tự do (chưa có nhóm). **Dùng làm nhân vật chính kiểm thử tạo nhóm mới từ đầu**. |
| **`student04`** | `Demo@12345` | Phạm Ngọc Anh | **STUDENT** | Lớp HTTT (MSSV: 22110004) | Sinh viên tự do. Dùng nhận lời mời vào nhóm của `student03`. |
| **`student05`** | `Demo@12345` | Đỗ Quốc Bảo | **STUDENT** | Lớp MMT (MSSV: 22110005) | Sinh viên tự do. Dùng kiểm thử mời thành viên thứ 3. |
| **`student06`** | `Demo@12345` | Vũ Mai Phương | **STUDENT** | Lớp MMT (MSSV: 22110006) | Sinh viên tự do. Dùng kiểm thử trường hợp từ chối lời mời hoặc mời vượt số lượng. |

---

## 4. KỊCH BẢN KIỂM THỬ LUỒNG ĐỢT ĐĂNG KÝ (REGISTRATION PERIOD WORKFLOW)

### 4.1. Mục tiêu kiểm thử
Xác minh Trưởng khoa (`DEAN`) có thể thiết lập đợt đăng ký hợp lệ, tuân thủ đúng các ràng buộc thời gian (giảng viên đề xuất trước sinh viên đăng ký, hạn phản biện trước ngày bảo vệ) và kiểm tra việc ngăn chặn dữ liệu thời gian không hợp lệ.

### 4.2. Kịch bản Thành công (Positive / Happy Path: TC-PER-01)
* **Tài khoản thực hiện:** `dean01`
* **Đường dẫn UI:** `/admin/registration-periods/list.html` ➔ Bấm **"+ Thêm mới"** (`form.html`)
* **Endpoint API ngầm:** `POST /api/admin/registration-periods`

#### Các bước thực hiện:
1. Đăng nhập tài khoản `dean01` / `Demo@12345`.
2. Truy cập menu **"Đợt đăng ký"** trên thanh điều hướng bên trái (`/admin/registration-periods/list.html`).
3. Bấm nút **"+ Thêm mới"**.
4. Điền các trường thông tin:
   * **Tên đợt:** `Đợt Khóa luận tốt nghiệp K22 Học kỳ II 2026-2027`
   * **Loại đề tài:** Chọn `KLTN` (Khóa luận tốt nghiệp)
   * **Bắt đầu GV đề xuất:** Chọn thời điểm hôm nay trừ 1 ngày (VD: `2026-10-01 08:00`)
   * **Hạn GV đề xuất:** Chọn thời điểm sau 7 ngày (VD: `2026-10-08 17:00`)
   * **Bắt đầu SV đăng ký:** Chọn sau hạn GV (VD: `2026-10-09 08:00`)
   * **Hạn SV đăng ký:** Chọn thời điểm sau 14 ngày (VD: `2026-10-23 17:00`)
   * **Hạn nộp phản biện:** Chọn sau hạn SV (VD: `2026-11-10 17:00`)
   * **Ngày tổ chức bảo vệ:** Chọn sau ngày phản biện (VD: `2026-11-20`)
5. Bấm nút **"Lưu đợt đăng ký"**.

#### Kết quả mong đợi:
* Hệ thống hiển thị thông báo thành công và chuyển hướng về danh sách đợt đăng ký.
* Bảng danh sách xuất hiện đợt đăng ký mới tạo với trạng thái hoạt động.
* Cơ sở dữ liệu: Bản ghi mới xuất hiện trong bảng `registration_periods` với các trường ngày khớp chính xác.

---

### 4.3. Các kịch bản Biên & Thất bại (Negative Test Cases)

| Mã test case | Tình huống kiểm thử | Dữ liệu kiểm thử | Kết quả mong đợi |
| :--- | :--- | :--- | :--- |
| **TC-PER-NEG-01** | Hạn nộp GV đề xuất xảy ra trước ngày bắt đầu | `lecturerStartAt` = Ngày 10, `lecturerEndAt` = Ngày 05 | Báo lỗi validation: `lecturerStartAt phải trước lecturerEndAt`. Hệ thống không lưu vào CSDL. |
| **TC-PER-NEG-02** | Thời gian SV đăng ký chồng lấn với thời gian GV đề xuất | `studentStartAt` = Ngày 06 (trong khi `lecturerEndAt` = Ngày 08) | Báo lỗi validation: `studentStartAt phải sau lecturerEndAt`. |
| **TC-PER-NEG-03** | Đợt loại `KLTN` nhưng không nhập ngày bảo vệ hoặc hạn phản biện | Chọn `type = KLTN`, để trống `reviewDeadline` hoặc `defenseDate` | Báo lỗi ràng buộc nghiệp vụ: Đợt Khóa luận bắt buộc phải cấu hình hạn phản biện và ngày bảo vệ hội đồng. |
| **TC-PER-NEG-04** | Đợt loại `COURSE` nhưng cố tình nhập ngày bảo vệ | Chọn `type = COURSE`, nhập `defenseDate` | Hệ thống tự động xóa bỏ hoặc báo lỗi ràng buộc `ck_period_review`: Đợt môn học không có ngày bảo vệ hội đồng. |
| **TC-PER-NEG-05** | Giảng viên thường truy cập form tạo đợt | Đăng nhập `lecturer01`, truy cập trực tiếp URL `/admin/registration-periods/form.html` | Bị chặn quyền truy cập (HTTP 403 Forbidden hoặc chuyển hướng về trang chủ). |

---

## 5. KỊCH BẢN KIỂM THỬ LUỒNG GIẢNG VIÊN ĐỀ XUẤT ĐỀ TÀI (LECTURER TOPIC WORKFLOW)

### 5.1. Mục tiêu kiểm thử
Xác minh giảng viên (`LECTURER`) có thể đề xuất đề tài mới trong đợt đang mở tiếp nhận đề xuất, đề tài tạo xong có trạng thái `PENDING`, và giảng viên không thể sửa/xóa đề tài của người khác.

### 5.2. Kịch bản Thành công (Positive / Happy Path: TC-TOP-01)
* **Tài khoản thực hiện:** `lecturer01`
* **Đường dẫn UI:** `/lecturer/topics/list.html` ➔ Bấm **"+ Đề xuất đề tài"** (`form.html`)
* **Endpoint API ngầm:** `POST /api/lecturer/topics`

#### Các bước thực hiện:
1. Đăng nhập tài khoản `lecturer01` / `Demo@12345`.
2. Trên thanh điều hướng, chọn **"Quản lý đề tài"** (`/lecturer/topics/list.html`).
3. Bấm nút **"+ Đề xuất đề tài mới"**.
4. Điền các trường thông tin:
   * **Mã đề tài:** `DT-CNPM-202699` (Mã duy nhất chưa tồn tại)
   * **Tên đề tài:** `Nghiên cứu ứng dụng Kiến trúc Clean Architecture trong phát triển Spring Boot`
   * **Mô tả chi tiết:** `Nghiên cứu và triển khai hệ thống mẫu áp dụng Clean Architecture, Domain-Driven Design (DDD) trên nền Java 21 và Spring Boot 3.3.` (Độ dài > 10 ký tự)
   * **Yêu cầu kỹ thuật:** `Thành thạo Java Core, Spring Boot, thiết kế CSDL quan hệ MySQL.`
   * **Số sinh viên tối đa:** `3` (Nhập số từ 1 đến 3)
   * **Loại đề tài:** Chọn `Môn học` (`COURSE`)
   * **Bộ môn:** Chọn `Công nghệ phần mềm` (`CNPM`)
   * **Đợt đăng ký:** Chọn `Đợt đề xuất đề tài môn học Học kỳ I năm học 2026-2027` (Đợt đang mở cho GV)
5. Bấm nút **"Đề xuất đề tài"**.

#### Kết quả mong đợi:
* Thông báo *"Đề xuất đề tài mới thành công!"* xuất hiện.
* Hệ thống chuyển về danh sách đề tài. Đề tài `DT-CNPM-202699` hiển thị với badge trạng thái **`Chờ duyệt` (PENDING)**.
* CSDL: Bản ghi mới trong bảng `topics` có `status = 'PENDING'`, `created_by = (ID của lecturer01)`, `advisor1_id = (ID của lecturer01)`.

---

### 5.3. Các kịch bản Biên & Thất bại (Negative Test Cases)

| Mã test case | Tình huống kiểm thử | Thao tác / Dữ liệu kiểm thử | Kết quả mong đợi |
| :--- | :--- | :--- | :--- |
| **TC-TOP-NEG-01** | Trùng mã đề tài đã có trong hệ thống | Nhập mã `DT-CNPM-202601` (đã có trong seed data) | Báo lỗi: Mã đề tài đã tồn tại. Không cho phép lưu. |
| **TC-TOP-NEG-02** | Mô tả đề tài quá ngắn (< 10 ký tự) | Nhập Mô tả: `App web` | Báo lỗi validation: `Mô tả đề tài phải có ít nhất 10 ký tự`. |
| **TC-TOP-NEG-03** | Số lượng sinh viên không hợp lệ | Nhập `maxStudents = 0` hoặc `maxStudents = 4` | Form chặn nhập hoặc API trả lỗi: Số lượng sinh viên phải từ 1 đến 3. |
| **TC-TOP-NEG-04** | Đề xuất vào đợt đã đóng nhận đề tài | Chọn đợt `Đợt bảo vệ Khóa luận...` (đã qua `lecturerEndAt`) | API từ chối với thông báo: Đợt đăng ký hiện không mở cho giảng viên đề xuất. |
| **TC-TOP-NEG-05** | Giảng viên sửa đề tài đã được duyệt | Đăng nhập `lecturer01`, mở form sửa đề tài `DT-CNPM-202601` (đã `APPROVED`) | Nút "Sửa" bị ẩn; nếu gửi API `PUT /api/lecturer/topics/{id}`, hệ thống báo lỗi không cho phép sửa đề tài đã duyệt. |
| **TC-TOP-NEG-06** | IDOR: Giảng viên A cố xóa đề tài của Giảng viên B | Đăng nhập `lecturer01`, gửi `DELETE /api/lecturer/topics/3` (đề tài của `lecturer02`) | Hệ thống trả về lỗi **403 Forbidden** hoặc thông báo bạn không có quyền sửa đổi đề tài này. |

---

## 6. KỊCH BẢN KIỂM THỬ LUỒNG DUYỆT ĐỀ TÀI & PHÂN CÔNG GVHD (TOPIC APPROVAL AND PUBLISHING WORKFLOW)

### 6.1. Mục tiêu kiểm thử
Xác minh Trưởng bộ môn (`HEAD_OF_DEPT`) và Trưởng khoa (`DEAN`) có thể thẩm định đề tài chờ duyệt, phân công Giảng viên hướng dẫn 1 và 2 (không trùng nhau), phê duyệt hoặc từ chối đề tài có lý do rõ ràng.

### 6.2. Kịch bản Thành công: Phân công GVHD và Phê duyệt đề tài (TC-APP-01)
* **Tài khoản thực hiện:** `hod01` (Trưởng bộ môn CNPM)
* **Đường dẫn UI:** `/lecturer/topics/approval.html`
* **Endpoint API ngầm:** 
  * Phân công GVHD: `POST /api/lecturer/department-topics/{id}/assign-advisors`
  * Duyệt đề tài: `POST /api/lecturer/department-topics/{id}/approval`

#### Các bước thực hiện:
1. Đăng nhập tài khoản `hod01` / `Demo@12345`.
2. Truy cập menu **"Duyệt đề tài bộ môn"** (`/lecturer/topics/approval.html`).
3. Xác định đề tài `DT-CNPM-202603` (Trạng thái `Chờ duyệt` trong seed data).
4. Bấm nút **"Gán giảng viên hướng dẫn"**:
   * Hộp thoại mở ra hiển thị thông tin đề tài.
   * Tại mục **Giảng viên hướng dẫn 1:** Chọn `TS. Trần Hoàng Nam` (`lecturer01`).
   * Tại mục **Giảng viên hướng dẫn 2:** Chọn `ThS. Đặng Thị Kim Ngân` (`lecturer02`).
   * Bấm **"Lưu phân công GVHD"**.
   * Hệ thống thông báo thành công. Badge hiển thị *"Đã gán GVHD"*.
5. Bấm nút **"Duyệt"**:
   * Hộp thoại xác nhận hiện ra: *"Bạn có chắc chắn muốn DUYỆT đề tài này không?"*.
   * Bấm **OK**.

#### Kết quả mong đợi:
* Đề tài `DT-CNPM-202603` chuyển trạng thái sang **`Đã duyệt` (APPROVED)**.
* Kiểm tra hiển thị phía sinh viên: Đăng nhập `student01` ➔ Vào tab **"Kho đề tài"** (`#topics`) ➔ Đề tài `DT-CNPM-202603` hiện đã xuất hiện trong danh sách sinh viên có thể đăng ký.

---

### 6.3. Kịch bản Thành công: Từ chối đề tài kèm lý do (TC-APP-02)
1. Đăng nhập tài khoản `hod01`.
2. Tạo nhanh một đề tài test hoặc chọn một đề tài PENDING.
3. Bấm nút **"Từ chối"** tại dòng đề tài đó.
4. Hộp thoại yêu cầu nhập lý do hiện ra: Nhập nội dung `Nội dung đề tài trùng lặp với đề tài năm học trước, yêu cầu cập nhật lại mục tiêu nghiên cứu.`
5. Bấm **OK**.

#### Kết quả mong đợi:
* Đề tài chuyển trạng thái sang **`Từ chối` (REJECTED)**.
* Giảng viên đề xuất truy cập xem chi tiết đề tài (`detail.html?id=...`) thấy khung màu đỏ hiển thị chính xác lý do từ chối.
* Đề tài này **tuyệt đối không hiển thị** trên Kho đề tài của sinh viên.

---

### 6.4. Các kịch bản Biên & Thất bại (Negative Test Cases)

| Mã test case | Tình huống kiểm thử | Thao tác kiểm thử | Kết quả mong đợi |
| :--- | :--- | :--- | :--- |
| **TC-APP-NEG-01** | Gán GVHD 1 trùng với GVHD 2 | Chọn GVHD 1 = `lecturer01`, GVHD 2 = `lecturer01` | Modal báo lỗi: `GVHD 1 và GVHD 2 không được trùng nhau`. Nút lưu bị chặn. |
| **TC-APP-NEG-02** | Trưởng bộ môn CNPM duyệt đề tài của Bộ môn HTTT | Đăng nhập `hod01` (CNPM), cố tình gọi API duyệt đề tài `DT-HTTT-202601` | Trả về mã lỗi **403 Forbidden**: Bạn không có quyền quản lý đề tài ngoài phạm vi bộ môn phụ trách. |
| **TC-APP-NEG-03** | Giảng viên thường cố tình duyệt đề tài | Đăng nhập `lecturer01`, gửi request `POST /api/lecturer/department-topics/1/approval` | Hệ thống trả về lỗi **403 Forbidden**. |

---

## 7. KỊCH BẢN KIỂM THỬ LUỒNG QUẢN LÝ NHÓM SINH VIÊN (STUDENT GROUP WORKFLOW)

### 7.1. Mục tiêu kiểm thử
Xác minh sinh viên có thể tự do tạo nhóm trong đợt, mời bạn cùng lớp qua MSSV, xử lý lời mời (đồng ý / từ chối), chuyển quyền nhóm trưởng, rời nhóm, giải tán nhóm và bảo đảm các quy chế đào tạo: nhóm tối đa 3 người, mỗi sinh viên chỉ thuộc tối đa 1 nhóm trong một đợt đăng ký.

### 7.2. Kịch bản Thành công: Tạo nhóm và Mời thành viên (TC-GRP-01)
* **Tài khoản thực hiện:** `student03` (MSSV: 22110003 - Chưa có nhóm) và `student04` (MSSV: 22110004 - Chưa có nhóm)
* **Đường dẫn UI:** `/` (Trang không gian sinh viên) ➔ Tab **"Nhóm của tôi"** (`#group`)

#### Các bước thực hiện:
1. Đăng nhập tài khoản `student03` / `Demo@12345`.
2. Nhấp vào mục điều hướng **"Nhóm của tôi"** (`#group`).
3. Khung màn hình hiển thị trạng thái chưa có nhóm. Bấm nút **"+ Tạo nhóm mới"**.
4. Nhập tên nhóm: `Nhóm Phát triển Hệ thống Web Thông minh` ➔ Bấm **"Tạo nhóm"**.
   * Hệ thống thông báo: *"Đã tạo nhóm. Bạn là nhóm trưởng."*.
   * Giao diện cập nhật danh sách thành viên: `Lê Hoàng Nam (Bạn) - MSSV: 22110003 - Badge: Nhóm trưởng` (Số lượng: 1 / 3 thành viên).
5. Bấm nút **"+ Mời thành viên"**:
   * Nhập MSSV: `22110004` (Tài khoản của `student04`).
   * Bấm **"Gửi lời mời"**.
   * Hệ thống thông báo đã gửi lời mời thành công. Bảng *"Lời mời của nhóm"* hiển thị dòng `Phạm Ngọc Anh - 22110004 - Chờ xác nhận`.
6. Mở trình duyệt ẩn danh (Incognito) hoặc Đăng xuất, đăng nhập tài khoản `student04` / `Demo@12345`.
7. `student04` truy cập tab **"Nhóm của tôi"** (`#group`):
   * Khung *"Lời mời dành cho bạn"* hiển thị thẻ mời từ nhóm `Nhóm Phát triển Hệ thống Web Thông minh`.
   * Bấm nút **"Tham gia nhóm"** (`POST /api/student/invitations/{id}/response` với `{ accept: true }`).
8. Cả hai sinh viên tải lại trang:
   * Danh sách thành viên nhóm hiển thị đủ 2 sinh viên (`student03` - Nhóm trưởng, `student04` - Thành viên).
   * Huy hiệu thành viên cập nhật `2 / 3 thành viên`.

---

### 7.3. Kịch bản Thành công: Chuyển quyền nhóm trưởng (TC-GRP-02)
1. Đang ở phiên đăng nhập của `student03` (Nhóm trưởng hiện tại).
2. Tại danh sách thành viên ở tab `#group`, dòng của `student04` có nút **"Chọn làm nhóm trưởng"**.
3. Bấm nút **"Chọn làm nhóm trưởng"** ➔ Hộp thoại xác nhận: *"Chọn Phạm Ngọc Anh làm nhóm trưởng?..."*.
4. Bấm **"Xác nhận chuyển quyền"**.
5. Giao diện làm mới:
   * `student04` nhận badge **Nhóm trưởng**.
   * `student03` trở thành **Thành viên** (các nút thao tác trưởng nhóm như Mời thành viên, Đăng ký đề tài bị ẩn khỏi `student03`).

---

### 7.4. Kịch bản Rời nhóm / Xóa thành viên / Giải tán nhóm (TC-GRP-03)
> [!NOTE]
> Các chức năng này được hiện thực đầy đủ tại Backend API Service (`/api/student/groups/leave`, `/api/student/groups/members/remove`, `/api/student/groups/disband`). Tester có thể kích hoạt qua DevTools Console / Postman / cURL.

1. **Thành viên tự rời nhóm (`POST /api/student/groups/leave`):**
   * `student03` (Thành viên) gửi request rời nhóm.
   * Nhóm giảm từ 2 thành viên xuống còn 1 thành viên (`student04`).
   * `student03` trở về trạng thái tự do, có thể tạo nhóm mới hoặc nhận lời mời khác.
2. **Nhóm trưởng xóa thành viên (`POST /api/student/groups/members/remove`):**
   * Nhóm trưởng gửi request xóa MSSV thành viên khi chưa đăng ký đề tài. Thành viên bị đưa ra khỏi nhóm.
3. **Giải tán nhóm (`POST /api/student/groups/disband`):**
   * Khi nhóm chưa đăng ký đề tài hoặc đăng ký đã bị từ chối/hủy, nhóm trưởng gửi request giải tán nhóm. Bản ghi nhóm bị xóa, tất cả thành viên được giải phóng.

---

### 7.5. Các kịch bản Biên & Thất bại (Negative Test Cases)

| Mã test case | Tình huống kiểm thử | Thao tác / Dữ liệu kiểm thử | Kết quả mong đợi |
| :--- | :--- | :--- | :--- |
| **TC-GRP-NEG-01** | Mời thành viên đã có nhóm trong cùng đợt | Mời MSSV `22110001` (`student01` vốn đã ở nhóm `GRP-202601`) | Báo lỗi: Sinh viên này đã tham gia một nhóm khác trong đợt đăng ký này. Không thể gửi lời mời. |
| **TC-GRP-NEG-02** | Mời vượt quá số lượng tối đa 3 thành viên | Nhóm đã có đủ 3 thành viên, tiếp tục gửi lời mời cho sinh viên thứ 4 | Báo lỗi nghiệp vụ: Nhóm đã đủ số lượng tối đa 3 thành viên. |
| **TC-GRP-NEG-03** | Thành viên (không phải nhóm trưởng) cố mời bạn | Đăng nhập tài khoản thành viên thường, gọi API `POST /api/student/groups/invitations` | Hệ thống báo lỗi: Chỉ nhóm trưởng mới có quyền gửi lời mời tham gia nhóm. |
| **TC-GRP-NEG-04** | Thay đổi thành viên khi nhóm đã đăng ký đề tài | Nhóm đã nộp đăng ký đề tài (trạng thái `PENDING` hoặc `APPROVED`), cố gửi lời mời / xóa thành viên | Giao diện hiển thị cảnh báo: *"Danh sách thành viên đã khóa vì nhóm đã đăng ký đề tài"*. API từ chối sửa đổi. |
| **TC-GRP-NEG-05** | Nhóm trưởng tự rời nhóm khi chưa chuyển quyền | Nhóm còn 2 thành viên, nhóm trưởng cố gọi API `leaveGroup` mà chưa chọn nhóm trưởng mới | Hệ thống yêu cầu nhóm trưởng phải chuyển quyền cho thành viên khác trước khi rời nhóm. |

---

## 8. KỊCH BẢN KIỂM THỬ LUỒNG ĐĂNG KÝ ĐỀ TÀI (TOPIC REGISTRATION WORKFLOW)

### 8.1. Mục tiêu kiểm thử
Xác minh Nhóm trưởng đại diện đăng ký đề tài mở trong Kho đề tài; Giảng viên hướng dẫn thẩm định và phê duyệt/từ chối nhóm; Nhóm trưởng có thể hủy đăng ký trong thời hạn và hệ thống lưu lại nhật ký lịch sử trạng thái đầy đủ.

### 8.2. Kịch bản Thành công: Nhóm đăng ký đề tài & Giảng viên phê duyệt (TC-REG-01)
* **Tài khoản thực hiện:** `student04` (Nhóm trưởng) và `lecturer03` (GVHD đề tài `DT-HTTT-202601`)
* **Đường dẫn UI:** 
  * Sinh viên: `/` ➔ Tab **"Kho đề tài"** (`#topics`)
  * Giảng viên: `/lecturer/topics/student-groups.html`

#### Các bước thực hiện:
1. Đăng nhập tài khoản `student04` / `Demo@12345` (Nhóm trưởng nhóm vừa tạo).
2. Nhấp vào tab **"Kho đề tài"** (`#topics`):
   * Sử dụng bộ lọc: Tìm kiếm `ERP` hoặc lọc theo Bộ môn `Hệ thống thông tin`.
   * Tìm thấy đề tài `DT-HTTT-202601: Xây dựng hệ thống hoạch định nguồn lực doanh nghiệp (ERP)...`.
   * Bấm vào tiêu đề đề tài hoặc nút **"Chi tiết"**.
3. Cửa sổ chi tiết đề tài hiện lên:
   * Bấm nút **"Xác nhận đăng ký đề tài"** (`POST /api/student/registrations`).
   * Hệ thống thông báo: *"Đã đăng ký đề tài. Vui lòng chờ giảng viên duyệt."*.
4. Kiểm tra tab **"Đề tài đang thực hiện"** (`#project`):
   * Huy hiệu trạng thái đề tài hiển thị: **`Chờ duyệt` (PENDING)**.
   * Dưới cùng có bảng *"Lịch sử đăng ký đề tài"*: Hiển thị dòng sự kiện `Khởi tạo ➔ Chờ duyệt` thực hiện bởi `Phạm Ngọc Anh`.
5. Đăng xuất, đăng nhập tài khoản giảng viên `lecturer03` / `Demo@12345`.
6. Giảng viên truy cập menu **"Nhóm sinh viên đăng ký"** (`/lecturer/topics/student-groups.html`):
   * Bảng danh sách hiển thị nhóm của `student04` đăng ký đề tài `DT-HTTT-202601` với trạng thái `PENDING`.
   * Cột thao tác có 2 nút: **"Nhận nhóm"** và **"Từ chối"**.
7. Bấm nút **"Nhận nhóm"**:
   * Hộp thoại xác nhận: *"Bạn có chắc đồng ý nhận nhóm sinh viên này?"* ➔ Bấm **OK**.
   * Hệ thống gọi `POST /api/lecturer/student-groups/{id}/approval` với `{ status: 'APPROVED' }`.
8. Kiểm tra lại phía sinh viên (`student04`):
   * Tab `#project` chuyển sang huy hiệu màu xanh: **`Đã được duyệt` (APPROVED)**.
   * Lịch sử đăng ký ghi nhận thêm dòng sự kiện: `PENDING ➔ APPROVED`, người thực hiện: `TS. Lê Văn Tuấn`.

---

### 8.3. Kịch bản Thành công: Hủy đăng ký đề tài trong hạn (TC-REG-02)
1. Trong khi đề tài đang `PENDING` hoặc `APPROVED` nhưng vẫn còn trong thời hạn đăng ký (`studentEndAt`), nhóm trưởng `student04` vào tab `#project`.
2. Bấm nút màu đỏ **"Hủy đăng ký"** (`data-action="cancel-registration"`).
3. Hộp thoại mở ra: Nhập lý do `Nhóm muốn đổi định hướng sang đề tài nghiên cứu khác phù hợp hơn.` ➔ Bấm **"Xác nhận hủy"**.
4. Kết quả:
   * Trạng thái đăng ký chuyển sang **`Đã hủy` (CANCELLED)**.
   * Lịch sử đăng ký lưu vết đầy đủ lý do hủy và thời gian hủy.
   * Nhóm được mở khóa để có thể tiếp tục chọn đăng ký đề tài khác trong Kho đề tài.

---

### 8.4. Các kịch bản Biên & Thất bại (Negative Test Cases)

| Mã test case | Tình huống kiểm thử | Thao tác kiểm thử | Kết quả mong đợi |
| :--- | :--- | :--- | :--- |
| **TC-REG-NEG-01** | Thành viên thường cố đăng ký đề tài | Đăng nhập tài khoản thành viên `student03`, mở modal đề tài | Nút "Xác nhận đăng ký đề tài" không hiển thị. Nếu gọi API trực tiếp, hệ thống trả về lỗi: Chỉ nhóm trưởng mới có quyền đăng ký đề tài. |
| **TC-REG-NEG-02** | Sinh viên chưa có nhóm cố đăng ký | Sinh viên tự do mở chi tiết đề tài | Khung cảnh báo thông báo: *"Bạn cần tạo hoặc tham gia nhóm"*. Nút đăng ký bị ẩn. |
| **TC-REG-NEG-03** | Đăng ký đề tài đã hết hạn đợt | Cố đăng ký vào đề tài thuộc đợt có `studentEndAt` < thời điểm hiện tại | Hệ thống báo lỗi: Đã ngoài thời gian đăng ký đề tài của đợt. |
| **TC-REG-NEG-04** | Nhóm cố đăng ký 2 đề tài cùng lúc | Nhóm đã có đề tài ở trạng thái `PENDING`/`APPROVED`, cố gọi API đăng ký thêm đề tài thứ hai | Báo lỗi nghiệp vụ: Nhóm hiện đã có một đăng ký đề tài đang được xử lý hoặc đã duyệt. |
| **TC-REG-NEG-05** | Giảng viên hướng dẫn từ chối nhóm không nhập lý do | Giảng viên bấm "Từ chối" nhóm nhưng để trống lý do | Hệ thống yêu cầu bắt buộc nhập lý do từ chối để sinh viên nắm được thông tin. |

---

## 9. KỊCH BẢN KIỂM THỬ LUỒNG NỘP BÁO CÁO TIẾN ĐỘ (REPORT SUBMISSION WORKFLOW)

### 9.1. Mục tiêu kiểm thử
Xác minh nhóm sinh viên đã được duyệt đề tài (`APPROVED`) có thể nộp báo cáo tiến độ tuần tự theo 3 cột mốc bắt buộc (**Đề cương ➔ Giữa kỳ ➔ Cuối kỳ**), kiểm tra tính hợp lệ của tệp tải lên (PDF/DOCX, dung lượng <= 10MB) và quyền tải báo cáo của Giảng viên hướng dẫn & Hội đồng.

### 9.2. Kịch bản Thành công: Nộp đủ 3 giai đoạn báo cáo (TC-REP-01)
* **Tài khoản thực hiện:** `student01` (Nhóm trưởng nhóm `GRP-202601` đã được duyệt đề tài `DT-CNPM-202602`)
* **Đường dẫn UI:** `/` ➔ Tab **"Nộp báo cáo"** (`#reports`)
* **Endpoint API ngầm:** 
  * Nộp báo cáo: `POST /api/student/reports` (Multipart form-data)
  * Tải báo cáo: `GET /api/student/reports/{id}/download`

#### Các bước thực hiện:
1. Chuẩn bị sẵn 3 tệp tin trên máy tính:
   * `De_cuong_De_tai_2026.pdf` (Tệp PDF hợp lệ < 5MB)
   * `Bao_cao_Giua_ky_2026.docx` (Tệp DOCX hợp lệ < 5MB)
   * `Bao_cao_Cuoi_ky_2026.pdf` (Tệp PDF hợp lệ < 10MB)
2. Đăng nhập tài khoản `student01` / `Demo@12345`.
3. Nhấp vào tab **"Nộp báo cáo"** (`#reports`):
   * Thấy form nộp bài hiển thị tên đề tài `Xây dựng cổng thông tin và hoạt động ngoại khóa...`.
4. **Nộp Giai đoạn 1 (Đề cương):**
   * Tại dropdown *Loại báo cáo:* Chọn `Đề cương`.
   * Chọn tệp: `De_cuong_De_tai_2026.pdf`.
   * Ghi chú: `Nhóm nộp đề cương chi tiết sau khi chỉnh sửa theo góp ý của thầy Nam.`
   * Bấm nút **"Nộp báo cáo"**.
   * Hệ thống thông báo thành công. Bảng *"Lịch sử nộp báo cáo"* xuất hiện dòng tệp Đề cương.
5. **Nộp Giai đoạn 2 (Giữa kỳ):**
   * Chọn *Loại báo cáo:* `Giữa kỳ`.
   * Chọn tệp: `Bao_cao_Giua_ky_2026.docx`.
   * Bấm **"Nộp báo cáo"** ➔ Thành công.
6. **Nộp Giai đoạn 3 (Cuối kỳ):**
   * Chọn *Loại báo cáo:* `Cuối kỳ`.
   * Chọn tệp: `Bao_cao_Cuoi_ky_2026.pdf`.
   * Bấm **"Nộp báo cáo"** ➔ Thành công.
7. Bấm nút **"Tải về"** tại mỗi dòng trong bảng lịch sử:
   * Trình duyệt tải về tệp tin tương ứng; mở tệp tin ra nội dung nguyên vẹn, không bị lỗi mã hóa font hoặc hỏng file.
8. Đăng xuất, đăng nhập tài khoản giảng viên `lecturer01` (GVHD của đề tài này):
   * Vào menu `Nhóm sinh viên` (`/lecturer/topics/student-groups.html`) ➔ Bấm nút **"Báo cáo"** tại dòng của nhóm `GRP-202601`.
   * Modal hiển thị đầy đủ danh sách 3 tệp tin kèm nút **"Tải về"** (`/api/lecturer/reports/{id}/download`). Giảng viên tải về kiểm tra thành công.

---

### 9.3. Các kịch bản Biên & Thất bại (Negative Test Cases)

| Mã test case | Tình huống kiểm thử | Thao tác / Dữ liệu kiểm thử | Kết quả mong đợi |
| :--- | :--- | :--- | :--- |
| **TC-REP-NEG-01** | Nhảy cóc giai đoạn: Nộp Giữa kỳ khi chưa nộp Đề cương | Tại nhóm mới chưa nộp báo cáo nào, chọn loại `Giữa kỳ` hoặc `Cuối kỳ` rồi bấm nộp | Hệ thống chặn và báo lỗi nghiệp vụ: `Phải nộp và hoàn thành báo cáo Đề cương trước khi nộp Giữa kỳ`. |
| **TC-REP-NEG-02** | Nộp sai định dạng tệp tin cho phép | Đính kèm tệp tin định dạng `.zip`, `.exe`, `.png`, `.txt` | Giao diện từ chối chọn file hoặc hệ thống báo lỗi: Chỉ chấp nhận tệp định dạng PDF (.pdf) hoặc Word (.docx). |
| **TC-REP-NEG-03** | Tệp tin vượt quá dung lượng tối đa 10 MB | Đính kèm tệp PDF dung lượng 15 MB | Hệ thống báo lỗi ngay trên giao diện: `Tệp vượt quá giới hạn 10 MB`. Yêu cầu nén tệp. |
| **TC-REP-NEG-04** | Thành viên (không phải nhóm trưởng) cố nộp báo cáo | Đăng nhập tài khoản `student02` (Thành viên nhóm `GRP-202601`), vào tab `#reports` | Form nộp báo cáo bị khóa, hiển thị dòng trạng thái: *"Chỉ nhóm trưởng được nộp báo cáo. Bạn vẫn có thể xem và tải các báo cáo của nhóm."*. |
| **TC-REP-NEG-05** | IDOR: Giảng viên không hướng dẫn cố tải báo cáo của nhóm | Đăng nhập `lecturer05` (thuộc bộ môn MMT, không hướng dẫn đề tài này), gọi trực tiếp URL `/api/lecturer/reports/{id}/download` | Hệ thống trả về lỗi **403 Forbidden**: Bạn không có quyền truy cập tệp báo cáo của nhóm này. |

---

## 10. KỊCH BẢN KIỂM THỬ LUỒNG HỘI ĐỒNG ĐÁNH GIÁ & PHÂN CÔNG (COUNCIL & DEFENSE WORKFLOW)

### 10.1. Mục tiêu kiểm thử
Xác minh Trưởng khoa (`DEAN`) có thể thành lập hội đồng bảo vệ hợp lệ (quy định từ 3 đến 5 giảng viên, bắt buộc có đủ Chủ tịch, Thư ký, Phản biện); phân công nhóm sinh viên vào hội đồng và chỉ định Giảng viên phản biện (GVPB) bảo đảm quy tắc độc lập: **Thành viên hội đồng và GVPB tuyệt đối không được trùng với Giảng viên hướng dẫn của đề tài**.

### 10.2. Kịch bản Thành công: Thành lập Hội đồng Bảo vệ (TC-COU-01)
* **Tài khoản thực hiện:** `dean01`
* **Đường dẫn UI:** `/councils/index.html`
* **Endpoint API ngầm:** `POST /api/councils`

#### Các bước thực hiện:
1. Đăng nhập tài khoản `dean01` / `Demo@12345`.
2. Truy cập trang Quản lý Hội đồng: `http://localhost:8080/councils/index.html`.
3. Bấm nút **"+ Thành lập hội đồng bảo vệ"** (`#create-council`).
4. Điền các trường thông tin trong hộp thoại:
   * **Mã hội đồng:** `HD-CNPM-02`
   * **Phòng báo cáo:** `A1-401`
   * **Tên hội đồng:** `Hội đồng Đánh giá Đề tài Chuyên ngành Kỹ thuật Phần mềm`
   * **Thời gian báo cáo:** Chọn ngày giờ trong tương lai (VD: `2026-11-20T08:30`)
   * **Thành viên hội đồng:**
     * Giảng viên 1: Chọn `PGS. TS. Nguyễn Văn Thành` (`dean01`) — Vai trò: `Chủ tịch` (CHAIRPERSON)
     * Giảng viên 2: Chọn `TS. Nguyễn Hoàng Long` (`hod01`) — Vai trò: `Thư ký` (SECRETARY)
     * Giảng viên 3: Chọn `TS. Võ Minh Trí` (`lecturer05`) — Vai trò: `Phản biện` (REVIEWER)
     * Giảng viên 4: Chọn `TS. Lê Văn Tuấn` (`lecturer03`) — Vai trò: `Ủy viên` (MEMBER)
     * Giảng viên 5: Để trống (`-- Không thêm --`)
5. Bấm nút **"Lưu hội đồng"**.

#### Kết quả mong đợi:
* Hộp thoại đóng lại, thông báo xanh *"Đã lưu thay đổi thành công"* xuất hiện.
* Thẻ hội đồng `HD-CNPM-02` hiển thị trên danh sách gồm 4 thành viên với vai trò tương ứng rõ ràng.

---

### 10.3. Kịch bản Thành công: Phân công Nhóm đề tài & Giảng viên phản biện (TC-COU-02)
* **Tài khoản thực hiện:** `dean01`
* **Đường dẫn UI:** `/councils/index.html` ➔ Bấm **"Phân công nhóm & Phản biện"** (`#assign-group`)
* **Endpoint API ngầm:** `POST /api/councils/assignments`

#### Các bước thực hiện:
1. Bấm nút **"Phân công nhóm & Phản biện"**.
2. Trong hộp thoại:
   * **Nhóm sinh viên & Đề tài:** Chọn nhóm `Nhóm Nghiên cứu Kỹ thuật Phần mềm UTE` (Đề tài `DT-CNPM-202602` do `lecturer01` hướng dẫn).
   * **Hội đồng phản biện:** Chọn hội đồng `Hội đồng Đánh giá Khóa luận Tốt nghiệp CNTT 01 (HD-CNTT-01)`.
   * **Giảng viên phản biện (GVPB):** Danh sách tự động lọc các giảng viên có vai trò `Phản biện` trong hội đồng đó. Chọn `TS. Võ Minh Trí` (`lecturer05`).
3. Bấm nút **"Xác nhận phân công"**.

#### Kết quả mong đợi:
* Hệ thống lưu phân công thành công.
* Tại phần **"Danh sách phân công bảo vệ & chấm điểm"**, thẻ bảo vệ của nhóm xuất hiện với các thông tin:
  * Trạng thái: **`Đang chấm điểm`**
  * Hội đồng: `Hội đồng Đánh giá Khóa luận Tốt nghiệp CNTT 01`
  * Phản biện: `TS. Võ Minh Trí`
  * Điểm tổng hợp: `—` (Chưa chấm)

---

### 10.4. Các kịch bản Biên & Thất bại (Negative Test Cases)

| Mã test case | Tình huống kiểm thử | Thao tác kiểm thử | Kết quả mong đợi |
| :--- | :--- | :--- | :--- |
| **TC-COU-NEG-01** | Tạo hội đồng ít hơn 3 thành viên | Chỉ chọn 2 giảng viên rồi bấm Lưu | Form báo lỗi: Hội đồng bắt buộc phải có ít nhất 3 giảng viên. Không cho phép lưu. |
| **TC-COU-NEG-02** | Hội đồng có giảng viên bị trùng nhau | Chọn Giảng viên 1 = `dean01` và Giảng viên 2 = `dean01` | Báo lỗi validation: Các thành viên trong cùng một hội đồng không được trùng lặp. |
| **TC-COU-NEG-03** | Hội đồng thiếu vai trò bắt buộc | Thiết lập hội đồng không có ai giữ vai trò `Phản biện` (REVIEWER) | Báo lỗi ràng buộc nghiệp vụ: Hội đồng bắt buộc phải có đúng 1 Chủ tịch, 1 Thư ký và ít nhất 1 Phản biện. |
| **TC-COU-NEG-04** | Xung đột quyền lợi: Phân công GVPB chính là GVHD của đề tài | Chọn nhóm do `lecturer01` hướng dẫn, nhưng chọn hội đồng có `lecturer01` làm GVPB | Hệ thống chặn và trả lỗi: Giảng viên hướng dẫn không được tham gia hội đồng chấm hoặc làm phản biện cho chính đề tài mình hướng dẫn. |
| **TC-COU-NEG-05** | Giảng viên thường cố tạo hội đồng | Đăng nhập `lecturer01`, truy cập `/councils/index.html` | Các nút "Thành lập hội đồng" và "Phân công" bị ẩn hoàn toàn (chỉ mở cho DEAN). Nếu gọi API POST trực tiếp, trả về **403 Forbidden**. |

---

## 11. KỊCH BẢN KIỂM THỬ LUỒNG CHẤM ĐIỂM BẢO VỆ (GRADING WORKFLOW)

### 11.1. Mục tiêu kiểm thử
Xác minh từng thành viên hội đồng có thể nhập điểm đánh giá cá nhân (thang điểm 0.00 – 10.00) kèm nhận xét chi tiết; người không thuộc hội đồng hoặc GVHD không thể chấm điểm; điểm có thể sửa đổi cho đến trước khi tổng hợp điểm.

### 11.2. Kịch bản Thành công: Từng thành viên hội đồng nhập điểm (TC-GRA-01)
* **Tài khoản thực hiện tuần tự:** 
  1. `lecturer05` (GVPB trong hội đồng `HD-CNTT-01`)
  2. `dean01` (Chủ tịch hội đồng)
  3. `hod01` (Thư ký hội đồng)
* **Đường dẫn UI:** `/councils/index.html`
* **Endpoint API ngầm:** `POST /api/councils/defenses/{id}/grade`

#### Các bước thực hiện:
1. Đăng nhập tài khoản Giảng viên phản biện `lecturer05` / `Demo@12345`.
2. Truy cập `/councils/index.html`. Tại thẻ bảo vệ của nhóm:
   * Thấy nút màu xanh **"Nhập / sửa điểm của tôi"** (`data-grade="{defenseId}"`).
   * Bấm nút này ➔ Hộp thoại mở ra:
     * **Điểm đánh giá (0 - 10):** Nhập `8.50`
     * **Nhận xét / Đánh giá chi tiết:** Nhập `Đề tài đáp ứng tốt mục tiêu đề ra, cấu trúc báo cáo rõ ràng, phần mềm chạy ổn định, sinh viên trả lời phản biện tốt.`
   * Bấm nút **"Lưu đánh giá"**.
   * Hệ thống thông báo lưu thành công. Mở mục *"Xem chi tiết phiếu đánh giá"* thấy phiếu của `TS. Võ Minh Trí: 8.5 điểm`.
3. Đăng xuất, đăng nhập tài khoản `dean01` / `Demo@12345`:
   * Vào `/councils/index.html`, bấm **"Nhập / sửa điểm của tôi"**.
   * Nhập điểm: `9.00` — Nhận xét: `Sản phẩm có tính ứng dụng cao, hoàn thành xuất sắc các yêu cầu.` ➔ Bấm **"Lưu đánh giá"**.
4. Đăng xuất, đăng nhập tài khoản `hod01` / `Demo@12345`:
   * Vào `/councils/index.html`, bấm **"Nhập / sửa điểm của tôi"**.
   * Nhập điểm: `8.75` — Nhận xét: `Kiến trúc hệ thống tốt, cần tối ưu thêm giao diện người dùng.` ➔ Bấm **"Lưu đánh giá"**.
5. Mở chi tiết phiếu đánh giá:
   * Danh sách hiển thị đủ 3 phiếu đánh giá độc lập của 3 giảng viên.

---

### 11.3. Các kịch bản Biên & Thất bại (Negative Test Cases)

| Mã test case | Tình huống kiểm thử | Thao tác / Dữ liệu kiểm thử | Kết quả mong đợi |
| :--- | :--- | :--- | :--- |
| **TC-GRA-NEG-01** | Nhập điểm vượt quá thang điểm quy định | Nhập điểm `10.5` hoặc `-1.0` | Form hoặc API trả về lỗi: `Điểm đánh giá phải nằm trong khoảng từ 0.00 đến 10.00`. |
| **TC-GRA-NEG-02** | Giảng viên không thuộc hội đồng cố chấm điểm | Đăng nhập `lecturer02` (không thuộc hội đồng `HD-CNTT-01`), gọi API `POST /api/councils/defenses/1/grade` | Hệ thống trả về lỗi **403 Forbidden**: Bạn không phải thành viên hội đồng được phân công đánh giá nhóm này. |
| **TC-GRA-NEG-03** | Tự chấm điểm: GVHD cố chấm điểm cho đề tài của mình | Nếu bằng cách nào đó GVHD gọi API chấm điểm cho nhóm mình hướng dẫn | Hệ thống chặn và trả lỗi: Giảng viên hướng dẫn không được quyền chấm điểm bảo vệ cho nhóm của mình. |
| **TC-GRA-NEG-04** | Để trống phần nhận xét đánh giá | Nhập điểm `8.0` nhưng để trống ô nhận xét | Form yêu cầu bắt buộc phải có nhận xét/đánh giá chi tiết trước khi lưu. |

---

## 12. KỊCH BẢN KIỂM THỬ LUỒNG TỔNG HỢP & CÔNG BỐ KẾT QUẢ (FINALIZE & PUBLISH WORKFLOW)

### 12.1. Mục tiêu kiểm thử
Xác minh Chủ tịch hội đồng/Trưởng khoa có thể tổng hợp điểm trung bình cộng chính xác (làm tròn 2 chữ số thập phân), khóa chỉnh sửa điểm sau khi tổng hợp; Trưởng khoa công bố kết quả; sinh viên nhóm bảo vệ xem được kết quả chính thức; kiểm tra ngăn chặn lỗ hổng IDOR khi sinh viên tra cứu điểm.

### 12.2. Kịch bản Thành công: Tổng hợp điểm, Công bố và Sinh viên xem kết quả (TC-PUB-01)
* **Tài khoản thực hiện:** `dean01` (Trưởng khoa / Chủ tịch) và `student01` (Sinh viên nhóm bảo vệ)
* **Đường dẫn UI:** 
  * Quản trị: `/councils/index.html`
  * Sinh viên: `/` ➔ Tab **"Kết quả đánh giá"** (`#results`)
* **Endpoint API ngầm:** 
  * Tổng hợp điểm: `POST /api/councils/defenses/{id}/finalize`
  * Công bố kết quả: `POST /api/councils/defenses/{id}/publish`
  * Sinh viên xem kết quả: `GET /api/student/result?periodId={id}`

#### Các bước thực hiện:
1. Đăng nhập tài khoản `dean01` / `Demo@12345`.
2. Truy cập `/councils/index.html`. Tại thẻ bảo vệ của nhóm đã có 3 phiếu điểm (8.50, 9.00, 8.75):
   * Thấy nút **"Tổng hợp điểm hội đồng"** (`data-finalize="{id}"`).
   * Bấm nút này ➔ Hộp thoại hiện thông báo giải thích: *"Hệ thống sẽ tính điểm trung bình cộng... và khóa chỉnh sửa điểm"*.
   * Bấm **"Xác nhận tổng hợp điểm"**.
3. Kiểm tra hiển thị sau khi tổng hợp:
   * Thẻ bảo vệ cập nhật: Badge **`Đã tổng hợp điểm`**.
   * Ô điểm tổng hợp hiển thị: `(8.50 + 9.00 + 8.75) / 3 = 8.75`.
   * Nút "Nhập / sửa điểm của tôi" bị vô hiệu hóa hoặc ẩn (không cho sửa điểm nữa).
   * Nút mới màu xanh xuất hiện: **"Công bố kết quả"** (`data-publish="{id}"`).
4. Bấm nút **"Công bố kết quả"**:
   * Hộp thoại xác nhận hiện ra ➔ Bấm **"Xác nhận công bố kết quả"**.
   * Badge chuyển sang màu xanh: **`Đã công bố kết quả`**.
5. Đăng xuất, đăng nhập tài khoản sinh viên `student01` / `Demo@12345`.
6. Nhấp vào tab **"Kết quả đánh giá"** (`#results`):
   * Khung kết quả chính thức xuất hiện trang trọng:
     * **ĐIỂM TỔNG KẾT:** `8.75 / 10`
     * **Tên đề tài:** `Xây dựng cổng thông tin và hoạt động ngoại khóa...`
     * **Bảng chi tiết phiếu đánh giá:** Hiển thị điểm và nhận xét của `TS. Võ Minh Trí (8.5)`, `PGS. TS. Nguyễn Văn Thành (9.0)`, `TS. Nguyễn Hoàng Long (8.75)`.

---

### 12.3. Kiểm thử hồi quy Tra cứu kết quả nhiều đợt (Multi-Period Result Regression: TC-PUB-02)
* **Mục tiêu:** Xác minh khi sinh viên đã từng tham gia nhiều nhóm ở các đợt khác nhau trong lịch sử, việc tra cứu điểm tại `GET /api/student/result` luôn ưu tiên đợt hiện tại đang chọn hoặc đợt mới nhất, không trả về ngẫu nhiên nhóm của đợt cũ.
* **Thao tác:**
  * Sinh viên có nhóm ở đợt quá khứ (đã xong) và nhóm ở đợt hiện tại.
  * Gọi API: `GET /api/student/result?periodId={currentPeriodId}`.
* **Kết quả mong đợi:** Dữ liệu trả về đúng kết quả của đợt được yêu cầu; nếu không truyền `periodId`, hệ thống ưu tiên đợt đang mở hoặc đợt có thời gian gần nhất.

---

### 12.4. Các kịch bản Biên & Thất bại (Negative Test Cases)

| Mã test case | Tình huống kiểm thử | Thao tác kiểm thử | Kết quả mong đợi |
| :--- | :--- | :--- | :--- |
| **TC-PUB-NEG-01** | Giảng viên không phải DEAN cố bấm công bố kết quả | Đăng nhập `hod01` hoặc `lecturer01`, gửi API `POST /api/councils/defenses/1/publish` | Hệ thống trả về lỗi **403 Forbidden**: Chỉ Trưởng khoa (DEAN) mới có thẩm quyền công bố điểm chính thức. |
| **TC-PUB-NEG-02** | Công bố kết quả khi chưa tổng hợp điểm | Cố gọi API `/publish` khi `finalized = false` | Hệ thống từ chối: Bắt buộc phải hoàn tất tổng hợp điểm trước khi công bố. |
| **TC-PUB-NEG-03** | Sửa điểm sau khi đã tổng hợp | Gửi API `/grade` vào thẻ defense đã có `finalized = true` | Hệ thống từ chối: Điểm bảo vệ đã được tổng hợp và khóa, không thể chỉnh sửa thêm. |
| **TC-PUB-NEG-04** | IDOR: Sinh viên nhóm B xem trộm kết quả của nhóm A | Đăng nhập `student03`, gọi `GET /api/student/result` khi nhóm của `student03` chưa bảo vệ xong | Hệ thống chỉ trả về kết quả nhóm của chính sinh viên đang đăng nhập (hoặc báo *"Chưa công bố kết quả"*), tuyệt đối không để lộ điểm của nhóm `student01`. |

---

## 13. DANH MỤC KIỂM THỬ BẢO MẬT & PHÂN QUYỀN (SECURITY & AUTHORIZATION CHECKLIST)

Bảng tổng hợp kiểm thử bảo mật cần rà soát định kỳ trước khi nghiệm thu:

| STT | Hạng mục kiểm tra | Phương pháp kiểm thử | Kết quả mong đợi | Trạng thái |
| :---: | :--- | :--- | :--- | :---: |
| 1 | **Xác thực danh tính (Authentication)** | Nhập sai mật khẩu 3 lần liên tiếp tại `/login.html`. | Báo lỗi thông tin đăng nhập không chính xác; không làm lộ chi tiết username có tồn tại hay không. | [ ] Đạt |
| 2 | **Tài khoản bị khóa (Locked Account)** | Đặt `status = 'LOCKED'` cho một user trong DB, sau đó thử đăng nhập. | Hệ thống từ chối đăng nhập với thông báo tài khoản đã bị khóa, liên hệ quản trị viên. | [ ] Đạt |
| 3 | **Phân quyền vai trò (RBAC - Student)** | Đăng nhập tài khoản `STUDENT`, truy cập trực tiếp URL `/admin/dashboard.html` hoặc `/api/admin/users`. | Trả về mã lỗi **403 Forbidden** hoặc tự động chuyển hướng về không gian sinh viên. | [ ] Đạt |
| 4 | **Phân quyền vai trò (RBAC - Lecturer)** | Đăng nhập tài khoản `LECTURER`, gửi request tạo đợt đăng ký `POST /api/admin/registration-periods`. | Trả về mã lỗi **403 Forbidden**. | [ ] Đạt |
| 5 | **Phạm vi Bộ môn (Department Scoping)** | `hod01` (CNPM) gửi request cập nhật đề tài thuộc bộ môn HTTT (`department_id = 2`). | Bị chặn quyền với lỗi không thuộc phạm vi bộ môn quản lý. | [ ] Đạt |
| 6 | **Chống khai thác IDOR (File Download)** | Sinh viên lấy ID báo cáo của nhóm khác và gửi request `GET /api/student/reports/{otherGroupId}/download`. | Trả về mã lỗi **403 Forbidden**; sinh viên chỉ được phép tải báo cáo của nhóm mình. | [ ] Đạt |
| 7 | **Hủy phiên khi đổi mật khẩu** | Đổi mật khẩu tài khoản tại phiên A, kiểm tra xem phiên đăng nhập B cũ còn hoạt động không. | Phiên cũ bị hủy hiệu lực; mọi thao tác tiếp theo yêu cầu đăng nhập lại (`password_changed_at` check). | [ ] Đạt |
| 8 | **Bảo vệ chống tấn công CSRF** | Gửi một request `POST` qua Postman/cURL mà không kèm header `X-XSRF-TOKEN` hoặc `_csrf`. | Máy chủ từ chối xử lý với mã lỗi **403 Forbidden (Invalid CSRF Token)**. | [ ] Đạt |
| 9 | **Chống SQL Injection / XSS** | Nhập chuỗi `' OR '1'='1` vào ô tìm kiếm đề tài hoặc nhập mã độc `<script>alert(1)</script>` vào tên nhóm. | Hệ thống escape dữ liệu an toàn, hiển thị nguyên văn dạng chuỗi văn bản, không thực thi mã độc. | [ ] Đạt |

---

## 14. CHECKLIST KIỂM TRA TRỰC TIẾP CƠ SỞ DỮ LIỆU (DATABASE VERIFICATION CHECKLIST)

Sau khi thực hiện các bước trên giao diện Web, QA Engineer / Giảng viên có thể mở công cụ quản lý CSDL (DBeaver, MySQL Workbench, H2 Console) và chạy các câu lệnh SQL sau để kiểm chứng tính toàn vẹn dữ liệu:

### 14.1. Kiểm tra Đợt đăng ký và Ràng buộc thời gian
```sql
SELECT id, name, type, lecturer_start_at, lecturer_end_at, student_start_at, student_end_at, review_deadline, defense_date
FROM registration_periods
ORDER BY id DESC;
-- Kiểm tra: lecturer_start_at < lecturer_end_at < student_start_at < student_end_at
```

### 14.2. Kiểm tra Đề tài và Phân công Giảng viên hướng dẫn
```sql
SELECT t.id, t.topic_code, t.title, t.status, t.topic_type,
       d.code AS dept_code,
       u_creator.full_name AS creator_name,
       u_adv1.full_name AS advisor1_name,
       u_adv2.full_name AS advisor2_name
FROM topics t
JOIN departments d ON t.department_id = d.id
JOIN users u_creator ON t.created_by = u_creator.id
LEFT JOIN users u_adv1 ON t.advisor1_id = u_adv1.id
LEFT JOIN users u_adv2 ON t.advisor2_id = u_adv2.id
ORDER BY t.id DESC;
-- Kiểm tra: status chuyển từ 'PENDING' sang 'APPROVED'; advisor1_id khác advisor2_id.
```

### 14.3. Kiểm tra Nhóm sinh viên và Tính duy nhất thành viên trong đợt
```sql
SELECT g.id AS group_id, g.group_code, g.group_name, g.status AS group_status,
       gm.member_role, u.user_code AS student_mssv, u.full_name, gm.registration_period_id
FROM student_groups g
JOIN group_members gm ON g.id = gm.group_id
JOIN users u ON gm.student_id = u.id
ORDER BY g.id, gm.member_role DESC;
-- Kiểm tra: Mỗi nhóm có tối đa 3 thành viên; ràng buộc uk_group_member_period_student không bị vi phạm.
```

### 14.4. Kiểm tra Lịch sử thay đổi trạng thái Đăng ký đề tài
```sql
SELECT tr.id AS reg_id, g.group_code, t.topic_code, tr.status AS current_status,
       h.old_status, h.new_status, u.full_name AS actor_name, h.changed_at, h.note
FROM topic_registrations tr
JOIN student_groups g ON tr.group_id = g.id
JOIN topics t ON tr.topic_id = t.id
LEFT JOIN registration_status_history h ON tr.id = h.registration_id
LEFT JOIN users u ON h.changed_by = u.id
ORDER BY tr.id, h.changed_at ASC;
-- Kiểm tra: Chuỗi trạng thái ghi nhận trung thực (VD: DRAFT -> PENDING -> APPROVED).
```

### 14.5. Kiểm tra Tệp báo cáo tiến độ và Tính tuần tự
```sql
SELECT r.id, g.group_code, r.stage, r.filename, r.content_type, r.report_size, r.submitted_at, u.full_name AS submitter
FROM student_reports r
JOIN student_groups g ON r.group_id = g.id
JOIN users u ON r.submitted_by_id = u.id
ORDER BY r.group_id, r.submitted_at ASC;
-- Kiểm tra: stage xuất hiện tuần tự 'Đề cương' -> 'Giữa kỳ' -> 'Cuối kỳ'.
```

### 14.6. Kiểm tra Hội đồng, Điểm đánh giá và Điểm tổng hợp
```sql
SELECT d.id AS defense_id, c.code AS council_code, g.group_name, t.topic_code,
       u_rev.full_name AS reviewer_name,
       d.final_score, d.finalized, d.published,
       dg.evaluator_id, u_eval.full_name AS evaluator_name, dg.score, dg.comment
FROM defenses d
JOIN councils c ON d.council_id = c.id
JOIN student_groups g ON d.group_id = g.id
JOIN topics t ON d.topic_id = t.id
JOIN users u_rev ON d.reviewer_id = u_rev.id
LEFT JOIN defense_grades dg ON d.id = dg.defense_id
LEFT JOIN users u_eval ON dg.evaluator_id = u_eval.id
ORDER BY d.id, dg.id ASC;
-- Kiểm tra: final_score = trung bình cộng các điểm dg.score; finalized = 1, published = 1.
```

---

## 15. KỊCH BẢN NGHIỆM THU TOÀN TRÌNH HOÀN CHỈNH (COMPLETE END-TO-END ACCEPTANCE CHECKLIST)

Tester hoặc Nhóm phát triển có thể dùng bảng kiểm này để đánh dấu tiến độ nghiệm thu từ A đến Z:

- [ ] **Bước 1 (DEAN):** Đăng nhập `dean01`, tạo đợt đăng ký mới với cấu hình thời gian chuẩn.
- [ ] **Bước 2 (DEAN):** Kiểm tra bộ lọc người dùng, bộ môn và đăng thông báo chung toàn khoa.
- [ ] **Bước 3 (LECTURER):** Đăng nhập `lecturer01`, đề xuất đề tài mới `DT-CNPM-202699` (Mô tả >= 10 ký tự, max 3 sinh viên).
- [ ] **Bước 4 (HOD):** Đăng nhập `hod01`, mở trang Duyệt đề tài bộ môn, phân công GVHD1 và GVHD2, bấm Duyệt đề tài.
- [ ] **Bước 5 (STUDENT):** Đăng nhập `student03`, vào tab Kho đề tài, xác nhận đề tài vừa duyệt đã xuất hiện công khai.
- [ ] **Bước 6 (STUDENT):** `student03` vào tab Nhóm của tôi, tạo nhóm mới `Nhóm Nghiên cứu Kỹ thuật Phần mềm`.
- [ ] **Bước 7 (STUDENT):** `student03` gửi lời mời tham gia nhóm cho `student04` (MSSV: 22110004).
- [ ] **Bước 8 (STUDENT):** `student04` đăng nhập, chấp nhận lời mời tham gia nhóm thành công (Nhóm đạt 2 thành viên).
- [ ] **Bước 9 (STUDENT):** Nhóm trưởng vào Kho đề tài, thực hiện Đăng ký đề tài vừa duyệt.
- [ ] **Bước 10 (LECTURER):** Giảng viên hướng dẫn vào trang Nhóm sinh viên đăng ký, bấm Nhận nhóm sinh viên.
- [ ] **Bước 11 (STUDENT):** Nhóm trưởng vào tab Nộp báo cáo, nộp tuần tự 3 tệp báo cáo: Đề cương ➔ Giữa kỳ ➔ Cuối kỳ (PDF/DOCX <= 10MB).
- [ ] **Bước 12 (LECTURER & COUNCIL):** Giảng viên hướng dẫn bấm xem và tải về thành công các tệp báo cáo đã nộp.
- [ ] **Bước 13 (DEAN):** Đăng nhập `dean01`, truy cập `/councils/index.html`, tạo Hội đồng bảo vệ gồm 4 thành viên (Chủ tịch, Thư ký, Phản biện, Ủy viên).
- [ ] **Bước 14 (DEAN):** Phân công nhóm sinh viên vào hội đồng và chỉ định Giảng viên phản biện (không trùng GVHD).
- [ ] **Bước 15 (COUNCIL MEMBERS):** Các thành viên hội đồng lần lượt đăng nhập và nhập điểm đánh giá (0 - 10) kèm nhận xét.
- [ ] **Bước 16 (DEAN):** Bấm "Tổng hợp điểm hội đồng" (kiểm tra điểm trung bình cộng chính xác và chức năng khóa điểm).
- [ ] **Bước 17 (DEAN):** Bấm "Công bố kết quả" chính thức.
- [ ] **Bước 18 (STUDENT):** Sinh viên đăng nhập, vào tab Kết quả đánh giá, tra cứu chính xác điểm tổng kết và bảng điểm từng giảng viên.

---

## 16. MẪU PHIẾU GHI NHẬN LỖI CHUẨN (BUG RECORDING TEMPLATE)

Khi phát hiện sự cố hoặc lỗi nghiệp vụ trong quá trình kiểm thử thủ công, tester sử dụng mẫu sau để báo cáo:

```markdown
### [BUG-ID] Tiêu đề tóm tắt lỗi ngắn gọn, rõ ràng

* **Độ nghiêm trọng (Severity):** [Blocker / Critical / Major / Minor / Trivial]
* **Mức độ ưu tiên (Priority):** [P0 / P1 / P2 / P3]
* **Mô-đun chức năng:** [Đợt đăng ký / Đề tài / Nhóm SV / Báo cáo / Hội đồng / Điểm số / Bảo mật]
* **Tài khoản kiểm thử thực hiện:** (VD: `student03`, `dean01`)
* **Môi trường kiểm thử:** (VD: Windows 11, JDK 21, Google Chrome v128, H2 Database)

#### 1. Các bước tái hiện lỗi (Steps to Reproduce):
1. Đăng nhập tài khoản ...
2. Truy cập đường dẫn URL ...
3. Nhập dữ liệu vào các trường ...
4. Bấm nút ...

#### 2. Kết quả thực tế (Actual Result):
* Hệ thống hiển thị thông báo lỗi 500 Internal Server Error hoặc dữ liệu tính toán sai...
* (Ghi lại mã lỗi HTTP, thông báo lỗi trên giao diện hoặc trong DevTools Console)

#### 3. Kết quả mong đợi (Expected Result):
* Hệ thống phải validate và hiển thị thông báo thân thiện...
* Dữ liệu trong bảng CSDL phải được cập nhật chính xác...

#### 4. Dữ liệu thử nghiệm & Bằng chứng đính kèm (Test Data & Evidence):
* Payload request: `{ ... }`
* Log máy chủ (Server stacktrace): `...`
* Ảnh chụp màn hình / Video ghi lại hành vi lỗi.
```

---
*Tài liệu được biên soạn bởi QA Lead & Business Analyst dành riêng cho dự án Quản lý Đề tài Sinh viên HCMUTE.*
