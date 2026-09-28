# Hướng dẫn triển khai Production: Hệ thống Quản lý Đề tài Sinh viên HCM-UTE

## 1. Yêu cầu hệ thống & Môi trường vận hành

### 1.1 Yêu cầu cấu hình phần cứng
- **CPU**: Tối thiểu 2 vCPUs (Khuyến nghị 4 vCPUs cho môi trường có nhiều lượt bảo vệ đồng thời).
- **RAM**: Tối thiểu 2 GB RAM (Khuyến nghị 4 GB trở lên để vận hành ổn định).
- **Ổ cứng**: Tối thiểu 20 GB SSD khả dụng (đảm bảo không gian lưu trữ cho các tệp báo cáo PDF/DOCX của sinh viên).

### 1.2 Yêu cầu phần mềm & Môi trường chạy
- **Hệ điều hành**: Linux (Ubuntu Server 22.04 LTS, Debian 12 hoặc RHEL 9) hoặc Windows Server.
- **Java Runtime**: OpenJDK 21 LTS trở lên (`java -version` $\ge 21$).
- **Hệ quản trị CSDL**: MySQL Server 8.0.30+ với Storage Engine InnoDB.
- **Máy chủ Reverse Proxy**: Nginx 1.20+ hoặc Apache HTTP Server 2.4+ (đảm nhiệm xử lý mã hóa SSL/TLS).

---

## 2. Cài đặt & Cấu hình Cơ sở dữ liệu MySQL

### 2.1 Khởi tạo Database và Tài khoản ứng dụng
Đăng nhập vào MySQL với quyền quản trị viên:
```bash
mysql -u root -p
```

Thực thi các câu lệnh khởi tạo cơ sở dữ liệu chuẩn UTF-8:
```sql
CREATE DATABASE student_topic_management 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

-- Tạo tài khoản ứng dụng riêng biệt tuân thủ nguyên tắc đặc quyền tối thiểu
CREATE USER 'topic_app'@'localhost' IDENTIFIED BY 'StrongProductionPassword#2026';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, REFERENCES, INDEX, ALTER 
    ON student_topic_management.* TO 'topic_app'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 2.2 Nạp cấu trúc Schema ban đầu
Nạp schema chuẩn từ tệp kịch bản của dự án:
```bash
mysql -u topic_app -p student_topic_management < database/schema.sql
```

*(Tùy chọn dành cho môi trường Staging / Kiểm thử thử nghiệm)*:
```bash
mysql -u topic_app -p student_topic_management < database/seed.sql
```

> [!NOTE]
> Khi khởi chạy ở môi trường Production với profile `mysql`, Hibernate được cấu hình ở chế độ `ddl-auto=validate`. Ứng dụng sẽ kiểm tra tính toàn vẹn của schema mà không tự động chỉnh sửa cấu trúc bảng. Toàn bộ các thay đổi kiến trúc tiếp theo được quản lý tự động bởi Flyway Migration trong thư mục `database/migration/`.

---

## 3. Đóng gói ứng dụng thành file thực thi JAR

Biên dịch và đóng gói ứng dụng bằng công cụ Maven Wrapper có sẵn:

### Trên Linux / macOS:
```bash
chmod +x ./mvnw
./mvnw clean package -DskipTests
```

### Trên Windows (PowerShell):
```powershell
.\mvnw.cmd clean package -DskipTests
```

Tệp thực thi độc lập (Fat JAR) sẽ được tạo tại đường dẫn:
```text
target/topic-management-1.0.0.jar
```

---

## 4. Cấu hình biến môi trường Production

Cấu hình các tham số vận hành thông qua biến môi trường hệ thống:

| Tên biến môi trường | Giá trị khuyến nghị Production | Mô tả ý nghĩa |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | `mysql` | Kích hoạt cấu hình lưu trữ với MySQL |
| `DB_URL` | `jdbc:mysql://localhost:3306/student_topic_management?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh` | Chuỗi kết nối JDBC MySQL |
| `DB_USERNAME` | `topic_app` | Tên tài khoản kết nối CSDL |
| `DB_PASSWORD` | `StrongProductionPassword#2026` | Mật khẩu tài khoản CSDL |
| `SERVER_PORT` | `8080` | Cổng mạng của máy chủ Tomcat nhúng |
| `SESSION_COOKIE_SECURE` | `true` | Bắt buộc truyền cookie session qua kênh HTTPS an toàn |

