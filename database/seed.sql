-- ========================================================
-- HỆ THỐNG QUẢN LÝ ĐỀ TÀI SINH VIÊN - HCMUTE
-- UNIFIED SEED DATA
-- ========================================================

USE student_topic_management;

-- 1. Departments
INSERT INTO departments (id, code, name, description) VALUES
(1, 'CNPM', 'Bộ môn Công nghệ Phần mềm', 'Khoa Công nghệ Thông tin - HCMUTE'),
(2, 'KHMT', 'Bộ môn Khoa học Máy tính', 'Khoa Công nghệ Thông tin - HCMUTE'),
(3, 'HTTT', 'Bộ môn Hệ thống Thông tin', 'Khoa Công nghệ Thông tin - HCMUTE'),
(4, 'KTMT', 'Bộ môn Kỹ thuật Máy tính', 'Khoa Công nghệ Thông tin - HCMUTE'),
(5, 'ATTT', 'Bộ môn An toàn Thông tin', 'Khoa Công nghệ Thông tin - HCMUTE')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 2. Users (Password BCrypt: $2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a = 'Password@123')
INSERT INTO users (id, user_code, username, password_hash, full_name, email, role, department_id, status) VALUES
-- Trưởng khoa
(1, 'DEAN01', 'dean', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'PGS.TS. Trần Văn Trưởng Khoa', 'dean@hcmute.edu.vn', 'DEAN', 1, 'ACTIVE'),

-- Trưởng bộ môn
(2, 'HEAD01', 'head_cnpm', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'TS. Nguyễn Thị Trưởng Bộ Môn', 'head.cnpm@hcmute.edu.vn', 'HEAD_OF_DEPT', 1, 'ACTIVE'),

-- Giảng viên
(3, 'GV001', 'lecturer1', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'TS. Nguyễn Hoàng Nam', 'nam.nh@hcmute.edu.vn', 'LECTURER', 1, 'ACTIVE'),
(4, 'GV002', 'lecturer2', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'PGS.TS. Trần Thị Minh Châu', 'chau.ttm@hcmute.edu.vn', 'LECTURER', 2, 'ACTIVE'),
(5, 'GV003', 'reviewer1', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'ThS. Vũ Hải Đăng', 'dang.vh@hcmute.edu.vn', 'LECTURER', 5, 'ACTIVE'),

