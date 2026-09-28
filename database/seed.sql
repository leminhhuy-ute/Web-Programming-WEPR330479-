USE student_topic_management;

-- Development/demo data only. All accounts use password: Demo@12345
START TRANSACTION;

INSERT INTO departments (code, name, description) VALUES
('CNPM', 'Công nghệ phần mềm', 'Bộ môn Kỹ thuật phần mềm & hệ thống thông minh'),
('HTTT', 'Hệ thống thông tin', 'Bộ môn Cơ sở dữ liệu và hệ thống thông tin quản lý'),
('KHDL', 'Khoa học dữ liệu', 'Bộ môn Trí tuệ nhân tạo và Khoa học dữ liệu'),
('MMT', 'Mạng máy tính và truyền thông', 'Bộ môn Mạng máy tính và An toàn thông tin');

SET @bcrypt = '$2a$10$4hfOH7aylkCZGYAL9qDEUOXmcW.WfBkNUjaHomsJD2mxlk.ETpRci';

INSERT INTO users (user_code, username, password_hash, full_name, email, role, department_id, status) VALUES
('DEAN01', 'dean01', @bcrypt, 'PGS. TS. Nguyễn Văn Thành', 'dean01@dean.hcmute.edu.vn', 'DEAN', NULL, 'ACTIVE'),
('HOD01', 'hod01', @bcrypt, 'TS. Nguyễn Hoàng Long', 'hod01@headdepartment.hcmute.edu.vn', 'HEAD_OF_DEPT',
    (SELECT id FROM departments WHERE code='CNPM'), 'ACTIVE'),
('GV001', 'lecturer01', @bcrypt, 'TS. Trần Hoàng Nam', 'lecturer01@lecturer.hcmute.edu.vn', 'LECTURER',
    (SELECT id FROM departments WHERE code='CNPM'), 'ACTIVE'),
('GV002', 'lecturer02', @bcrypt, 'ThS. Đặng Thị Kim Ngân', 'lecturer02@lecturer.hcmute.edu.vn', 'LECTURER',
    (SELECT id FROM departments WHERE code='CNPM'), 'ACTIVE'),
('GV003', 'lecturer03', @bcrypt, 'TS. Lê Văn Tuấn', 'lecturer03@lecturer.hcmute.edu.vn', 'LECTURER',
    (SELECT id FROM departments WHERE code='HTTT'), 'ACTIVE'),
('GV004', 'lecturer04', @bcrypt, 'ThS. Phạm Ngọc Bích', 'lecturer04@lecturer.hcmute.edu.vn', 'LECTURER',
    (SELECT id FROM departments WHERE code='KHDL'), 'ACTIVE'),
('GV005', 'lecturer05', @bcrypt, 'TS. Võ Minh Trí', 'lecturer05@lecturer.hcmute.edu.vn', 'LECTURER',
    (SELECT id FROM departments WHERE code='MMT'), 'ACTIVE'),
('22110001', 'student01', @bcrypt, 'Nguyễn Minh Tuấn', 'student01@student.hcmute.edu.vn', 'STUDENT',
    (SELECT id FROM departments WHERE code='CNPM'), 'ACTIVE'),
('22110002', 'student02', @bcrypt, 'Trần Thu Hà', 'student02@student.hcmute.edu.vn', 'STUDENT',
    (SELECT id FROM departments WHERE code='CNPM'), 'ACTIVE'),
('22110003', 'student03', @bcrypt, 'Lê Hoàng Nam', 'student03@student.hcmute.edu.vn', 'STUDENT',
    (SELECT id FROM departments WHERE code='HTTT'), 'ACTIVE'),
('22110004', 'student04', @bcrypt, 'Phạm Ngọc Anh', 'student04@student.hcmute.edu.vn', 'STUDENT',
    (SELECT id FROM departments WHERE code='HTTT'), 'ACTIVE'),
('22110005', 'student05', @bcrypt, 'Đỗ Quốc Bảo', 'student05@student.hcmute.edu.vn', 'STUDENT',
    (SELECT id FROM departments WHERE code='MMT'), 'ACTIVE'),
('22110006', 'student06', @bcrypt, 'Vũ Mai Phương', 'student06@student.hcmute.edu.vn', 'STUDENT',
    (SELECT id FROM departments WHERE code='MMT'), 'ACTIVE');

INSERT INTO registration_periods
    (name, type, lecturer_start_at, lecturer_end_at, student_start_at, student_end_at, review_deadline, defense_date)
