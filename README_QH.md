# HCMUTE Topic Management

Hệ thống quản lý đề tài dành cho Khoa Công nghệ Thông tin, HCMUTE.

Phiên bản hiện tại tập trung vào chức năng **Quản trị / Trưởng khoa (DEAN)**, bao gồm quản lý tài khoản, bộ môn, đợt đăng ký và thông báo.

## Chức năng hiện có

### Xác thực và phân quyền

- Đăng nhập bằng **tên đăng nhập hoặc email**.
- Xác thực bằng Spring Security và HTTP Session.
- Mật khẩu được lưu dưới dạng BCrypt.
- Hỗ trợ xác minh hash legacy và nâng cấp sang BCrypt sau khi đăng nhập thành công.
- Bảo vệ CSRF bằng `XSRF-TOKEN` / `X-XSRF-TOKEN`.
- Chỉ tài khoản có role `DEAN` được truy cập khu vực quản trị.
- Tài khoản `LOCKED` hoặc `INACTIVE` không thể đăng nhập.

### Quản lý tài khoản

- Xem và tìm kiếm tài khoản.
- Lọc theo vai trò và bộ môn.
- Thêm, sửa và xóa tài khoản.
- Khóa / mở khóa tài khoản.
- Kiểm tra trùng mã người dùng, username và email.
- Không cho phép xóa chính tài khoản đang đăng nhập.
- Không cho phép thao tác khiến hệ thống không còn Trưởng khoa đang hoạt động.

### Quản lý bộ môn

- Xem danh sách.
- Tìm kiếm.
- Thêm, sửa và xóa bộ môn.
- Không cho phép xóa bộ môn đang được tài khoản sử dụng.

### Quản lý đợt đăng ký

Hỗ trợ 4 loại:

- `COURSE` — Môn học
- `NCKH` — Nghiên cứu khoa học
- `TLCN` — Tiểu luận chuyên ngành
- `KLTN` — Khóa luận tốt nghiệp

Các mốc thời gian được kiểm tra theo thứ tự:

```text
GV bắt đầu
    <
GV kết thúc
    <
SV bắt đầu
    <
SV kết thúc
```

Quy tắc bổ sung:

- `COURSE`, `NCKH`: không sử dụng hạn GVPB và ngày bảo vệ.
- `TLCN`: bắt buộc có hạn GVPB sau thời gian đăng ký sinh viên.
- `KLTN`: bắt buộc có hạn GVPB và ngày bảo vệ; ngày bảo vệ phải sau hạn GVPB.

### Quản lý thông báo

- Xem và tìm kiếm thông báo.
- Lọc theo đối tượng.
- Thêm, sửa và xóa thông báo.
- Đối tượng hỗ trợ:
  - `ALL`
  - `STUDENT`
  - `LECTURER`

## Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| Backend | Java, Spring Framework |
| Web MVC / REST | Spring MVC |
| Security | Spring Security |
| ORM | Spring Data JPA, Hibernate |
| Database | MySQL |
| Frontend | HTML5, CSS3, JavaScript thuần |
| Build | Maven |
| Deploy | WAR, Apache Tomcat 11 |
| Testing | JUnit 5, Spring Test |

Project **không sử dụng Spring Boot** và không sử dụng framework frontend như React, Vue, Angular, Bootstrap hoặc Tailwind CSS.

## Yêu cầu môi trường

- JDK 25 hoặc mới hơn
- Maven 3.9+
- MySQL 8+
- Apache Tomcat 11

## Cấu trúc chính

```text
final-project/
├── config/
│   └── db.properties.example
├── database/
│   ├── schema.sql
│   └── seed.sql
├── scripts/
│   └── setup-tomcat-db.ps1
├── src/
│   ├── main/
│   │   ├── java/topicmanagement/
│   │   └── webapp/
│   └── test/java/topicmanagement/
├── pom.xml
├── README.md
└── IMPLEMENTATION.md
```

## Thiết lập database

### 1. Khởi tạo database

Chạy lần lượt bằng MySQL Workbench hoặc MySQL client:

```text
database/schema.sql
database/seed.sql
```

`seed.sql` chỉ dành cho môi trường phát triển/demo và nên chạy trên database mới.

### 2. Cấu hình kết nối database

Thông tin đăng nhập database **không được lưu trong source code hoặc WAR**.

Copy:

```text
config/db.properties.example
```

thành:

```text
<CATALINA_BASE>/conf/topic-management/db.properties
```

Nội dung:

```properties
db.url=jdbc:mysql://localhost:3306/student_topic_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh
db.username=<YOUR_MYSQL_USERNAME>
db.password=<YOUR_MYSQL_PASSWORD>
```

Có thể dùng script:

```powershell
.\scripts\setup-tomcat-db.ps1 -CatalinaBase "C:\path\to\tomcat"
```

Thứ tự ưu tiên cấu hình database:

```text
JVM system properties
→ environment variables
→ external db.properties
```

Các biến hỗ trợ:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
DB_CONFIG_FILE
```

## Build

Tại thư mục project:

```powershell
mvn -B clean package
```

Nếu thành công:

```text
BUILD SUCCESS
```

WAR được tạo tại:

```text
target/topic-management.war
```

## Deploy Tomcat

1. Dừng Tomcat.
2. Xóa bản deploy cũ nếu có.
3. Copy:

```text
target/topic-management.war
```

vào:

```text
<CATALINA_BASE>/webapps/
```

4. Khởi động lại Tomcat.

Truy cập:

```text
http://localhost:8080/topic-management/login.html
```

## API chính

### Authentication

```text
GET  /api/auth/csrf
POST /api/auth/login
POST /api/auth/logout
GET  /api/auth/me
```

`POST /api/auth/login` chấp nhận username hoặc email trong trường `username`.

### Admin

```text
/api/admin/users
/api/admin/departments
/api/admin/registration-periods
/api/admin/announcements
```

Các API quản trị yêu cầu role:

```text
DEAN
```

## Kiểm thử

Chạy:

```powershell
mvn -B test
```

Bản hiện tại có 20 automated tests:

- `DatabaseConfigLoaderTest`: 6
- `RegistrationPeriodServiceTest`: 4
- `UserServiceV2Test`: 2
- `DatabaseAuthenticationProviderTest`: 8

Kiểm tra các trường hợp quan trọng gồm:

- login bằng username;
- login bằng email;
- email không phân biệt hoa/thường;
- sai mật khẩu;
- tài khoản bị khóa;
- bảo vệ Trưởng khoa cuối cùng;
- validation đợt đăng ký;
- external database configuration.

## Lưu ý bảo mật

Không commit:

```text
db.properties
database backups
target/
WAR build artifacts
IDE/runtime logs
```

Không sử dụng tài khoản/mật khẩu demo trong môi trường public hoặc production.

## Phạm vi hiện tại

Phiên bản này hoàn thiện module quản trị dành cho **Trưởng khoa**.

Các chức năng nghiệp vụ riêng dành cho Giảng viên và Sinh viên chưa thuộc phạm vi phiên bản hiện tại.