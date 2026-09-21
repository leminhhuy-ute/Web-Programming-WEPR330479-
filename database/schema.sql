-- ========================================================
-- HỆ THỐNG QUẢN LÝ ĐỀ TÀI SINH VIÊN - HCMUTE
-- UNIFIED DATABASE SCHEMA (MySQL 8.0+)
-- ========================================================

CREATE DATABASE IF NOT EXISTS student_topic_management
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE student_topic_management;

-- 1. Bảng Bộ môn (Departments)
CREATE TABLE IF NOT EXISTS departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL
);

-- 2. Bảng Người dùng (Users: Trưởng khoa, Trưởng bộ môn, Giảng viên, Sinh viên)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_code VARCHAR(50) UNIQUE NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(512) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    role VARCHAR(30) NOT NULL,
    department_id BIGINT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_department FOREIGN KEY (department_id) REFERENCES departments(id),
    CONSTRAINT ck_user_role CHECK (role IN ('DEAN', 'HEAD_OF_DEPT', 'LECTURER', 'STUDENT')),
    CONSTRAINT ck_user_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'LOCKED'))
);

-- 3. Bảng Đợt đăng ký đề tài (Registration Periods)
CREATE TABLE IF NOT EXISTS registration_periods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(30) NOT NULL,
    lecturer_start_at DATETIME NOT NULL,
    lecturer_end_at DATETIME NOT NULL,
    student_start_at DATETIME NOT NULL,
    student_end_at DATETIME NOT NULL,
    review_deadline DATETIME NULL,
    defense_date DATE NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT ck_period_type CHECK (type IN ('COURSE', 'NCKH', 'TLCN', 'KLTN'))
);

-- 4. Bảng Thông báo (Announcements)
CREATE TABLE IF NOT EXISTS announcements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    audience VARCHAR(30) NOT NULL DEFAULT 'ALL',
    created_by BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_announcement_author FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT ck_announcement_audience CHECK (audience IN ('ALL', 'STUDENT', 'LECTURER'))
);

-- 5. Bảng Đề tài (Topics)
CREATE TABLE IF NOT EXISTS topics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    topic_code VARCHAR(50) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NULL,
    requirements TEXT NULL,
    max_students INT NOT NULL DEFAULT 3,
    topic_type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    rejection_reason TEXT NULL,
    department_id BIGINT NOT NULL,
    period_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    advisor1_id BIGINT NULL,
    advisor2_id BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_topic_department FOREIGN KEY (department_id) REFERENCES departments(id),
    CONSTRAINT fk_topic_period FOREIGN KEY (period_id) REFERENCES registration_periods(id),
    CONSTRAINT fk_topic_creator FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT fk_topic_advisor1 FOREIGN KEY (advisor1_id) REFERENCES users(id),
    CONSTRAINT fk_topic_advisor2 FOREIGN KEY (advisor2_id) REFERENCES users(id),
    CONSTRAINT ck_topic_type CHECK (topic_type IN ('COURSE', 'NCKH', 'TLCN', 'KLTN')),
    CONSTRAINT ck_topic_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

-- 6. Bảng Nhóm sinh viên (Student Groups)
CREATE TABLE IF NOT EXISTS student_groups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_code VARCHAR(50) UNIQUE NOT NULL,
    group_name VARCHAR(255) NOT NULL,
    leader_id BIGINT NOT NULL,
    topic_id BIGINT NULL,
    member_count INT NOT NULL DEFAULT 1,
    max_members INT NOT NULL DEFAULT 3,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes TEXT NULL,
    CONSTRAINT fk_group_leader FOREIGN KEY (leader_id) REFERENCES users(id),
    CONSTRAINT fk_group_topic FOREIGN KEY (topic_id) REFERENCES topics(id),
    CONSTRAINT ck_group_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

-- 7. Bảng Thành viên nhóm (Group Members)
CREATE TABLE IF NOT EXISTS group_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    member_role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_member_group FOREIGN KEY (group_id) REFERENCES student_groups(id) ON DELETE CASCADE,
    CONSTRAINT fk_member_student FOREIGN KEY (student_id) REFERENCES users(id),
    CONSTRAINT ck_member_role CHECK (member_role IN ('LEADER', 'MEMBER')),
    CONSTRAINT uq_group_student UNIQUE (group_id, student_id)
);

