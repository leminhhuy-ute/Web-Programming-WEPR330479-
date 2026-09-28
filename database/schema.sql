CREATE DATABASE IF NOT EXISTS student_topic_management
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE student_topic_management;

CREATE TABLE IF NOT EXISTS departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_code VARCHAR(50) NOT NULL UNIQUE,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(512) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(30) NOT NULL,
    department_id BIGINT NULL,
    status VARCHAR(20) NOT NULL,
    password_changed_at DATETIME(6) NULL,
    created_at DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_users_department FOREIGN KEY (department_id) REFERENCES departments(id),
    CONSTRAINT ck_users_role CHECK (role IN ('DEAN', 'HEAD_OF_DEPT', 'LECTURER', 'STUDENT')),
    CONSTRAINT ck_users_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'LOCKED'))
);

CREATE TABLE IF NOT EXISTS registration_periods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(30) NOT NULL,
    lecturer_start_at DATETIME(6) NOT NULL,
    lecturer_end_at DATETIME(6) NOT NULL,
    student_start_at DATETIME(6) NOT NULL,
    student_end_at DATETIME(6) NOT NULL,
    review_deadline DATETIME(6) NULL,
    defense_date DATE NULL,
    created_at DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT ck_period_type CHECK (type IN ('COURSE', 'NCKH', 'TLCN', 'KLTN')),
    CONSTRAINT ck_period_times CHECK (
        lecturer_start_at < lecturer_end_at
        AND lecturer_end_at < student_start_at
        AND student_start_at < student_end_at
    ),
    CONSTRAINT ck_period_review CHECK (
        (type IN ('COURSE', 'NCKH') AND review_deadline IS NULL AND defense_date IS NULL)
        OR (type = 'TLCN' AND review_deadline > student_end_at AND defense_date IS NULL)
        OR (type = 'KLTN' AND review_deadline > student_end_at
            AND defense_date > DATE(review_deadline))
    )
);

CREATE TABLE IF NOT EXISTS announcements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    audience VARCHAR(30) NOT NULL,
    created_by BIGINT NULL,
    created_at DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_announcements_author FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT ck_announcements_audience CHECK (audience IN ('ALL', 'STUDENT', 'LECTURER')),
    INDEX idx_announcements_audience (audience, created_at)
);

CREATE TABLE IF NOT EXISTS topics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NULL,
    topic_code VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    requirements TEXT NULL,
    max_students INT NOT NULL DEFAULT 3,
    topic_type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    rejection_reason TEXT NULL,
    department_id BIGINT NOT NULL,
    period_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    advisor1_id BIGINT NULL,
    advisor2_id BIGINT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_topics_department FOREIGN KEY (department_id) REFERENCES departments(id),
    CONSTRAINT fk_topics_period FOREIGN KEY (period_id) REFERENCES registration_periods(id),
    CONSTRAINT fk_topics_creator FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT fk_topics_advisor1 FOREIGN KEY (advisor1_id) REFERENCES users(id),
    CONSTRAINT fk_topics_advisor2 FOREIGN KEY (advisor2_id) REFERENCES users(id),
    CONSTRAINT ck_topics_type CHECK (topic_type IN ('COURSE', 'NCKH', 'TLCN', 'KLTN')),
    CONSTRAINT ck_topics_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT ck_topics_size CHECK (max_students BETWEEN 1 AND 3),
    CONSTRAINT ck_topics_advisors CHECK (advisor2_id IS NULL OR advisor1_id <> advisor2_id),
    INDEX idx_topics_department_status (department_id, status),
    INDEX idx_topics_period (period_id)
);

CREATE TABLE IF NOT EXISTS student_groups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NULL,
    group_code VARCHAR(50) NOT NULL UNIQUE,
    group_name VARCHAR(255) NOT NULL,
    topic_id BIGINT NULL,
    period_id BIGINT NULL,
    leader_id BIGINT NOT NULL,
    member_count INT NOT NULL DEFAULT 1,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    registered_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    notes TEXT NULL,
    CONSTRAINT fk_groups_topic FOREIGN KEY (topic_id) REFERENCES topics(id),
    CONSTRAINT fk_groups_period FOREIGN KEY (period_id) REFERENCES registration_periods(id),
    CONSTRAINT fk_groups_leader FOREIGN KEY (leader_id) REFERENCES users(id),
    CONSTRAINT ck_groups_size CHECK (member_count BETWEEN 1 AND 3),
    CONSTRAINT ck_groups_status CHECK (status IN ('DRAFT', 'PENDING', 'APPROVED', 'REJECTED')),
    INDEX idx_groups_topic_status (topic_id, status)
);

