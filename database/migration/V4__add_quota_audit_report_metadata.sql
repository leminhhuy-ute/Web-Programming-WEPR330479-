USE student_topic_management;

UPDATE topics SET description = '' WHERE description IS NULL;
ALTER TABLE topics MODIFY description TEXT NOT NULL;

CREATE TABLE advisor_quotas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NULL,
    registration_period_id BIGINT NOT NULL,
    lecturer_id BIGINT NOT NULL,
    max_groups INT NOT NULL,
    CONSTRAINT uk_quota_period_lecturer UNIQUE (registration_period_id, lecturer_id),
    CONSTRAINT fk_quota_period FOREIGN KEY (registration_period_id) REFERENCES registration_periods(id),
    CONSTRAINT fk_quota_lecturer FOREIGN KEY (lecturer_id) REFERENCES users(id),
    CONSTRAINT ck_quota_max CHECK (max_groups BETWEEN 1 AND 100)
);

ALTER TABLE student_reports
    ADD COLUMN report_size BIGINT NULL,
    ADD COLUMN checksum VARCHAR(64) NULL,
    ADD COLUMN storage_path VARCHAR(500) NULL;

UPDATE student_reports SET report_size = OCTET_LENGTH(content) WHERE report_size IS NULL;

CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    action VARCHAR(80) NOT NULL,
    entity_name VARCHAR(80) NOT NULL,
    entity_id VARCHAR(80) NULL,
    old_value LONGTEXT NULL,
    new_value LONGTEXT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ip_address VARCHAR(64) NULL,
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_audit_entity (entity_name, entity_id),
    INDEX idx_audit_user_created (user_id, created_at)
);