-- 8. Bảng Lời mời nhóm (Group Invitations)
CREATE TABLE IF NOT EXISTS group_invitations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    inviter_id BIGINT NOT NULL,
    invitee_id BIGINT NOT NULL,
    message VARCHAR(500) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_invitation_group FOREIGN KEY (group_id) REFERENCES student_groups(id) ON DELETE CASCADE,
    CONSTRAINT fk_invitation_inviter FOREIGN KEY (inviter_id) REFERENCES users(id),
    CONSTRAINT fk_invitation_invitee FOREIGN KEY (invitee_id) REFERENCES users(id),
    CONSTRAINT ck_invitation_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'CANCELLED'))
);

-- 9. Bảng Đăng ký đề tài (Topic Registrations)
CREATE TABLE IF NOT EXISTS topic_registrations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    topic_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    proposal_note TEXT NULL,
    supervisor_feedback TEXT NULL,
    registered_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at DATETIME NULL,
    CONSTRAINT fk_reg_group FOREIGN KEY (group_id) REFERENCES student_groups(id),
    CONSTRAINT fk_reg_topic FOREIGN KEY (topic_id) REFERENCES topics(id),
    CONSTRAINT ck_reg_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

-- 10. Bảng Báo cáo tiến độ (Progress Reports)
CREATE TABLE IF NOT EXISTS progress_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    submitted_by BIGINT NOT NULL,
    report_title VARCHAR(255) NOT NULL,
    stage VARCHAR(50) NOT NULL, -- Đề cương, Giữa kỳ, Báo cáo cuối kỳ
    content_summary TEXT NULL,
    attachment_url VARCHAR(500) NULL,
    file_name VARCHAR(255) NULL,
    completion_percentage INT NOT NULL DEFAULT 0,
    supervisor_score DOUBLE NULL,
    supervisor_feedback TEXT NULL,
    submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at DATETIME NULL,
    CONSTRAINT fk_report_group FOREIGN KEY (group_id) REFERENCES student_groups(id),
    CONSTRAINT fk_report_submitter FOREIGN KEY (submitted_by) REFERENCES users(id)
);

-- 11. Bảng Hội đồng đánh giá / bảo vệ (Councils)
CREATE TABLE IF NOT EXISTS councils (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    department_id BIGINT NOT NULL,
    period_id BIGINT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    defense_date DATE NULL,
    defense_time VARCHAR(20) NULL,
    room VARCHAR(100) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_council_department FOREIGN KEY (department_id) REFERENCES departments(id),
    CONSTRAINT fk_council_period FOREIGN KEY (period_id) REFERENCES registration_periods(id),
    CONSTRAINT ck_council_status CHECK (status IN ('PENDING', 'ACTIVE', 'COMPLETED', 'CANCELLED'))
);

-- 12. Bảng Thành viên hội đồng (Council Members)
CREATE TABLE IF NOT EXISTS council_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    council_id BIGINT NOT NULL,
    lecturer_id BIGINT NOT NULL,
    member_role VARCHAR(30) NOT NULL, -- CHAIR, SECRETARY, MEMBER, REVIEWER
    CONSTRAINT fk_cm_council FOREIGN KEY (council_id) REFERENCES councils(id) ON DELETE CASCADE,
    CONSTRAINT fk_cm_lecturer FOREIGN KEY (lecturer_id) REFERENCES users(id),
    CONSTRAINT ck_cm_role CHECK (member_role IN ('CHAIR', 'SECRETARY', 'MEMBER', 'REVIEWER')),
    CONSTRAINT uq_council_lecturer UNIQUE (council_id, lecturer_id)
);

-- 13. Bảng Điểm & Đánh giá (Evaluations)
CREATE TABLE IF NOT EXISTS evaluations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    council_id BIGINT NULL,
    topic_id BIGINT NOT NULL,
    evaluator_id BIGINT NOT NULL,
    evaluation_type VARCHAR(30) NOT NULL, -- REVIEWER, COUNCIL
    score DOUBLE NOT NULL,
    feedback TEXT NULL,
    evaluated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_eval_council FOREIGN KEY (council_id) REFERENCES councils(id),
    CONSTRAINT fk_eval_topic FOREIGN KEY (topic_id) REFERENCES topics(id),
    CONSTRAINT fk_eval_evaluator FOREIGN KEY (evaluator_id) REFERENCES users(id),
    CONSTRAINT ck_eval_type CHECK (evaluation_type IN ('REVIEWER', 'COUNCIL'))
);
