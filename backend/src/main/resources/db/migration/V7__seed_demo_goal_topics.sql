-- DB-CONTENT-002: relevance biên soạn thủ công cho bảy goal và tám topic demo.
-- Nguồn và giới hạn: docs/database/DB-CONTENT-002-demo-goal-topic-provenance.md.
WITH curated(goal_name, topic_name, relevance_weight) AS (
    VALUES
    ('GENERAL_ENGLISH', 'Daily Life', 0.250),
    ('GENERAL_ENGLISH', 'Travel', 0.250),
    ('GENERAL_ENGLISH', 'Food', 0.250),
    ('GENERAL_ENGLISH', 'Business', 0.250),
    ('GENERAL_ENGLISH', 'Technology', 0.250),
    ('GENERAL_ENGLISH', 'Education', 0.250),
    ('GENERAL_ENGLISH', 'Health', 0.250),
    ('GENERAL_ENGLISH', 'Environment', 0.250),
    ('TRAVEL', 'Travel', 1.000),
    ('TRAVEL', 'Food', 0.500),
    ('BUSINESS', 'Business', 1.000),
    ('BUSINESS', 'Technology', 0.500),
    ('TOEIC', 'Business', 1.000),
    ('TOEIC', 'Travel', 0.500),
    ('IELTS', 'Education', 1.000),
    ('IELTS', 'Environment', 0.500),
    ('IELTS', 'Health', 0.500),
    ('COMMUNICATION', 'Daily Life', 1.000),
    ('COMMUNICATION', 'Travel', 0.500),
    ('COMMUNICATION', 'Food', 0.500),
    ('ACADEMIC', 'Education', 1.000),
    ('ACADEMIC', 'Technology', 0.500),
    ('ACADEMIC', 'Environment', 0.500)
)
INSERT INTO goal_topics (id, goal_id, topic_id, relevance_weight, created_at)
SELECT md5('DB-CONTENT-002:' || g.name || ':' || t.name)::uuid,
       g.id,
       t.id,
       c.relevance_weight,
       CURRENT_TIMESTAMP
FROM curated c
JOIN goals g ON g.name = c.goal_name AND g.is_active = TRUE
JOIN topics t ON t.name = c.topic_name AND t.is_active = TRUE
ON CONFLICT (goal_id, topic_id) DO NOTHING;
