# Production Deployment Guide: HCM-UTE Student Topic Management System

## 1. Prerequisites & System Requirements

### 1.1 Hardware Specifications
- **CPU**: 2 vCPUs minimum (4 vCPUs recommended).
- **RAM**: 2 GB minimum (4 GB+ recommended for concurrent defense periods).
- **Disk Storage**: 20 GB SSD minimum (accounting for student PDF/DOCX report submissions).

### 1.2 Software Requirements
- **Operating System**: Linux (Ubuntu 22.04 LTS / Debian 12 / RHEL 9) or Windows Server.
- **Java Runtime**: OpenJDK 21 LTS (`java --version` $\ge 21$).
- **Database**: MySQL Server 8.0.30+ with InnoDB engine.
- **Reverse Proxy**: Nginx 1.20+ or Apache HTTP Server 2.4+ (for SSL/TLS termination).

---

## 2. MySQL Database Setup

### 2.1 Database Creation & Character Set
Log into MySQL as an administrative user:
```bash
mysql -u root -p
```

Execute database initialization:
```sql
CREATE DATABASE student_topic_management 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

-- Create dedicated least-privilege application user
CREATE USER 'topic_app'@'localhost' IDENTIFIED BY 'StrongProductionPassword#2026';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, REFERENCES, INDEX, ALTER 
    ON student_topic_management.* TO 'topic_app'@'localhost';
FLUSH PRIVILEGES;
```

### 2.2 Schema Initialization
Apply the baseline schema:
```bash
mysql -u topic_app -p student_topic_management < database/schema.sql
```

*(Optional for staging / verification environments)*:
```bash
mysql -u topic_app -p student_topic_management < database/seed.sql
```

> [!NOTE]
> On production startup with profile `mysql`, Hibernate operates in `ddl-auto=validate` mode. It validates the schema without altering tables automatically. Future schema updates are managed deterministically via Flyway migrations in `database/migration/`.

---

## 3. Application Build & Packaging

Build the standalone executable JAR from source using the Maven wrapper:

### Linux / macOS:
```bash
./mvnw clean package -DskipTests
```

### Windows (PowerShell):
```powershell
.\mvnw.cmd clean package -DskipTests
```

The build artifact will be generated at:
```text
target/topic-management-1.0.0.jar
```

---

## 4. Production Configuration & Environment Variables

Configure application settings using standard environment variables:

| Variable | Recommended Production Value | Description |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | `mysql` | Activates MySQL persistence profile |
| `DB_URL` | `jdbc:mysql://localhost:3306/student_topic_management?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh` | JDBC Connection String |
| `DB_USERNAME` | `topic_app` | Dedicated database user |
| `DB_PASSWORD` | `StrongProductionPassword#2026` | Database password |
| `SERVER_PORT` | `8080` | Local port bound by embedded Tomcat |
| `SESSION_COOKIE_SECURE` | `true` | Enforces HTTPS-only transmission of session cookies |

---

## 5. Linux Service Deployment (systemd)

Create a dedicated system user:
```bash
sudo useradd -r -s /bin/false topicapp
sudo mkdir -p /opt/topic-management
sudo cp target/topic-management-1.0.0.jar /opt/topic-management/app.jar
sudo chown -R topicapp:topicapp /opt/topic-management
```

Create environment configuration file `/opt/topic-management/app.env`:
```ini
SPRING_PROFILES_ACTIVE=mysql
DB_URL=jdbc:mysql://127.0.0.1:3306/student_topic_management?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh
DB_USERNAME=topic_app
DB_PASSWORD=StrongProductionPassword#2026
SESSION_COOKIE_SECURE=true
SERVER_PORT=8080
JAVA_OPTS=-Xms512m -Xmx2048m -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError
```
Secure permissions on the environment file:
```bash
sudo chmod 600 /opt/topic-management/app.env
sudo chown topicapp:topicapp /opt/topic-management/app.env
```

Create systemd unit file `/etc/systemd/system/topic-management.service`:
```ini
[Unit]
Description=HCM-UTE Student Topic Management Application
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

Reload systemd and start the service:
```bash
sudo systemctl daemon-reload
sudo systemctl enable --now topic-management
sudo systemctl status topic-management
```

---

## 6. Reverse Proxy & SSL Configuration (Nginx)

Install Nginx and configure SSL termination:

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

    # Maximum file upload size for student project reports
    client_max_body_size 15M;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto https;
        proxy_set_header X-Forwarded-Port 443;

        # WebSocket & Long-polling support
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";

        # Timeouts for large report uploads
        proxy_connect_timeout 60s;
        proxy_send_timeout 120s;
        proxy_read_timeout 120s;
    }
}
```

---

## 7. Operational Maintenance & Backup Strategy

### 7.1 Automated Database Backup Script
Create `/usr/local/bin/backup-topic-management.sh`:
```bash
#!/bin/bash
BACKUP_DIR="/var/backups/student_topic_management"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
mkdir -p "$BACKUP_DIR"

mysqldump --single-transaction --quick --routines --triggers \
    -u topic_app -pStrongProductionPassword#2026 student_topic_management \
    | gzip > "$BACKUP_DIR/db_backup_$TIMESTAMP.sql.gz"

# Retain backups for 30 days
find "$BACKUP_DIR" -type f -name "*.sql.gz" -mtime +30 -delete
```
Make executable and schedule in cron:
```bash
chmod +x /usr/local/bin/backup-topic-management.sh
(crontab -l 2>/dev/null; echo "0 2 * * * /usr/local/bin/backup-topic-management.sh") | crontab -
```

### 7.2 Database Restore Procedure
To restore from backup:
```bash
gunzip < /var/backups/student_topic_management/db_backup_TIMESTAMP.sql.gz | mysql -u topic_app -p student_topic_management
```
