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
    CONSTRAINT ck_user_role CHECK (role IN ('DEAN', 'LECTURER', 'STUDENT')),
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
    CONSTRAINT ck_period_type CHECK (type IN ('COURSE', 'NCKH', 'TLCN', 'KLTN')),
    CONSTRAINT ck_period_times CHECK (
        lecturer_start_at < lecturer_end_at
        AND lecturer_end_at < student_start_at
        AND student_start_at < student_end_at
    ),
    CONSTRAINT ck_period_review CHECK (
        (type IN ('COURSE', 'NCKH') AND review_deadline IS NULL AND defense_date IS NULL)
        OR (type = 'TLCN' AND review_deadline IS NOT NULL
            AND review_deadline > student_end_at AND defense_date IS NULL)
        OR (type = 'KLTN' AND review_deadline IS NOT NULL
            AND review_deadline > student_end_at AND defense_date IS NOT NULL
            AND defense_date > DATE(review_deadline))
    )
);

CREATE TABLE IF NOT EXISTS announcements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    audience VARCHAR(30) NOT NULL,
    created_by BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_announcement_author FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT ck_announcement_audience CHECK (audience IN ('ALL', 'STUDENT', 'LECTURER'))
);
