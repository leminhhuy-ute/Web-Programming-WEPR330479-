-- Migration V13: Groups have 1-3 members and topics accept 1-3 students (replaces the 1-5 / 3-5 rules of V12).
-- Groups that already have more than 3 members make the guard below fail on purpose:
-- members are never removed automatically, such groups must be fixed by hand before upgrading.

-- 0. Guard first: fails before any other change when a group has more than 3 members
ALTER TABLE student_groups ADD CONSTRAINT ck_groups_size_v13_guard CHECK (member_count BETWEEN 1 AND 3);

-- 1. Bring existing topics into the new range [1, 3]
UPDATE topics SET max_students = 3 WHERE max_students > 3;
UPDATE topics SET max_students = 1 WHERE max_students < 1;

-- 2. topics.max_students: default 3, allowed 1..3
ALTER TABLE topics ALTER COLUMN max_students SET DEFAULT 3;
ALTER TABLE topics DROP CONSTRAINT ck_topics_size;
ALTER TABLE topics ADD CONSTRAINT ck_topics_size CHECK (max_students BETWEEN 1 AND 3);

-- 3. student_groups.member_count: allowed 1..3
ALTER TABLE student_groups DROP CONSTRAINT ck_groups_size;
ALTER TABLE student_groups ADD CONSTRAINT ck_groups_size CHECK (member_count BETWEEN 1 AND 3);
ALTER TABLE student_groups DROP CONSTRAINT ck_groups_size_v13_guard;