-- Sinh viên nhóm đồ án HCMUTE
(6, '24110019', '24110019', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Lê Minh Huy', '24110019@student.hcmute.edu.vn', 'STUDENT', 1, 'ACTIVE'),
(7, '24110012', '24110012', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Trần Hữu Thành Đô', '24110012@student.hcmute.edu.vn', 'STUDENT', 1, 'ACTIVE'),
(8, '24162043', '24162043', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Lê Bảo Huy', '24162043@student.hcmute.edu.vn', 'STUDENT', 1, 'ACTIVE'),
(9, '24162048', '24162048', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Nguyễn Quang Huy', '24162048@student.hcmute.edu.vn', 'STUDENT', 1, 'ACTIVE'),
(10, '24110033', '24110033', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Lê Đăng Minh', '24110033@student.hcmute.edu.vn', 'STUDENT', 1, 'ACTIVE'),

-- Sinh viên demo thêm
(11, '22110001', '22110001', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Nguyễn Minh Anh', '22110001@student.hcmute.edu.vn', 'STUDENT', 1, 'ACTIVE'),
(12, '22110002', '22110002', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Trần Ngọc Linh', '22110002@student.hcmute.edu.vn', 'STUDENT', 1, 'ACTIVE')
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

-- 3. Registration Periods
INSERT INTO registration_periods (id, name, type, lecturer_start_at, lecturer_end_at, student_start_at, student_end_at, review_deadline, defense_date) VALUES
(1, 'Khóa luận tốt nghiệp HK1 2026-2027', 'KLTN', '2026-08-15 08:00:00', '2026-09-01 17:00:00', '2026-09-05 08:00:00', '2026-10-15 17:00:00', '2026-12-15 17:00:00', '2026-12-28'),
(2, 'Tiểu luận chuyên ngành HK1 2026-2027', 'TLCN', '2026-08-20 08:00:00', '2026-09-05 17:00:00', '2026-09-10 08:00:00', '2026-10-20 17:00:00', '2026-12-20 17:00:00', NULL),
(3, 'Nghiên cứu khoa học sinh viên 2026', 'NCKH', '2026-08-01 08:00:00', '2026-08-30 17:00:00', '2026-09-01 08:00:00', '2026-11-01 17:00:00', NULL, NULL),
(4, 'Đồ án môn học Web Programming HK1', 'COURSE', '2026-09-01 08:00:00', '2026-09-15 17:00:00', '2026-09-16 08:00:00', '2026-10-30 17:00:00', NULL, NULL)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 4. Announcements
INSERT INTO announcements (id, title, content, audience, created_by, created_at) VALUES
(1, 'Thông báo khởi động đợt đăng ký Khóa luận tốt nghiệp HK1', 'Khoa CNTT thông báo lịch đăng ký đề tài KLTN cho sinh viên năm cuối. Đề nghị các nhóm sinh viên hoàn thành lập nhóm và chọn đề tài đúng hạn.', 'ALL', 1, NOW()),
(2, 'Nhắc nhở Giảng viên cập nhật danh mục đề tài', 'Kính đề nghị quý Thầy/Cô bộ môn gửi đề xuất đề tài lên hệ thống trước ngày 01/09 để Hội đồng khoa phê duyệt.', 'LECTURER', 1, NOW()),
(3, 'Hướng dẫn nộp đề cương và quy cách trình bày báo cáo', 'Các nhóm sinh viên đã được duyệt đề tài chú ý tải mẫu đề cương chi tiết tại thư mục tài liệu và nộp đúng hạn cột mốc 1.', 'STUDENT', 1, NOW())
ON DUPLICATE KEY UPDATE title=VALUES(title);

-- 5. Topics
INSERT INTO topics (id, topic_code, title, description, requirements, max_students, topic_type, status, department_id, period_id, created_by, advisor1_id, advisor2_id) VALUES
(1, 'KLTN26-001', 'Hệ thống Quản lý Chuỗi cung ứng nông sản ứng dụng Blockchain và IoT', 'Nghiên cứu giải pháp truy xuất nguồn gốc nông sản từ nông trại đến bàn ăn, lưu trữ hash giao dịch trên Blockchain Polygon/Ethereum.', 'Java Spring Boot, Solidity, Smart Contracts, MQTT, Docker', 3, 'KLTN', 'APPROVED', 1, 1, 3, 3, 4),
(2, 'KLTN26-002', 'Ứng dụng Trí tuệ Nhân tạo trong chẩn đoán hình ảnh X-quang phổi', 'Xây dựng mô hình Vision Transformer phân loại tổn thương phổi và phát hiện viêm phổi từ ảnh X-quang y tế.', 'Python PyTorch, FastAPI, Spring Boot, TensorFlow', 3, 'KLTN', 'APPROVED', 2, 1, 4, 4, NULL),
(3, 'TLCN26-001', 'Hệ sinh thái Quản lý Đồ án tốt nghiệp và Kết nối Tuyển dụng', 'Xây dựng web app quản lý toàn diện quy trình làm đồ án sinh viên và hỗ trợ doanh nghiệp tiếp cận sản phẩm đồ án.', 'Spring Boot, Thymeleaf, MySQL, Spring Security', 3, 'TLCN', 'APPROVED', 1, 2, 3, 3, NULL),
(4, 'NCKH26-001', 'Hệ thống phát hiện xâm nhập mạng (NIDS) sử dụng Machine Learning', 'Phân tích gói tin mạng thời gian thực, phát hiện bất thường bằng giải thuật XGBoost và Random Forest.', 'Python Scapy, Spring Boot, Snort, Chart.js', 2, 'NCKH', 'APPROVED', 5, 3, 5, 5, NULL)
ON DUPLICATE KEY UPDATE title=VALUES(title);

-- 6. Student Groups
INSERT INTO student_groups (id, group_code, group_name, leader_id, topic_id, member_count, max_members, status, notes) VALUES
(1, 'GRP-01', 'Alpha Dev Team', 6, 1, 3, 3, 'APPROVED', 'Nhóm chuyên trách đề tài Blockchain & IoT'),
(2, 'GRP-02', 'Web Programming Crew', 9, 3, 2, 3, 'PENDING', 'Nhóm đăng ký đề tài Quản lý đồ án tốt nghiệp')
ON DUPLICATE KEY UPDATE group_name=VALUES(group_name);

-- 7. Group Members
INSERT INTO group_members (group_id, student_id, member_role) VALUES
(1, 6, 'LEADER'),
(1, 7, 'MEMBER'),
(1, 8, 'MEMBER'),
(2, 9, 'LEADER'),
(2, 10, 'MEMBER')
ON DUPLICATE KEY UPDATE member_role=VALUES(member_role);

-- 8. Group Invitations
INSERT INTO group_invitations (id, group_id, inviter_id, invitee_id, message, status) VALUES
(1, 2, 9, 11, 'Mời bạn Minh Anh tham gia nhóm làm đề tài Web Programming nhé!', 'PENDING')
ON DUPLICATE KEY UPDATE status=VALUES(status);

-- 9. Topic Registrations
INSERT INTO topic_registrations (id, group_id, topic_id, status, proposal_note, supervisor_feedback, reviewed_at) VALUES
(1, 1, 1, 'APPROVED', 'Nhóm đã có kinh nghiệm lập trình Spring Boot và Solidity, mong muốn được thầy Nam hướng dẫn.', 'Đề tài phù hợp năng lực nhóm. Thầy đồng ý hướng dẫn.', NOW())
ON DUPLICATE KEY UPDATE status=VALUES(status);

-- 10. Progress Reports
INSERT INTO progress_reports (id, group_id, submitted_by, report_title, stage, content_summary, attachment_url, file_name, completion_percentage, supervisor_score, supervisor_feedback, reviewed_at) VALUES
(1, 1, 6, 'Báo cáo Đề cương chi tiết và Đặc tả yêu cầu SRS', 'Đề cương', 'Nhóm đã hoàn thành tài liệu SRS, ERD cơ sở dữ liệu và dựng khung mã nguồn ban đầu của hệ thống.', 'https://github.com/alphadev/supply-chain-docs/releases/tag/v1.0-srs.pdf', 'De_cuong_SRS_AlphaDev.pdf', 100, 9.2, 'Đề cương chuẩn bị rất tốt và chi tiết. Tiếp tục triển khai phase 2.', NOW())
ON DUPLICATE KEY UPDATE report_title=VALUES(report_title);

-- 11. Councils
INSERT INTO councils (id, code, name, department_id, period_id, status, defense_date, defense_time, room) VALUES
(1, 'HD-CNPM-01', 'Hội đồng Bảo vệ Khóa luận tốt nghiệp CNPM 01', 1, 1, 'ACTIVE', '2026-12-28', '08:30', 'Phòng A1-402, Tòa nhà Trung tâm')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 12. Council Members
INSERT INTO council_members (council_id, lecturer_id, member_role) VALUES
(1, 2, 'CHAIR'),     -- TS. Nguyễn Thị Trưởng Bộ Môn (Chủ tịch)
(1, 4, 'SECRETARY'), -- PGS.TS. Trần Thị Minh Châu (Thư ký)
(1, 5, 'REVIEWER')   -- ThS. Vũ Hải Đăng (Phản biện)
ON DUPLICATE KEY UPDATE member_role=VALUES(member_role);

-- 13. Evaluations
INSERT INTO evaluations (id, council_id, topic_id, evaluator_id, evaluation_type, score, feedback) VALUES
(1, 1, 1, 5, 'REVIEWER', 9.0, 'Đề tài mang tính thời sự cao, sản phẩm chạy ổn định, mã nguồn tổ chức khoa học.')
ON DUPLICATE KEY UPDATE score=VALUES(score);
