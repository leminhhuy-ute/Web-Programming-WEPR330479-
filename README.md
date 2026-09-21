# HCMUTE Topic Management — Final_project (Module Giảng viên & Quản lý đề tài)

Hệ thống quản lý đề tài dành cho Khoa Công nghệ Thông tin, Trường Đại học Công nghệ Kỹ thuật TP. Hồ Chí Minh (HCMUTE).

Phiên bản này tập trung hoàn toàn vào chức năng **Giảng viên & Quản lý đề tài (LECTURER / HEAD_OF_DEPT)**, tuân thủ kiến trúc **Spring Framework** (Spring MVC + Spring Data JPA + Spring Security + Maven WAR deployment) không dùng Spring Boot.

---

## 🚀 Chức năng chính

### 1. GV Đăng ký / Đề xuất đề tài
- Giảng viên đề xuất đề tài mới (Tên đề tài, mô tả, yêu cầu, số lượng SV tối đa, loại đề tài, bộ môn, đợt đăng ký).
- Đề tài mới được tạo mặc định ở trạng thái `PENDING` (Chờ duyệt).

### 2. Sửa thông tin đề tài
- Giảng viên tự cập nhật thông tin chi tiết của đề tài do chính mình đề xuất.
- Cho phép xóa đề tài đề xuất nếu ở trạng thái `PENDING`.

### 3. Xem & Tìm kiếm danh sách đề tài
- Danh sách đề tài nghiên cứu khoa học, khóa luận, tiểu luận, môn học.
- Bộ lọc đa tiêu chí: Bộ môn, Đợt đăng ký, Loại đề tài (`COURSE`, `NCKH`, `TLCN`, `KLTN`), Trạng thái (`PENDING`, `APPROVED`, `REJECTED`), tìm kiếm theo từ khóa tên hoặc mã đề tài.

### 4. Duyệt / Quản lý đề tài theo bộ môn
- Dành cho Trưởng bộ môn hoặc Giảng viên quản lý bộ môn.
- Phê duyệt (`APPROVED`) hoặc Từ chối (`REJECTED` kèm lý do) các đề tài do giảng viên trong bộ môn đề xuất.

### 5. Gán 1–2 Giảng viên hướng dẫn (GVHD)
- Phân công Giảng viên hướng dẫn 1 (`GVHD 1`) và Giảng viên hướng dẫn 2 (`GVHD 2`) cho các đề tài đã duyệt.

### 6. Xem & Duyệt các nhóm Sinh viên đăng ký đề tài
- Giảng viên xem danh sách nhóm sinh viên (Mã nhóm, Tên nhóm, Trưởng nhóm, số lượng thành viên, ngày đăng ký) đã đăng ký vào đề tài của mình.
- Chấp nhận (`APPROVED`) hoặc từ chối (`REJECTED`) yêu cầu đăng ký đề tài của nhóm.

---

## 🛠️ Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| Backend | Java 25, **Spring Framework 6** (Spring MVC) |
| Security | Spring Security 6 (BCrypt, HTTP Session, Cookie CSRF Token) |
| ORM | Spring Data JPA 3, Hibernate 6 |
| Database | MySQL 8 |
| Frontend | HTML5, CSS3 (Modern Vanilla CSS), JavaScript thuần |
| Build & Deploy | Maven (WAR artifact), Apache Tomcat 11 |
| Testing | JUnit 5, Spring Test |

---

## 💻 Hướng dẫn chạy và Kiểm thử

### 1. Build & Test tự động bằng Maven

Tại thư mục `Final_project`:

```powershell
mvn -B clean package
```

Kết quả build sẽ sinh file WAR tại `target/Final_project.war`.

### 2. Cấu hình Database

Khởi tạo cơ sở dữ liệu MySQL bằng các file SQL trong thư mục `database/`:

```text
database/schema.sql
database/seed.sql
```

Tạo file cấu hình database external tại `<CATALINA_BASE>/conf/topic-management/db.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/student_topic_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh
db.username=root
db.password=root1234
```

### 3. Deploy trên Tomcat 11

1. Copy file `target/Final_project.war` vào thư mục `webapps/` của Apache Tomcat.
2. Khởi động Tomcat.
3. Truy cập hệ thống tại: `http://localhost:8080/Final_project/login.html`

Tài khoản demo:
- **Giảng viên**: `lecturer01` / `Demo@12345`
- **Trưởng bộ môn**: `hod_cnpm` / `Demo@12345`
- **Trưởng khoa**: `dean01` / `Demo@12345`
