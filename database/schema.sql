CREATE DATABASE IF NOT EXISTS student_topic_management
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE student_topic_management;

CREATE TABLE IF NOT EXISTS departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_code VARCHAR(50) UNIQUE NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(512) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    role VARCHAR(30) NOT NULL,
    department_id BIGINT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_department FOREIGN KEY (department_id) REFERENCES departments(id),
    CONSTRAINT ck_user_role CHECK (role IN ('DEAN', 'LECTURER', 'HEAD_OF_DEPT', 'STUDENT')),
    CONSTRAINT ck_user_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'LOCKED'))
);

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

CREATE TABLE IF NOT EXISTS topics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    topic_code VARCHAR(50) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NULL,
    requirements TEXT NULL,
    max_students INT NOT NULL DEFAULT 2,
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

CREATE TABLE IF NOT EXISTS student_groups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_code VARCHAR(50) UNIQUE NOT NULL,
    group_name VARCHAR(255) NOT NULL,
    topic_id BIGINT NOT NULL,
    leader_id BIGINT NOT NULL,
    member_count INT NOT NULL DEFAULT 1,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    registered_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes TEXT NULL,
    CONSTRAINT fk_group_topic FOREIGN KEY (topic_id) REFERENCES topics(id),
    CONSTRAINT fk_group_leader FOREIGN KEY (leader_id) REFERENCES users(id),
    CONSTRAINT ck_group_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

CREATE TABLE IF NOT EXISTS group_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    member_role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_member_group FOREIGN KEY (group_id) REFERENCES student_groups(id) ON DELETE CASCADE,
    CONSTRAINT fk_member_student FOREIGN KEY (student_id) REFERENCES users(id),
    CONSTRAINT ck_member_role CHECK (member_role IN ('LEADER', 'MEMBER'))
);
