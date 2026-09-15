-- Seed dữ liệu tham chiếu canonical bằng UUID ổn định và upsert idempotent.

INSERT INTO cefr_levels (id, code, name, description, sort_order)
VALUES
    ('10000000-0000-0000-0000-000000000001', 'A1', 'A1', NULL, 1),
    ('10000000-0000-0000-0000-000000000002', 'A2', 'A2', NULL, 2),
    ('10000000-0000-0000-0000-000000000003', 'B1', 'B1', NULL, 3),
    ('10000000-0000-0000-0000-000000000004', 'B2', 'B2', NULL, 4),
    ('10000000-0000-0000-0000-000000000005', 'C1', 'C1', NULL, 5),
    ('10000000-0000-0000-0000-000000000006', 'C2', 'C2', NULL, 6)
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name,
    description = EXCLUDED.description,
    sort_order = EXCLUDED.sort_order;

INSERT INTO goals (id, name, description, is_active, created_at)
VALUES
    ('20000000-0000-0000-0000-000000000001', 'GENERAL_ENGLISH', NULL, TRUE, now()),
    ('20000000-0000-0000-0000-000000000002', 'TRAVEL', NULL, TRUE, now()),
    ('20000000-0000-0000-0000-000000000003', 'BUSINESS', NULL, TRUE, now()),
    ('20000000-0000-0000-0000-000000000004', 'TOEIC', NULL, TRUE, now()),
    ('20000000-0000-0000-0000-000000000005', 'IELTS', NULL, TRUE, now()),
    ('20000000-0000-0000-0000-000000000006', 'COMMUNICATION', NULL, TRUE, now()),
    ('20000000-0000-0000-0000-000000000007', 'ACADEMIC', NULL, TRUE, now())
ON CONFLICT (name) DO UPDATE
SET is_active = EXCLUDED.is_active;

INSERT INTO badges (
    id, name, description, icon_url, condition_type, condition_value, is_active
)
VALUES
    ('30000000-0000-0000-0000-000000000001', 'First Lesson', NULL, NULL,
     'FIRST_LESSON', 1, TRUE),
    ('30000000-0000-0000-0000-000000000002', '7 Day Streak', NULL, NULL,
     'STREAK_7', 7, TRUE),
    ('30000000-0000-0000-0000-000000000003', '100 Words', NULL, NULL,
     'WORDS_100', 100, TRUE),
    ('30000000-0000-0000-0000-000000000004', '500 Words', NULL, NULL,
     'WORDS_500', 500, TRUE),
    ('30000000-0000-0000-0000-000000000005', 'Perfect Quiz', NULL, NULL,
     'PERFECT_QUIZ', 5, TRUE)
ON CONFLICT (name) DO UPDATE
SET condition_type = EXCLUDED.condition_type,
    condition_value = EXCLUDED.condition_value,
    is_active = EXCLUDED.is_active;
