ALTER TABLE student_reports ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING';
ALTER TABLE student_reports ADD COLUMN edit_count INT NOT NULL DEFAULT 0;
ALTER TABLE student_reports ADD COLUMN status_updated_at DATETIME(6) NULL;
ALTER TABLE student_reports ADD COLUMN status_updated_by_id BIGINT NULL;
ALTER TABLE student_reports ADD CONSTRAINT ck_student_report_status
    CHECK (status IN ('PENDING', 'REJECTED', 'APPROVED'));
ALTER TABLE student_reports ADD CONSTRAINT fk_student_report_status_author
    FOREIGN KEY (status_updated_by_id) REFERENCES users(id);

CREATE TABLE student_report_edits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_id BIGINT NOT NULL,
    changed_by_id BIGINT NOT NULL,
    edited_at DATETIME(6) NOT NULL,
    filename VARCHAR(180) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    note VARCHAR(1000) NOT NULL,
    report_size BIGINT NOT NULL,
    checksum VARCHAR(64) NULL,
    content LONGBLOB NOT NULL,
    CONSTRAINT fk_student_report_edits_report FOREIGN KEY (report_id) REFERENCES student_reports(id) ON DELETE CASCADE,
    CONSTRAINT fk_student_report_edits_user FOREIGN KEY (changed_by_id) REFERENCES users(id)
);