VALUES
('Đợt đề xuất đề tài môn học Học kỳ I năm học 2026-2027', 'COURSE',
    CURRENT_TIMESTAMP - INTERVAL 7 DAY, CURRENT_TIMESTAMP + INTERVAL 7 DAY,
    CURRENT_TIMESTAMP + INTERVAL 8 DAY, CURRENT_TIMESTAMP + INTERVAL 21 DAY, NULL, NULL),
('Đợt đăng ký đề tài sinh viên Học kỳ I năm học 2026-2027', 'COURSE',
    CURRENT_TIMESTAMP - INTERVAL 21 DAY, CURRENT_TIMESTAMP - INTERVAL 8 DAY,
    CURRENT_TIMESTAMP - INTERVAL 7 DAY, CURRENT_TIMESTAMP + INTERVAL 14 DAY, NULL, NULL),
('Đợt bảo vệ Khóa luận tốt nghiệp năm học 2026-2027', 'KLTN',
    CURRENT_TIMESTAMP - INTERVAL 30 DAY, CURRENT_TIMESTAMP - INTERVAL 15 DAY,
    CURRENT_TIMESTAMP - INTERVAL 14 DAY, CURRENT_TIMESTAMP - INTERVAL 2 DAY,
    CURRENT_TIMESTAMP + INTERVAL 12 DAY, CURRENT_DATE + INTERVAL 15 DAY);

INSERT INTO topics
    (version, topic_code, title, description, requirements, max_students, topic_type, status,
     department_id, period_id, created_by, advisor1_id)
VALUES
(0, 'DT-CNPM-202601', 'Xây dựng hệ thống quản lý đề tài và học vụ tích hợp',
 'Xây dựng ứng dụng Spring Boot & web hiện đại quản lý vòng đời đề tài và phân công hội đồng trong khoa CNTT.',
 'Java 21, Spring Boot, Spring Data JPA, H2/MySQL, HTML, CSS, JavaScript', 3, 'COURSE', 'APPROVED',
 (SELECT id FROM departments WHERE code='CNPM'),
 (SELECT id FROM registration_periods WHERE name='Đợt đề xuất đề tài môn học Học kỳ I năm học 2026-2027'),
 (SELECT id FROM users WHERE username='lecturer01'),
 (SELECT id FROM users WHERE username='lecturer01')),
(0, 'DT-CNPM-202602', 'Xây dựng cổng thông tin và hoạt động ngoại khóa dành cho sinh viên HCMUTE',
 'Thiết kế và triển khai cổng thông tin điện tử kết nối hoạt động Đoàn - Hội và nghiên cứu khoa học sinh viên.',
 'Spring Boot, REST API, Responsive Web Design, Bootstrap / Vanilla CSS', 3, 'COURSE', 'APPROVED',
 (SELECT id FROM departments WHERE code='CNPM'),
 (SELECT id FROM registration_periods WHERE name='Đợt đăng ký đề tài sinh viên Học kỳ I năm học 2026-2027'),
 (SELECT id FROM users WHERE username='lecturer01'),
 (SELECT id FROM users WHERE username='lecturer01')),
(0, 'DT-CNPM-202603', 'Nghiên cứu ứng dụng Deep Learning trong nhận diện và phân loại bệnh lý mắt',
 'Sử dụng mạng nơ-ron tích chập (CNN) và Vision Transformer để hỗ trợ chẩn đoán hình ảnh nhãn khoa.',
 'Python, PyTorch, FastAPI, JavaScript, kiến thức nền tảng về Deep Learning', 2, 'COURSE', 'PENDING',
 (SELECT id FROM departments WHERE code='CNPM'),
 (SELECT id FROM registration_periods WHERE name='Đợt đề xuất đề tài môn học Học kỳ I năm học 2026-2027'),
 (SELECT id FROM users WHERE username='lecturer02'), NULL),
(0, 'DT-HTTT-202601', 'Xây dựng hệ thống hoạch định nguồn lực doanh nghiệp (ERP) cho chuỗi bán lẻ',
 'Ứng dụng kiến trúc Microservices trong quản lý kho hàng, điểm bán lẻ và báo cáo kinh doanh thông minh (BI).',
 'Java, Spring Cloud, PostgreSQL / MySQL, Docker, React / HTML5', 3, 'COURSE', 'APPROVED',
 (SELECT id FROM departments WHERE code='HTTT'),
 (SELECT id FROM registration_periods WHERE name='Đợt đề xuất đề tài môn học Học kỳ I năm học 2026-2027'),
 (SELECT id FROM users WHERE username='lecturer03'),
 (SELECT id FROM users WHERE username='lecturer03')),
