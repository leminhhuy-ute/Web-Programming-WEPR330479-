ALTER TABLE announcements ADD COLUMN department_id BIGINT NULL;
ALTER TABLE announcements ADD CONSTRAINT fk_announcements_department FOREIGN KEY (department_id) REFERENCES departments(id);
ALTER TABLE announcements DROP CONSTRAINT ck_announcements_audience;
ALTER TABLE announcements ADD CONSTRAINT ck_announcements_audience CHECK (audience IN ('ALL', 'HEAD_OF_DEPT', 'LECTURER', 'STUDENT'));

CREATE TABLE announcement_reads (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  announcement_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  read_at DATETIME(6) NOT NULL,
  CONSTRAINT uk_announcement_read UNIQUE (announcement_id, user_id),
  CONSTRAINT fk_announcement_read_announcement FOREIGN KEY (announcement_id) REFERENCES announcements(id) ON DELETE CASCADE,
  CONSTRAINT fk_announcement_read_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

ALTER TABLE student_reports ADD COLUMN advisor_feedback TEXT NULL;
ALTER TABLE student_reports ADD COLUMN feedback_by_id BIGINT NULL;
ALTER TABLE student_reports ADD COLUMN feedback_at DATETIME(6) NULL;
ALTER TABLE student_reports ADD CONSTRAINT fk_student_report_feedback_author FOREIGN KEY (feedback_by_id) REFERENCES users(id);