CREATE TABLE IF NOT EXISTS group_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    registration_period_id BIGINT NULL,
    member_role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    joined_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_group_members_group FOREIGN KEY (group_id) REFERENCES student_groups(id) ON DELETE CASCADE,
    CONSTRAINT fk_group_members_student FOREIGN KEY (student_id) REFERENCES users(id),
    CONSTRAINT fk_group_members_period FOREIGN KEY (registration_period_id) REFERENCES registration_periods(id),
    CONSTRAINT ck_group_member_role CHECK (member_role IN ('LEADER', 'MEMBER')),
    CONSTRAINT uk_group_member_period_student UNIQUE (registration_period_id, student_id)
);

CREATE TABLE IF NOT EXISTS topic_registrations (
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

CREATE TABLE IF NOT EXISTS registration_status_history (
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

CREATE TABLE IF NOT EXISTS advisor_quotas (
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

CREATE TABLE IF NOT EXISTS group_invitations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT uk_group_invitation UNIQUE (group_id, student_id),
    CONSTRAINT fk_invitations_group FOREIGN KEY (group_id) REFERENCES student_groups(id) ON DELETE CASCADE,
    CONSTRAINT fk_invitations_student FOREIGN KEY (student_id) REFERENCES users(id),
    CONSTRAINT ck_invitation_status CHECK (status IN ('PENDING', 'ACCEPTED', 'DECLINED'))
);

CREATE TABLE IF NOT EXISTS student_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    registration_id BIGINT NULL,
    topic_id BIGINT NULL,
    submitted_by_id BIGINT NOT NULL,
    filename VARCHAR(180) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    stage VARCHAR(255) NOT NULL,
    note VARCHAR(1000) NOT NULL,
    submitted_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    late BOOLEAN NOT NULL DEFAULT FALSE,
    report_size BIGINT NULL,
    checksum VARCHAR(64) NULL,
    storage_path VARCHAR(500) NULL,
    content LONGBLOB NOT NULL,
    CONSTRAINT fk_reports_group FOREIGN KEY (group_id) REFERENCES student_groups(id) ON DELETE CASCADE,
    CONSTRAINT fk_reports_registration FOREIGN KEY (registration_id) REFERENCES topic_registrations(id) ON DELETE SET NULL,
    CONSTRAINT fk_reports_topic FOREIGN KEY (topic_id) REFERENCES topics(id) ON DELETE SET NULL,
    CONSTRAINT fk_reports_submitter FOREIGN KEY (submitted_by_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS councils (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NULL,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    defense_date DATETIME(6) NOT NULL,
    room VARCHAR(80) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    CONSTRAINT ck_council_status CHECK (status IN ('DRAFT', 'READY', 'COMPLETED'))
);

CREATE TABLE IF NOT EXISTS council_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    council_id BIGINT NOT NULL,
    lecturer_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    CONSTRAINT uk_council_lecturer UNIQUE (council_id, lecturer_id),
    CONSTRAINT fk_council_members_council FOREIGN KEY (council_id) REFERENCES councils(id) ON DELETE CASCADE,
    CONSTRAINT fk_council_members_lecturer FOREIGN KEY (lecturer_id) REFERENCES users(id),
    CONSTRAINT ck_council_member_role CHECK (role IN ('CHAIRPERSON', 'SECRETARY', 'REVIEWER', 'MEMBER'))
);

CREATE TABLE IF NOT EXISTS defenses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NULL,
    group_id BIGINT NOT NULL UNIQUE,
    registration_id BIGINT NULL UNIQUE,
    topic_id BIGINT NULL,
    council_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    finalized BOOLEAN NOT NULL DEFAULT FALSE,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    final_score DECIMAL(4,2) NULL,
    CONSTRAINT fk_defenses_group FOREIGN KEY (group_id) REFERENCES student_groups(id),
    CONSTRAINT fk_defenses_registration FOREIGN KEY (registration_id) REFERENCES topic_registrations(id),
    CONSTRAINT fk_defenses_topic FOREIGN KEY (topic_id) REFERENCES topics(id),
    CONSTRAINT fk_defenses_council FOREIGN KEY (council_id) REFERENCES councils(id),
    CONSTRAINT fk_defenses_reviewer FOREIGN KEY (reviewer_id) REFERENCES users(id),
    CONSTRAINT ck_defense_score CHECK (final_score IS NULL OR final_score BETWEEN 0 AND 10),
    INDEX idx_defenses_council (council_id)
);

CREATE TABLE IF NOT EXISTS defense_grades (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    defense_id BIGINT NOT NULL,
    evaluator_id BIGINT NOT NULL,
    score DECIMAL(4,2) NOT NULL,
    comment VARCHAR(2000) NOT NULL,
    updated_at DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT uk_defense_evaluator UNIQUE (defense_id, evaluator_id),
    CONSTRAINT fk_grades_defense FOREIGN KEY (defense_id) REFERENCES defenses(id) ON DELETE CASCADE,
    CONSTRAINT fk_grades_evaluator FOREIGN KEY (evaluator_id) REFERENCES users(id),
    CONSTRAINT ck_grade_score CHECK (score BETWEEN 0 AND 10)
);

CREATE TABLE IF NOT EXISTS audit_logs (
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
