<div align="center">

# 🎓 HỆ THỐNG QUẢN LÝ ĐỀ TÀI SINH VIÊN

### 📚 Đồ án môn học

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-Backend-brightgreen?logo=springboot)
![MySQL](https://img.shields.io/badge/MySQL-Database-blue?logo=mysql)
![GitHub](https://img.shields.io/badge/GitHub-Version%20Control-black?logo=github)

</div>

---

## 📌 Giới thiệu

Hệ thống hỗ trợ quản lý toàn bộ quy trình thực hiện đề tài sinh viên:

> 📝 Đăng ký đề tài → ✅ Phê duyệt → 👨‍🏫 Phân công GVHD/GVPB  
> → 📄 Nộp báo cáo → ⭐ Chấm điểm → 📢 Công bố kết quả

Hệ thống áp dụng cho:

- 📘 Đề tài môn học
- 🔬 Nghiên cứu khoa học (NCKH)
- 📑 Tiểu luận chuyên ngành (TLCN)
- 🎓 Khóa luận tốt nghiệp (KLTN)

---

## 👥 Thành viên nhóm

| 👤  |     MSSV     | Họ và tên         |
| :-: | :----------: | ----------------- |
| 1️⃣  | **24110019** | Lê Minh Huy       |
| 2️⃣  | **24110012** | Trần Hữu Thành Đô |
| 3️⃣  | **24110033** | Lê Đăng Minh      |
| 4️⃣  | **24162043** | Lê Bảo Huy        |
| 5️⃣  | **24162048** | Nguyễn Quang Huy  |

---

## ⚙️ Chức năng chính

- 🔐 Đăng nhập & phân quyền
- 👨‍🎓 Quản lý sinh viên
- 👨‍🏫 Quản lý giảng viên
- 📚 Quản lý đề tài
- 👥 Quản lý nhóm sinh viên
- 📝 Đăng ký đề tài
- 👨‍🏫 Phân công GVHD & GVPB
- 🏛️ Quản lý hội đồng
- 📄 Nộp báo cáo
- ⭐ Chấm điểm
- 📊 Công bố kết quả
- 📢 Quản lý thông báo

---

## 🛠️ Công nghệ sử dụng

| Thành phần         | Công nghệ                   |
| ------------------ | --------------------------- |
| ☕ Backend         | Java, Spring Boot           |
| 🔐 Security        | Spring Security             |
| 💾 Database        | MySQL                       |
| 🗃️ ORM             | Spring Data JPA / Hibernate |
| 🎨 Frontend        | HTML, CSS, JavaScript       |
| 🌿 Version Control | Git & GitHub                |

---

## 🏗️ Kiến trúc hệ thống

```text
🎨 Frontend
     │
     │ REST API
     ▼
☕ Spring Boot
     │
     ├── Controller
     ├── Service
     ├── Repository
     └── Security
     │
     ▼
💾 MySQL
```

---

## Module Phản biện – Hội đồng – Chấm điểm

Module hiện có các luồng:

- Quản lý danh sách giảng viên phản biện.
- Tạo hội đồng, thêm 3–5 thành viên, phân vai trò Chủ tịch/Thư ký/Ủy viên/GVPB.
- Phân công đề tài và giảng viên phản biện cho hội đồng đã sẵn sàng.
- Nhập/cập nhật điểm thang 10 và nhận xét của từng thành viên.
- Tự động tính điểm trung bình và cập nhật trạng thái chấm.
- Chặn giảng viên hướng dẫn phản biện/chấm đề tài của mình ở cả giao diện và service.

### Chạy nhanh với dữ liệu demo

Yêu cầu Java 17+ và Maven 3.6.3+.

```bash
mvn spring-boot:run
```

Mở `http://localhost:8080` và đăng nhập:

| Vai trò    | Tài khoản   | Mật khẩu        |
| ---------- | ----------- | --------------- |
| Quản trị   | `admin`     | `Admin@123`     |
| Giảng viên | `giangvien` | `Giangvien@123` |

Chế độ mặc định dùng H2 in-memory và tự tạo dữ liệu minh họa.

### Chạy với MySQL

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/topic_management?createDatabaseIfNotExist=true"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_password"
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

### Kiểm thử

```bash
mvn test
```
