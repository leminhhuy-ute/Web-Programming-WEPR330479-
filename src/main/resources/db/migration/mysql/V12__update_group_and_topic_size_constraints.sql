-- Migration V12: Update group and topic student capacity constraints (group 1-5 members, topic 3-5 students)

-- 1. Ensure existing topics have max_students within new range [3, 5]
UPDATE topics SET max_students = 3 WHERE max_students < 3;
UPDATE topics SET max_students = 5 WHERE max_students > 5;

-- 2. Update topics.max_students default and check constraint
ALTER TABLE topics ALTER COLUMN max_students SET DEFAULT 3;
ALTER TABLE topics DROP CONSTRAINT ck_topics_size;
ALTER TABLE topics ADD CONSTRAINT ck_topics_size CHECK (max_students BETWEEN 3 AND 5);

-- 3. Update student_groups member_count check constraint to allow 1 to 5 members
ALTER TABLE student_groups DROP CONSTRAINT ck_groups_size;
ALTER TABLE student_groups ADD CONSTRAINT ck_groups_size CHECK (member_count BETWEEN 1 AND 5);