(0, 'DT-KHDL-202601', 'Phân tích cảm xúc và dự báo xu hướng dư luận mạng xã hội bằng mô hình ngôn ngữ lớn (LLM)',
 'Thu thập dữ liệu tiếng Việt đa chiều, tinh chỉnh mô hình nền tảng (LLM fine-tuning) để phân loại cảm xúc tự động.',
 'Python, Transformers, NLP tiếng Việt, LangChain, Web Dashboard', 2, 'COURSE', 'APPROVED',
 (SELECT id FROM departments WHERE code='KHDL'),
 (SELECT id FROM registration_periods WHERE name='Đợt đề xuất đề tài môn học Học kỳ I năm học 2026-2027'),
 (SELECT id FROM users WHERE username='lecturer04'),
 (SELECT id FROM users WHERE username='lecturer04')),
(0, 'DT-MMT-202601', 'Hệ thống phát hiện xâm nhập mạng (NIDS) thông minh ứng dụng Machine Learning',
 'Phân tích lưu lượng gói tin mạng trong thời gian thực nhằm phát hiện các cuộc tấn công DDoS, quét cổng và mã độc.',
 'Python, C++, Network Security, Wireshark, Scikit-learn', 2, 'COURSE', 'APPROVED',
 (SELECT id FROM departments WHERE code='MMT'),
 (SELECT id FROM registration_periods WHERE name='Đợt đề xuất đề tài môn học Học kỳ I năm học 2026-2027'),
 (SELECT id FROM users WHERE username='lecturer05'),
 (SELECT id FROM users WHERE username='lecturer05'));

INSERT INTO student_groups
    (version, group_code, group_name, topic_id, leader_id, member_count, status, registered_at)
VALUES
(0, 'GRP-202601', 'Nhóm Nghiên cứu Kỹ thuật Phần mềm UTE',
 (SELECT id FROM topics WHERE topic_code='DT-CNPM-202602'),
 (SELECT id FROM users WHERE username='student01'), 2, 'APPROVED', CURRENT_TIMESTAMP - INTERVAL 5 DAY);

INSERT INTO group_members (group_id, student_id, member_role, joined_at) VALUES
((SELECT id FROM student_groups WHERE group_code='GRP-202601'), (SELECT id FROM users WHERE username='student01'), 'LEADER', CURRENT_TIMESTAMP - INTERVAL 5 DAY),
((SELECT id FROM student_groups WHERE group_code='GRP-202601'), (SELECT id FROM users WHERE username='student02'), 'MEMBER', CURRENT_TIMESTAMP - INTERVAL 4 DAY);

INSERT INTO councils (code, name, defense_date, room, status) VALUES
('HD-CNTT-01', 'Hội đồng Đánh giá Khóa luận Tốt nghiệp CNTT 01', CURRENT_TIMESTAMP + INTERVAL 15 DAY, 'A1-302', 'READY');

INSERT INTO council_members (council_id, lecturer_id, role) VALUES
((SELECT id FROM councils WHERE code='HD-CNTT-01'), (SELECT id FROM users WHERE username='dean01'), 'CHAIRPERSON'),
((SELECT id FROM councils WHERE code='HD-CNTT-01'), (SELECT id FROM users WHERE username='hod01'), 'SECRETARY'),
((SELECT id FROM councils WHERE code='HD-CNTT-01'), (SELECT id FROM users WHERE username='lecturer05'), 'REVIEWER'),
((SELECT id FROM councils WHERE code='HD-CNTT-01'), (SELECT id FROM users WHERE username='lecturer04'), 'MEMBER'),
((SELECT id FROM councils WHERE code='HD-CNTT-01'), (SELECT id FROM users WHERE username='lecturer03'), 'MEMBER');

INSERT INTO announcements (title, content, audience, created_by) VALUES
('Kế hoạch đề xuất đề tài học vụ năm học 2026-2027',
 'Kính gửi quý Thầy Cô và các bạn Sinh viên, Khoa CNTT chính thức mở cổng tiếp nhận đề xuất và đăng ký đề tài cho năm học mới.', 'ALL',
 (SELECT id FROM users WHERE username='dean01')),
('Hướng dẫn dành cho giảng viên',
 'Quý Thầy Cô vui lòng kiểm tra đợt đăng ký và cơ cấu bộ môn trước khi gửi đề xuất đề tài. Mỗi giảng viên hướng dẫn không vượt quá định mức tối đa.', 'LECTURER',
 (SELECT id FROM users WHERE username='dean01')),
('Hướng dẫn dành cho sinh viên',
 'Nhóm trưởng đại diện gửi đăng ký đề tài, theo dõi kết quả duyệt từ GVHD và nộp báo cáo tiến độ đúng thời hạn quy định.', 'STUDENT',
 (SELECT id FROM users WHERE username='dean01'));

COMMIT;
