-- BE-VOC-002: liên kết topic thủ công cho một tập từ demo chọn lọc.
-- Nguồn gốc và tiêu chí rà soát: docs/database/BE-VOC-002-demo-topic-provenance.md.
-- Chỉ thêm liên kết còn thiếu; không thay đổi từ vựng hoặc topic đã biên tập.
WITH curated(cefr_code, word, part_of_speech, topic_name) AS (
    VALUES
    ('A1', 'family', 'noun', 'Daily Life'),
    ('A1', 'friend', 'noun', 'Daily Life'),
    ('A1', 'house', 'noun', 'Daily Life'),
    ('A1', 'train', 'noun', 'Travel'),
    ('A2', 'baggage', 'noun', 'Travel'),
    ('A2', 'destination', 'noun', 'Travel'),
    ('A2', 'passport', 'noun', 'Travel'),
    ('A1', 'apple', 'noun', 'Food'),
    ('A1', 'bread', 'noun', 'Food'),
    ('A2', 'restaurant', 'noun', 'Food'),
    ('A2', 'customer', 'noun', 'Business'),
    ('A2', 'discount', 'noun', 'Business'),
    ('B2', 'investment', 'noun', 'Business'),
    ('B2', 'negotiation', 'noun', 'Business'),
    ('B2', 'framework', 'noun', 'Technology'),
    ('B2', 'efficiency', 'noun', 'Technology'),
    ('B2', 'reliability', 'noun', 'Technology'),
    ('A1', 'book', 'noun', 'Education'),
    ('A1', 'school', 'noun', 'Education'),
    ('A1', 'teacher', 'noun', 'Education'),
    ('A2', 'library', 'noun', 'Education'),
    ('A2', 'medicine', 'noun', 'Health'),
    ('A2', 'temperature', 'noun', 'Health'),
    ('B2', 'welfare', 'noun', 'Health'),
    ('A1', 'garden', 'noun', 'Environment'),
    ('B1', 'environment', 'noun', 'Environment'),
    ('B2', 'sustainability', 'noun', 'Environment')
)
INSERT INTO vocabulary_topics (vocabulary_id, topic_id)
SELECT v.id, t.id
FROM curated m
JOIN cefr_levels c ON c.code = m.cefr_code
JOIN vocabulary v ON v.cefr_level_id = c.id AND v.word = m.word
                 AND v.part_of_speech = m.part_of_speech
JOIN topics t ON t.name = m.topic_name
ON CONFLICT (vocabulary_id, topic_id) DO NOTHING;
