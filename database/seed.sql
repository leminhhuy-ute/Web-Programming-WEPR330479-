USE student_topic_management;

-- Demo password for every account: Demo@12345.
-- Stored value is BCrypt, never plaintext.

START TRANSACTION;

-- =========================================================
-- Departments
-- =========================================================
INSERT INTO departments (code, name, description) VALUES
('CNPM', 'Công nghệ phần mềm', 'Phát triển và kiểm thử phần mềm'),
('HTTT', 'Hệ thống thông tin', 'Cơ sở dữ liệu và hệ thống thông tin'),
('MMT', 'Mạng máy tính', 'Mạng máy tính và an toàn thông tin');


-- =========================================================
-- Users
-- =========================================================
INSERT INTO users (
    user_code,
    username,
    password_hash,
    full_name,
    email,
    role,
    department_id,
    status
) VALUES

-- Trưởng khoa
(
    'DEAN01',
    'dean01',
    '$2a$10$4hfOH7aylkCZGYAL9qDEUOXmcW.WfBkNUjaHomsJD2mxlk.ETpRci',
    'Nguyễn Quang Huy',
    'dean01@hcmute.edu.vn',
    'DEAN',
    NULL,
    'ACTIVE'
),

-- Giảng viên
(
    'GV001',
    'lecturer01',
    '$2a$10$4hfOH7aylkCZGYAL9qDEUOXmcW.WfBkNUjaHomsJD2mxlk.ETpRci',
    'Nguyễn Minh Anh',
    'lecturer01@hcmute.edu.vn',
    'LECTURER',
    (SELECT id FROM departments WHERE code = 'CNPM'),
    'ACTIVE'
),

-- Sinh viên
(
    'SV001',
    'student01',
    '$2a$10$4hfOH7aylkCZGYAL9qDEUOXmcW.WfBkNUjaHomsJD2mxlk.ETpRci',
    'Trần Hoàng Nam',
    'student01@student.hcmute.edu.vn',
    'STUDENT',
    (SELECT id FROM departments WHERE code = 'HTTT'),
    'ACTIVE'
);


-- =========================================================
-- Registration periods
-- =========================================================
INSERT INTO registration_periods (
    name,
    type,
    lecturer_start_at,
    lecturer_end_at,
    student_start_at,
    student_end_at,
    review_deadline,
    defense_date
) VALUES

(
    'Môn học — Học kỳ 1, 2026–2027',
    'COURSE',
    '2026-09-01 08:00:00',
    '2026-09-10 17:00:00',
    '2026-09-11 08:00:00',
    '2026-09-20 17:00:00',
    NULL,
    NULL
),

(
    'Nghiên cứu khoa học 2026',
    'NCKH',
    '2026-08-01 08:00:00',
    '2026-08-10 17:00:00',
    '2026-08-11 08:00:00',
    '2026-08-25 17:00:00',
    NULL,
    NULL
),

(
    'Tiểu luận chuyên ngành — Học kỳ 1',
    'TLCN',
    '2026-09-15 08:00:00',
    '2026-09-25 17:00:00',
    '2026-09-26 08:00:00',
    '2026-10-05 17:00:00',
    '2026-12-20 17:00:00',
    NULL
),

(
    'Khóa luận tốt nghiệp — Học kỳ 1',
    'KLTN',
    '2026-09-01 08:00:00',
    '2026-09-05 17:00:00',
    '2026-09-06 08:00:00',
    '2026-09-15 17:00:00',
    '2027-01-05 17:00:00',
    '2027-01-12'
);


-- =========================================================
-- Announcements
-- =========================================================
INSERT INTO announcements (
    title,
    content,
    audience,
    created_by
) VALUES

(
    'Lịch đăng ký đề tài học kỳ 1',
    'Giảng viên và sinh viên theo dõi các mốc thời gian đăng ký đề tài của khoa.',
    'ALL',
    (SELECT id FROM users WHERE user_code = 'DEAN01')
),

(
    'Thông tin dành cho giảng viên',
    'Kính mời giảng viên theo dõi lịch đăng ký đề tài học kỳ mới.',
    'LECTURER',
    (SELECT id FROM users WHERE user_code = 'DEAN01')
),

(
    'Hướng dẫn theo dõi đợt đăng ký',
    'Sinh viên vui lòng kiểm tra thông báo và lịch đăng ký trước khi bắt đầu học kỳ.',
    'STUDENT',
    (SELECT id FROM users WHERE user_code = 'DEAN01')
);

COMMIT;