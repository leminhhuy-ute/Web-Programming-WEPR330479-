# Implementation Notes

## 1. Kiến trúc

Hệ thống sử dụng kiến trúc web truyền thống:

```text
Browser
  │
  │ HTML / CSS / JavaScript + Fetch API
  ▼
Spring MVC Controllers
  │
  ▼
Service Layer
  │
  ▼
Spring Data JPA
  │
  ▼
Hibernate
  │
  ▼
MySQL
```

Ứng dụng được đóng gói dưới dạng WAR và triển khai trên Apache Tomcat 11.

## 2. Công nghệ

- Java 25 target
- Spring Framework 6
- Spring MVC
- Spring Security
- Spring Data JPA
- Hibernate 6
- MySQL
- HTML / CSS / JavaScript thuần
- Maven
- Tomcat 11
- JUnit 5

## 3. Authentication

Endpoint:

```text
GET  /api/auth/csrf
POST /api/auth/login
POST /api/auth/logout
GET  /api/auth/me
```

Login request giữ cấu trúc:

```json
{
  "username": "identifier",
  "password": "password"
}
```

`username` ở đây được sử dụng như một login identifier và có thể là:

```text
username
hoặc
email
```

`DatabaseAuthenticationProvider` tìm tài khoản bằng:

```text
username OR email
```

Email được tìm không phân biệt hoa/thường.

Sau khi xác thực thành công, principal vẫn sử dụng username chuẩn của tài khoản để giữ tương thích với các phần còn lại của hệ thống.

## 4. Password

Mật khẩu mới được mã hóa bằng BCrypt.

Nếu tài khoản cũ sử dụng password hash legacy:

1. hệ thống xác minh bằng `LegacyPasswordVerifier`;
2. nếu đăng nhập hợp lệ, password được mã hóa lại bằng BCrypt;
3. hash mới được lưu lại.

Không lưu mật khẩu plaintext.

## 5. Authorization

Các URL:

```text
/admin/**
/api/admin/**
```

chỉ được truy cập bởi:

```text
ROLE_DEAN
```

API chưa đăng nhập trả HTTP `401`.

API đã đăng nhập nhưng không đủ quyền trả HTTP `403` JSON.

## 6. Session

SecurityContext được lưu trong HTTP Session.

Session được tạo khi cần:

```text
SessionCreationPolicy.IF_REQUIRED
```

Số session đồng thời tối đa cho một tài khoản:

```text
1
```

## 7. CSRF

Ứng dụng sử dụng:

```text
CookieCsrfTokenRepository
```

Cookie:

```text
XSRF-TOKEN
```

Header gửi từ frontend:

```text
X-XSRF-TOKEN
```

Frontend lấy token qua:

```text
GET /api/auth/csrf
```

CSRF không bị disable.

## 8. Current user

Thông tin tài khoản hiện tại được lấy từ:

```text
GET /api/auth/me
```

`common.js` cache request current-user để tránh gửi nhiều request trùng lặp.

`admin-shell.js` chịu trách nhiệm render:

- full name;
- role;
- avatar initials.

Dashboard, list, form và detail page sử dụng chung admin shell.

## 9. Database configuration

Database credentials không được đóng gói vào WAR.

Nguồn cấu hình được ưu tiên theo thứ tự:

```text
1. JVM system properties
2. Environment variables
3. External db.properties
```

Các key:

```text
DB_URL / db.url
DB_USERNAME / db.username
DB_PASSWORD / db.password
```

External file mặc định:

```text
<CATALINA_BASE>/conf/topic-management/db.properties
```

Có thể override bằng:

```text
-Ddb.config.file=<path>
```

hoặc:

```text
DB_CONFIG_FILE
```

## 10. User management rules

Các trường unique:

```text
user_code
username
email
```

Một tài khoản có trạng thái:

```text
ACTIVE
INACTIVE
LOCKED
```

Chỉ `ACTIVE` được đăng nhập.

Hệ thống không cho phép:

- xóa chính tài khoản đang đăng nhập;
- khóa Trưởng khoa hoạt động cuối cùng;
- đổi role Trưởng khoa hoạt động cuối cùng;
- xóa Trưởng khoa hoạt động cuối cùng.

Hệ thống phải luôn còn ít nhất một:

```text
DEAN + ACTIVE
```

## 11. Department rules

Không thể xóa bộ môn nếu vẫn còn tài khoản tham chiếu đến bộ môn đó.

Mã bộ môn phải unique.

## 12. Registration period rules

Các loại:

```text
COURSE
NCKH
TLCN
KLTN
```

Thứ tự thời gian bắt buộc:

```text
lecturerStartAt < lecturerEndAt < studentStartAt < studentEndAt
```

### COURSE / NCKH

Không sử dụng:

```text
reviewDeadline
defenseDate
```

Service tự đặt hai giá trị này thành `null`.

### TLCN

Bắt buộc:

```text
reviewDeadline > studentEndAt
```

Không sử dụng `defenseDate`.

### KLTN

Bắt buộc:

```text
reviewDeadline > studentEndAt
```

và:

```text
defenseDate > reviewDeadline.toLocalDate()
```

## 13. Announcement rules

Announcement gồm:

```text
title
content
audience
createdBy
createdAt
```

Audience:

```text
ALL
STUDENT
LECTURER
```

Người tạo được lấy từ tài khoản DEAN đang đăng nhập.

## 14. Frontend architecture

### common.js

Chứa các helper dùng chung:

- context path;
- REST request;
- CSRF header;
- current user cache;
- authorization check;
- logout;
- date/time formatting.

### admin-shell.js

Chứa:

- topbar;
- sidebar;
- navigation;
- current-user rendering;
- responsive sidebar behavior.

### admin-crud.js

Chứa logic frontend cho:

- list;
- search;
- filter;
- create;
- edit;
- delete;
- lock/unlock;
- các CRUD admin.

## 15. Database scripts

### schema.sql

Dùng để tạo database mới.

### seed.sql

Dùng cho môi trường local/demo.

Không sử dụng dữ liệu demo trong production.

## 16. Testing

Test suite hiện tại:

```text
DatabaseConfigLoaderTest              6
RegistrationPeriodServiceTest        4
UserServiceV2Test                     2
DatabaseAuthenticationProviderTest   8
---------------------------------------
Total                               20
```

Chạy:

```powershell
mvn -B test
```

Build đầy đủ:

```powershell
mvn -B clean package
```

## 17. Pre-deployment checklist

Trước khi deploy:

```text
[ ] Database schema đã tồn tại
[ ] External db.properties đã được cấu hình
[ ] Không có credential thật trong repository
[ ] mvn -B clean package -> BUILD SUCCESS
[ ] Tất cả test pass
[ ] WAR mới được copy vào Tomcat
[ ] Tomcat restart thành công
[ ] Login bằng username hoạt động
[ ] Login bằng email hoạt động
[ ] /api/auth/me hoạt động
[ ] Sidebar/topbar hoạt động
[ ] CRUD User hoạt động
[ ] CRUD Department hoạt động
[ ] CRUD Registration Period hoạt động
[ ] CRUD Announcement hoạt động
[ ] Logout hoạt động
```

## 18. Repository hygiene

Không commit:

```text
target/
*.class
local db.properties
database backups
runtime logs
IDE configuration
generated modernization logs
```

Chỉ commit template cấu hình:

```text
config/db.properties.example
```