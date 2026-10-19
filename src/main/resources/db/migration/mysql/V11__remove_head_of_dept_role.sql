-- Migration V11: Migrate all HEAD_OF_DEPT accounts to LECTURER and enforce 3-role constraint (DEAN, LECTURER, STUDENT)

-- 1. Migrate legacy users from HEAD_OF_DEPT to LECTURER
UPDATE users SET role = 'LECTURER' WHERE role = 'HEAD_OF_DEPT';

-- 2. Migrate any announcement audiences targeted to HEAD_OF_DEPT to LECTURER
UPDATE announcements SET audience = 'LECTURER' WHERE audience = 'HEAD_OF_DEPT';

-- 3. Update check constraint on users(role)
ALTER TABLE users DROP CONSTRAINT ck_users_role;
ALTER TABLE users ADD CONSTRAINT ck_users_role CHECK (role IN ('DEAN', 'LECTURER', 'STUDENT'));

-- 4. Update check constraint on announcements(audience)
ALTER TABLE announcements DROP CONSTRAINT ck_announcements_audience;
ALTER TABLE announcements ADD CONSTRAINT ck_announcements_audience CHECK (audience IN ('ALL', 'LECTURER', 'STUDENT'));
