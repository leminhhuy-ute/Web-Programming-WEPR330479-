-- MySQL 8, empty database only. Apply once, then run with the mysql profile.
CREATE TABLE sv_student (
 id VARCHAR(255) PRIMARY KEY, name VARCHAR(255) NOT NULL, class_name VARCHAR(255),
 password_hash VARCHAR(255) NOT NULL, group_id BIGINT
);
CREATE TABLE sv_group (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(80) NOT NULL,
 leader_id VARCHAR(255) NOT NULL, created_at DATETIME(6)
);
CREATE TABLE sv_invitation (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, group_id BIGINT NOT NULL,
 student_id VARCHAR(255) NOT NULL, status VARCHAR(255) NOT NULL, created_at DATETIME(6),
 CONSTRAINT uk_invitation_group_student UNIQUE(group_id,student_id)
);
CREATE TABLE sv_topic (
 id VARCHAR(255) PRIMARY KEY, title VARCHAR(255) NOT NULL, description VARCHAR(3000),
 department VARCHAR(255), type VARCHAR(255), supervisor VARCHAR(255), technologies VARCHAR(255),
 published BIT NOT NULL, capacity INT NOT NULL, opens_at DATETIME(6), closes_at DATETIME(6), report_due_at DATETIME(6)
);
CREATE TABLE sv_registration (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, group_id BIGINT NOT NULL UNIQUE,
 topic_id VARCHAR(255) NOT NULL, status VARCHAR(255) NOT NULL,
 submitted_at DATETIME(6), feedback VARCHAR(1000)
);
CREATE TABLE sv_report (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, group_id BIGINT NOT NULL, submitted_by VARCHAR(255),
 filename VARCHAR(255), content_type VARCHAR(255), stage VARCHAR(255), note VARCHAR(1000),
 submitted_at DATETIME(6), late BIT NOT NULL, content LONGBLOB
);
CREATE INDEX idx_student_group ON sv_student(group_id);
CREATE INDEX idx_invitation_student_status ON sv_invitation(student_id,status);
CREATE INDEX idx_registration_topic_status ON sv_registration(topic_id,status);
CREATE INDEX idx_report_group_time ON sv_report(group_id,submitted_at);
