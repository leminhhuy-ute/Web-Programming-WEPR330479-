USE student_topic_management;

ALTER TABLE users ADD COLUMN password_changed_at DATETIME(6) NULL;

ALTER TABLE student_groups ADD COLUMN period_id BIGINT NULL;
ALTER TABLE student_groups ADD CONSTRAINT fk_groups_period FOREIGN KEY (period_id) REFERENCES registration_periods(id);

ALTER TABLE group_members ADD COLUMN registration_period_id BIGINT NULL;
ALTER TABLE group_members DROP INDEX student_id;
ALTER TABLE group_members ADD CONSTRAINT fk_group_members_period FOREIGN KEY (registration_period_id) REFERENCES registration_periods(id);
ALTER TABLE group_members ADD CONSTRAINT uk_group_member_period_student UNIQUE (registration_period_id, student_id);

ALTER TABLE student_reports ADD COLUMN registration_id BIGINT NULL;
ALTER TABLE student_reports ADD COLUMN topic_id BIGINT NULL;
ALTER TABLE student_reports ADD CONSTRAINT fk_reports_registration FOREIGN KEY (registration_id) REFERENCES topic_registrations(id) ON DELETE SET NULL;
ALTER TABLE student_reports ADD CONSTRAINT fk_reports_topic FOREIGN KEY (topic_id) REFERENCES topics(id) ON DELETE SET NULL;

ALTER TABLE defenses ADD COLUMN registration_id BIGINT NULL;
ALTER TABLE defenses ADD COLUMN topic_id BIGINT NULL;
ALTER TABLE defenses ADD CONSTRAINT uk_defenses_registration UNIQUE (registration_id);
ALTER TABLE defenses ADD CONSTRAINT fk_defenses_registration FOREIGN KEY (registration_id) REFERENCES topic_registrations(id);
ALTER TABLE defenses ADD CONSTRAINT fk_defenses_topic FOREIGN KEY (topic_id) REFERENCES topics(id);
