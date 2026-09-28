USE student_topic_management;

CREATE TABLE registration_status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    registration_id BIGINT NOT NULL,
    old_status VARCHAR(20) NULL,
    new_status VARCHAR(20) NOT NULL,
    changed_by BIGINT NOT NULL,
    changed_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    note VARCHAR(2000) NULL,
    CONSTRAINT fk_registration_history_registration FOREIGN KEY (registration_id) REFERENCES topic_registrations(id) ON DELETE CASCADE,
    CONSTRAINT fk_registration_history_actor FOREIGN KEY (changed_by) REFERENCES users(id),
    CONSTRAINT ck_registration_history_old CHECK (old_status IS NULL OR old_status IN ('DRAFT','PENDING','APPROVED','REJECTED','CANCELLED')),
    CONSTRAINT ck_registration_history_new CHECK (new_status IN ('DRAFT','PENDING','APPROVED','REJECTED','CANCELLED')),
    INDEX idx_registration_history (registration_id, changed_at)
);

INSERT INTO registration_status_history (registration_id, old_status, new_status, changed_by, changed_at, note)
SELECT tr.id, NULL, tr.status, sg.leader_id, tr.created_at, 'Imported from legacy student_groups state'
FROM topic_registrations tr
JOIN student_groups sg ON sg.id = tr.group_id;
