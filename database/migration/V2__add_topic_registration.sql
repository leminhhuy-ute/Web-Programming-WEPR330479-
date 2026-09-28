USE student_topic_management;

CREATE TABLE topic_registrations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NULL,
    group_id BIGINT NOT NULL,
    topic_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    decided_by BIGINT NULL,
    decided_at DATETIME(6) NULL,
    reason VARCHAR(2000) NULL,
    CONSTRAINT fk_registrations_group FOREIGN KEY (group_id) REFERENCES student_groups(id) ON DELETE CASCADE,
    CONSTRAINT fk_registrations_topic FOREIGN KEY (topic_id) REFERENCES topics(id),
    CONSTRAINT fk_registrations_decider FOREIGN KEY (decided_by) REFERENCES users(id),
    CONSTRAINT ck_registration_status CHECK (status IN ('DRAFT','PENDING','APPROVED','REJECTED','CANCELLED')),
    INDEX idx_registration_group_created (group_id, created_at),
    INDEX idx_registration_topic_status (topic_id, status)
);

INSERT INTO topic_registrations (group_id, topic_id, status, created_at, updated_at, reason)
SELECT id, topic_id, status, registered_at, registered_at, notes
FROM student_groups
WHERE topic_id IS NOT NULL;
