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

| 👤 | MSSV | Họ và tên |
|:---:|:---:|---|
| 1️⃣ | **24110019** | Lê Minh Huy |
| 2️⃣ | **24110012** | Trần Hữu Thành Đô |
| 3️⃣ | **24110033** | Lê Đăng Minh |
| 4️⃣ | **24162043** | Lê Bảo Huy |
| 5️⃣ | **24162048** | Nguyễn Quang Huy |

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

| Thành phần | Công nghệ |
|---|---|
| ☕ Backend | Java, Spring Boot |
| 🔐 Security | Spring Security |
| 💾 Database | MySQL |
| 🗃️ ORM | Spring Data JPA / Hibernate |
| 🎨 Frontend | HTML, CSS, JavaScript |
| 🌿 Version Control | Git & GitHub |

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
