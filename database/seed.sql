USE student_topic_management;

-- Demo password for all accounts: Demo@12345
-- Hash: $2a$10$4hfOH7aylkCZGYAL9qDEUOXmcW.WfBkNUjaHomsJD2mxlk.ETpRci

START TRANSACTION;

-- Departments
INSERT INTO departments (code, name, description) VALUES
('CNPM', 'Công nghệ phần mềm', 'Phát triển phần mềm, ứng dụng web và di động'),
('HTTT', 'Hệ thống thông tin', 'Cơ sở dữ liệu, phân tích dữ liệu và hệ thống doanh nghiệp'),
('MMT', 'Mạng máy tính và An ninh mạng', 'Hạ tầng mạng, điện toán đám mây và an toàn thông tin');

-- Users
INSERT INTO users (user_code, username, password_hash, full_name, email, role, department_id, status) VALUES
-- Trưởng khoa
('DEAN01', 'dean01', '$2a$10$4hfOH7aylkCZGYAL9qDEUOXmcW.WfBkNUjaHomsJD2mxlk.ETpRci', 'Nguyễn Quang Huy', 'dean01@hcmute.edu.vn', 'DEAN', NULL, 'ACTIVE'),

-- Trưởng bộ môn CNPM
('HOD01', 'hod_cnpm', '$2a$10$4hfOH7aylkCZGYAL9qDEUOXmcW.WfBkNUjaHomsJD2mxlk.ETpRci', 'Phạm Văn Nam', 'hod.cnpm@hcmute.edu.vn', 'HEAD_OF_DEPT', (SELECT id FROM departments WHERE code = 'CNPM'), 'ACTIVE'),

-- Giảng viên CNPM
('GV001', 'lecturer01', '$2a$10$4hfOH7aylkCZGYAL9qDEUOXmcW.WfBkNUjaHomsJD2mxlk.ETpRci', 'Nguyễn Minh Anh', 'lecturer01@hcmute.edu.vn', 'LECTURER', (SELECT id FROM departments WHERE code = 'CNPM'), 'ACTIVE'),
('GV002', 'lecturer02', '$2a$10$4hfOH7aylkCZGYAL9qDEUOXmcW.WfBkNUjaHomsJD2mxlk.ETpRci', 'Trần Thị Thu', 'lecturer02@hcmute.edu.vn', 'LECTURER', (SELECT id FROM departments WHERE code = 'CNPM'), 'ACTIVE'),

-- Giảng viên HTTT
('GV003', 'lecturer03', '$2a$10$4hfOH7aylkCZGYAL9qDEUOXmcW.WfBkNUjaHomsJD2mxlk.ETpRci', 'Lê Hoàng Long', 'lecturer03@hcmute.edu.vn', 'LECTURER', (SELECT id FROM departments WHERE code = 'HTTT'), 'ACTIVE'),

-- Sinh viên
('SV001', 'student01', '$2a$10$4hfOH7aylkCZGYAL9qDEUOXmcW.WfBkNUjaHomsJD2mxlk.ETpRci', 'Đặng Quốc Anh', 'student01@student.hcmute.edu.vn', 'STUDENT', (SELECT id FROM departments WHERE code = 'CNPM'), 'ACTIVE'),
('SV002', 'student02', '$2a$10$4hfOH7aylkCZGYAL9qDEUOXmcW.WfBkNUjaHomsJD2mxlk.ETpRci', 'Vũ Đức Cường', 'student02@student.hcmute.edu.vn', 'STUDENT', (SELECT id FROM departments WHERE code = 'CNPM'), 'ACTIVE'),
('SV003', 'student03', '$2a$10$4hfOH7aylkCZGYAL9qDEUOXmcW.WfBkNUjaHomsJD2mxlk.ETpRci', 'Hoàng Bảo Ngọc', 'student03@student.hcmute.edu.vn', 'STUDENT', (SELECT id FROM departments WHERE code = 'HTTT'), 'ACTIVE');

-- Registration Periods
INSERT INTO registration_periods (name, type, lecturer_start_at, lecturer_end_at, student_start_at, student_end_at, review_deadline, defense_date) VALUES
('Đăng ký đề tài Môn học — HK1 2026-2027', 'COURSE', '2026-09-01 08:00:00', '2026-09-15 17:00:00', '2026-09-16 08:00:00', '2026-09-30 17:00:00', NULL, NULL),
('Nghiên cứu khoa học sinh viên 2026', 'NCKH', '2026-08-01 08:00:00', '2026-08-20 17:00:00', '2026-08-21 08:00:00', '2026-09-05 17:00:00', NULL, NULL),
('Tiểu luận chuyên ngành — HK1 2026-2027', 'TLCN', '2026-09-01 08:00:00', '2026-09-20 17:00:00', '2026-09-21 08:00:00', '2026-10-05 17:00:00', '2026-12-15 17:00:00', NULL),
('Khóa luận tốt nghiệp — HK1 2026-2027', 'KLTN', '2026-09-01 08:00:00', '2026-09-10 17:00:00', '2026-09-11 08:00:00', '2026-09-25 17:00:00', '2027-01-05 17:00:00', '2027-01-15');