---

## 5. Cấu hình dịch vụ máy chủ Linux (systemd)

Tạo tài khoản dịch vụ chuyên dụng trên máy chủ:
```bash
sudo useradd -r -s /bin/false topicapp
sudo mkdir -p /opt/topic-management
sudo cp target/topic-management-1.0.0.jar /opt/topic-management/app.jar
sudo chown -R topicapp:topicapp /opt/topic-management
```

Tạo tệp cấu hình biến môi trường an toàn `/opt/topic-management/app.env`:
```ini
SPRING_PROFILES_ACTIVE=mysql
DB_URL=jdbc:mysql://127.0.0.1:3306/student_topic_management?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh
DB_USERNAME=topic_app
DB_PASSWORD=StrongProductionPassword#2026
SESSION_COOKIE_SECURE=true
SERVER_PORT=8080
JAVA_OPTS=-Xms512m -Xmx2048m -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError
```
Thiết lập phân quyền nghiêm ngặt cho tệp cấu hình:
```bash
sudo chmod 600 /opt/topic-management/app.env
sudo chown topicapp:topicapp /opt/topic-management/app.env
```

Khởi tạo tệp cấu hình dịch vụ `/etc/systemd/system/topic-management.service`:
```ini
[Unit]
Description=HCM-UTE Student Topic Management System
After=network.target mysql.service

[Service]
Type=simple
User=topicapp
Group=topicapp
WorkingDirectory=/opt/topic-management
EnvironmentFile=/opt/topic-management/app.env
ExecStart=/usr/bin/java $JAVA_OPTS -jar /opt/topic-management/app.jar
Restart=always
RestartSec=10
SuccessExitStatus=143
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

Kích hoạt và khởi chạy dịch vụ:
```bash
sudo systemctl daemon-reload
sudo systemctl enable --now topic-management
sudo systemctl status topic-management
```

---

## 6. Cấu hình Reverse Proxy Nginx & Chứng chỉ SSL

Cài đặt Nginx và thiết lập chuyển hướng HTTPS kèm chứng chỉ SSL/TLS:

```nginx
server {
    listen 80;
    server_name topics.fit.hcmute.edu.vn;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl http2;
    server_name topics.fit.hcmute.edu.vn;

    ssl_certificate /etc/letsencrypt/live/topics.fit.hcmute.edu.vn/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/topics.fit.hcmute.edu.vn/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    # Dung lượng tối đa cho phép tải lên đối với tệp báo cáo sinh viên
    client_max_body_size 15M;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto https;
        proxy_set_header X-Forwarded-Port 443;

        # Hỗ trợ truyền thông tin phiên làm việc
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";

        # Thời gian chờ đối với các tệp tải lên dung lượng lớn
        proxy_connect_timeout 60s;
        proxy_send_timeout 120s;
        proxy_read_timeout 120s;
    }
}
```

---

## 7. Chiến lược vận hành & Sao lưu dữ liệu định kỳ

### 7.1 Kịch bản tự động sao lưu CSDL hàng ngày
Tạo tệp script sao lưu `/usr/local/bin/backup-topic-management.sh`:
```bash
#!/bin/bash
BACKUP_DIR="/var/backups/student_topic_management"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
mkdir -p "$BACKUP_DIR"

mysqldump --single-transaction --quick --routines --triggers \
    -u topic_app -pStrongProductionPassword#2026 student_topic_management \
    | gzip > "$BACKUP_DIR/db_backup_$TIMESTAMP.sql.gz"

# Tự động dọn dẹp các bản sao lưu cũ hơn 30 ngày
find "$BACKUP_DIR" -type f -name "*.sql.gz" -mtime +30 -delete
```
Cấp quyền thực thi và đưa vào cron job chạy lúc 02:00 sáng mỗi ngày:
```bash
chmod +x /usr/local/bin/backup-topic-management.sh
(crontab -l 2>/dev/null; echo "0 2 * * * /usr/local/bin/backup-topic-management.sh") | crontab -
```

### 7.2 Quy trình phục hồi dữ liệu từ bản sao lưu
Trong tình huống cần phục hồi dữ liệu:
```bash
gunzip < /var/backups/student_topic_management/db_backup_TIMESTAMP.sql.gz | mysql -u topic_app -p student_topic_management
```