-- Topics
INSERT INTO topics (topic_code, title, description, requirements, max_students, topic_type, status, rejection_reason, department_id, period_id, created_by, advisor1_id, advisor2_id) VALUES
(
    'DT-CNPM-001',
    'Xây dựng hệ thống quản lý đề tài khoa CNTT bằng Spring Framework',
    'Phát triển ứng dụng Web quản lý đề tài khóa luận, tiểu luận cho sinh viên và giảng viên.',
    'Kiến thức vững về Java, Spring MVC, JPA/Hibernate, SQL MySQL và HTML/CSS/JS.',
    2,
    'KLTN',
    'APPROVED',
    NULL,
    (SELECT id FROM departments WHERE code = 'CNPM'),
    (SELECT id FROM registration_periods WHERE type = 'KLTN' LIMIT 1),
    (SELECT id FROM users WHERE user_code = 'GV001'),
    (SELECT id FROM users WHERE user_code = 'GV001'),
    (SELECT id FROM users WHERE user_code = 'GV002')
),
(
    'DT-CNPM-002',
    'Ứng dụng Trí tuệ nhân tạo hỗ trợ chấm bài tập tự động',
    'Nghiên cứu mô hình LLM để đánh giá và nhận xét mã nguồn sinh viên.',
    'Thành thạo Python, OpenAI API / Local LLM, Spring Boot/MVC REST API.',
    3,
    'NCKH',
    'APPROVED',
    NULL,
    (SELECT id FROM departments WHERE code = 'CNPM'),
    (SELECT id FROM registration_periods WHERE type = 'NCKH' LIMIT 1),
    (SELECT id FROM users WHERE user_code = 'GV002'),
    (SELECT id FROM users WHERE user_code = 'GV002'),
    NULL
),
(
    'DT-CNPM-003',
    'Phân tích và thiết kế hệ thống thương mại điện tử microservices',
    'Đề xuất kiến trúc microservices tối ưu cho trang thương mại điện tử lớn.',
    'Có kinh nghiệm thiết kế cơ sở dữ liệu và Spring Cloud.',
    2,
    'TLCN',
    'PENDING',
    NULL,
    (SELECT id FROM departments WHERE code = 'CNPM'),
    (SELECT id FROM registration_periods WHERE type = 'TLCN' LIMIT 1),
    (SELECT id FROM users WHERE user_code = 'GV001'),
    NULL,
    NULL
),
(
    'DT-HTTT-001',
    'Xây dựng kho dữ liệu và Dashboard phân tích học tập sinh viên',
    'Xây dựng hệ thống Data Warehouse và Tableau Dashboard cho nhà trường.',
    'Hiểu biết về ETL, SQL Server/MySQL và PowerBI/Tableau.',
    2,
    'COURSE',
    'APPROVED',
    NULL,
    (SELECT id FROM departments WHERE code = 'HTTT'),
    (SELECT id FROM registration_periods WHERE type = 'COURSE' LIMIT 1),
    (SELECT id FROM users WHERE user_code = 'GV003'),
    (SELECT id FROM users WHERE user_code = 'GV003'),
    NULL
);

-- Student Groups
INSERT INTO student_groups (group_code, group_name, topic_id, leader_id, member_count, status, notes) VALUES
(
    'GRP-001',
    'Nhóm Sáng Tạo CNPM',
    (SELECT id FROM topics WHERE topic_code = 'DT-CNPM-001'),
    (SELECT id FROM users WHERE user_code = 'SV001'),
    2,
    'APPROVED',
    'Nhóm có định hướng phát triển bài bản và cam kết tiến độ.'
),
(
    'GRP-002',
    'Nhóm NCKH AI',
    (SELECT id FROM topics WHERE topic_code = 'DT-CNPM-002'),
    (SELECT id FROM users WHERE user_code = 'SV002'),
    1,
    'PENDING',
    'Mong muốn tìm hiểu sâu về AI và LLM.'
);

-- Group Members
INSERT INTO group_members (group_id, student_id, member_role) VALUES
((SELECT id FROM student_groups WHERE group_code = 'GRP-001'), (SELECT id FROM users WHERE user_code = 'SV001'), 'LEADER'),
((SELECT id FROM student_groups WHERE group_code = 'GRP-001'), (SELECT id FROM users WHERE user_code = 'SV002'), 'MEMBER'),
((SELECT id FROM student_groups WHERE group_code = 'GRP-002'), (SELECT id FROM users WHERE user_code = 'SV002'), 'LEADER');

COMMIT;
