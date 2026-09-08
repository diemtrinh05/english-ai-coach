-- Baseline khởi tạo mới theo Database Schema v1.6.
-- Các migration đã được chia sẻ phải giữ immutable; mọi thay đổi sau V1 dùng version mới.

-- Tài khoản và xác thực.
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    auth_provider VARCHAR(20) NOT NULL,
    provider_user_id VARCHAR(255),
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    last_login_at TIMESTAMPTZ
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    last_used_at TIMESTAMPTZ,
    device_info VARCHAR(500)
);

CREATE TABLE cefr_levels (
    id UUID PRIMARY KEY,
    code VARCHAR(2) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    description TEXT,
    sort_order INTEGER NOT NULL
);

CREATE TABLE user_profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id),
    avatar_url TEXT,
    current_cefr_level_id UUID REFERENCES cefr_levels(id),
    daily_learning_minutes INTEGER NOT NULL,
    timezone VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

-- Mục tiêu và assessment.
CREATE TABLE goals (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    is_active BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE user_goals (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    goal_id UUID NOT NULL REFERENCES goals(id),
    is_primary BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_user_goals_user_goal UNIQUE (user_id, goal_id)
);

CREATE TABLE user_level_assessments (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    assessment_type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    current_cefr_level_id UUID NOT NULL REFERENCES cefr_levels(id),
    final_cefr_level_id UUID REFERENCES cefr_levels(id),
    score DECIMAL(5,2),
    questions_answered INTEGER NOT NULL DEFAULT 0,
    correct_answers INTEGER NOT NULL DEFAULT 0,
    block_questions INTEGER NOT NULL DEFAULT 0,
    block_correct INTEGER NOT NULL DEFAULT 0,
    stable_block_count INTEGER NOT NULL DEFAULT 0,
    started_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

-- Từ vựng và topic.
CREATE TABLE topics (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    icon_url TEXT,
    parent_topic_id UUID REFERENCES topics(id),
    is_active BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE goal_topics (
    id UUID PRIMARY KEY,
    goal_id UUID NOT NULL REFERENCES goals(id),
    topic_id UUID NOT NULL REFERENCES topics(id),
    relevance_weight DECIMAL(4,3) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_goal_topics_goal_topic UNIQUE (goal_id, topic_id),
    CONSTRAINT ck_goal_topics_relevance_weight
        CHECK (relevance_weight >= 0 AND relevance_weight <= 1)
);

CREATE TABLE vocabulary (
    id UUID PRIMARY KEY,
    word VARCHAR(150) NOT NULL,
    phonetic_ipa VARCHAR(255),
    meaning_vi TEXT,
    meaning_en TEXT,
    part_of_speech VARCHAR(50),
    cefr_level_id UUID NOT NULL REFERENCES cefr_levels(id),
    audio_url TEXT,
    image_url TEXT,
    source VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_vocabulary_word_pos_cefr
        UNIQUE (word, part_of_speech, cefr_level_id)
);

CREATE TABLE assessment_items (
    id UUID PRIMARY KEY,
    assessment_id UUID NOT NULL REFERENCES user_level_assessments(id),
    sequence_no INTEGER NOT NULL,
    vocabulary_id UUID NOT NULL REFERENCES vocabulary(id),
    cefr_level_id UUID NOT NULL REFERENCES cefr_levels(id),
    question_text TEXT NOT NULL,
    options_json JSONB NOT NULL,
    correct_answer TEXT NOT NULL,
    selected_answer TEXT,
    is_correct BOOLEAN,
    response_time_ms INTEGER,
    answered_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_assessment_items_sequence UNIQUE (assessment_id, sequence_no),
    CONSTRAINT uq_assessment_items_vocabulary UNIQUE (assessment_id, vocabulary_id),
    CONSTRAINT ck_assessment_items_response_time
        CHECK (response_time_ms IS NULL OR response_time_ms >= 0)
);

CREATE TABLE vocabulary_topics (
    vocabulary_id UUID NOT NULL REFERENCES vocabulary(id),
    topic_id UUID NOT NULL REFERENCES topics(id),
    PRIMARY KEY (vocabulary_id, topic_id)
);

CREATE TABLE vocabulary_examples (
    id UUID PRIMARY KEY,
    vocabulary_id UUID NOT NULL REFERENCES vocabulary(id),
    example_text TEXT NOT NULL,
    translation_text TEXT,
    source VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

-- Trạng thái học và personalization snapshot.
CREATE TABLE user_vocabulary_progress (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    vocabulary_id UUID NOT NULL REFERENCES vocabulary(id),
    status VARCHAR(20) NOT NULL,
    ease_factor DECIMAL(5,2) NOT NULL,
    interval_days INTEGER NOT NULL,
    repetitions INTEGER NOT NULL,
    next_review_at TIMESTAMPTZ,
    last_reviewed_at TIMESTAMPTZ,
    correct_count INTEGER NOT NULL,
    incorrect_count INTEGER NOT NULL,
    avg_response_time_ms INTEGER,
    last_quality SMALLINT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_user_vocabulary_progress_user_vocabulary
        UNIQUE (user_id, vocabulary_id),
    CONSTRAINT ck_user_vocabulary_progress_last_quality
        CHECK (last_quality IS NULL OR last_quality BETWEEN 0 AND 5),
    CONSTRAINT ck_user_vocabulary_progress_correct_count CHECK (correct_count >= 0),
    CONSTRAINT ck_user_vocabulary_progress_incorrect_count CHECK (incorrect_count >= 0),
    CONSTRAINT ck_user_vocabulary_progress_repetitions CHECK (repetitions >= 0),
    CONSTRAINT ck_user_vocabulary_progress_interval_days CHECK (interval_days >= 0),
    CONSTRAINT ck_user_vocabulary_progress_ease_factor CHECK (ease_factor > 0)
);

CREATE TABLE learning_sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    session_type VARCHAR(30) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    ended_at TIMESTAMPTZ,
    words_studied_count INTEGER NOT NULL,
    accuracy_percent DECIMAL(5,2)
);

CREATE TABLE session_attempts (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES learning_sessions(id),
    vocabulary_id UUID NOT NULL REFERENCES vocabulary(id),
    attempt_type VARCHAR(30) NOT NULL,
    is_correct BOOLEAN NOT NULL,
    response_time_ms INTEGER,
    answer_quality SMALLINT NOT NULL,
    attempted_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_session_attempts_response_time
        CHECK (response_time_ms IS NULL OR response_time_ms >= 0),
    CONSTRAINT ck_session_attempts_answer_quality CHECK (answer_quality BETWEEN 0 AND 5),
    CONSTRAINT ck_session_attempts_correctness
        CHECK (is_correct = (answer_quality >= 3))
);

CREATE TABLE daily_plans (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    plan_date DATE NOT NULL,
    new_words_target INTEGER NOT NULL,
    review_words_target INTEGER NOT NULL,
    quiz_target INTEGER NOT NULL,
    estimated_minutes INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    generated_by VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_daily_plans_user_date UNIQUE (user_id, plan_date)
);

CREATE TABLE daily_plan_items (
    id UUID PRIMARY KEY,
    daily_plan_id UUID NOT NULL REFERENCES daily_plans(id),
    item_type VARCHAR(20) NOT NULL,
    vocabulary_id UUID REFERENCES vocabulary(id),
    position INTEGER NOT NULL,
    reason_code VARCHAR(50),
    target_count INTEGER NOT NULL DEFAULT 1,
    completed_count INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_daily_plan_items_position UNIQUE (daily_plan_id, position),
    CONSTRAINT ck_daily_plan_items_target_count CHECK (target_count > 0),
    CONSTRAINT ck_daily_plan_items_completed_count
        CHECK (completed_count >= 0 AND completed_count <= target_count),
    CONSTRAINT ck_daily_plan_items_type_target
        CHECK (
            (item_type IN ('REVIEW', 'NEW') AND vocabulary_id IS NOT NULL AND target_count = 1)
            OR (item_type = 'QUIZ' AND vocabulary_id IS NULL)
        )
);

-- Quiz.
CREATE TABLE quizzes (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    topic_id UUID REFERENCES topics(id),
    cefr_level_id UUID REFERENCES cefr_levels(id),
    source VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE quiz_questions (
    id UUID PRIMARY KEY,
    quiz_id UUID NOT NULL REFERENCES quizzes(id),
    vocabulary_id UUID REFERENCES vocabulary(id),
    question_text TEXT NOT NULL,
    question_type VARCHAR(30) NOT NULL,
    correct_answer TEXT NOT NULL,
    options JSONB,
    explanation TEXT,
    sort_order INTEGER NOT NULL
);

CREATE TABLE quiz_attempts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    quiz_id UUID NOT NULL REFERENCES quizzes(id),
    score DECIMAL(5,2) NOT NULL,
    total_questions INTEGER NOT NULL,
    correct_answers INTEGER NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ
);

CREATE TABLE quiz_attempt_answers (
    id UUID PRIMARY KEY,
    quiz_attempt_id UUID NOT NULL REFERENCES quiz_attempts(id),
    quiz_question_id UUID NOT NULL REFERENCES quiz_questions(id),
    user_answer TEXT,
    is_correct BOOLEAN NOT NULL,
    response_time_ms INTEGER,
    answered_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_quiz_attempt_answers_attempt_question
        UNIQUE (quiz_attempt_id, quiz_question_id),
    CONSTRAINT ck_quiz_attempt_answers_response_time
        CHECK (response_time_ms IS NULL OR response_time_ms >= 0)
);

-- Gamification.
CREATE TABLE streaks (
    id UUID PRIMARY KEY,
    user_id UUID UNIQUE REFERENCES users(id),
    current_streak INTEGER NOT NULL,
    longest_streak INTEGER NOT NULL,
    last_active_date DATE,
    version BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE xp_logs (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    xp_amount INTEGER NOT NULL,
    reason VARCHAR(100) NOT NULL,
    reference_type VARCHAR(50),
    reference_id UUID,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE badges (
    id UUID PRIMARY KEY,
    name VARCHAR(100) UNIQUE,
    description TEXT,
    icon_url TEXT,
    condition_type VARCHAR(50),
    condition_value INTEGER,
    is_active BOOLEAN NOT NULL
);

CREATE TABLE user_badges (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    badge_id UUID NOT NULL REFERENCES badges(id),
    earned_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_user_badges_user_badge UNIQUE (user_id, badge_id)
);

-- Notification.
CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    type VARCHAR(30) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    scheduled_at TIMESTAMPTZ,
    sent_at TIMESTAMPTZ,
    status VARCHAR(20) NOT NULL,
    local_notification_date DATE,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE user_devices (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    installation_id UUID NOT NULL,
    platform VARCHAR(20) NOT NULL,
    push_token TEXT NOT NULL UNIQUE,
    is_active BOOLEAN NOT NULL,
    last_seen_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_user_devices_user_installation UNIQUE (user_id, installation_id)
);

CREATE TABLE notification_preferences (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id),
    push_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    review_reminder_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    daily_plan_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    streak_reminder_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    preferred_study_time TIME NOT NULL DEFAULT '19:00',
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

-- AI và audit.
CREATE TABLE ai_generated_content (
    id UUID PRIMARY KEY,
    content_scope VARCHAR(20) NOT NULL,
    content_type VARCHAR(30) NOT NULL,
    user_id UUID REFERENCES users(id),
    vocabulary_id UUID REFERENCES vocabulary(id),
    topic_id UUID REFERENCES topics(id),
    prompt_used TEXT NOT NULL,
    prompt_version VARCHAR(30),
    generated_content JSONB NOT NULL,
    model_used VARCHAR(100) NOT NULL,
    generation_key VARCHAR(255),
    status VARCHAR(30) NOT NULL,
    reviewed_by UUID REFERENCES users(id),
    reviewed_at TIMESTAMPTZ,
    review_note TEXT,
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE ai_requests (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    feature VARCHAR(50) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    model VARCHAR(100) NOT NULL,
    request_tokens INTEGER,
    response_tokens INTEGER,
    total_tokens INTEGER,
    estimated_cost DECIMAL(12,6),
    status VARCHAR(30) NOT NULL,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE ai_usage_daily (
    id UUID PRIMARY KEY,
    usage_date DATE NOT NULL,
    provider VARCHAR(50) NOT NULL,
    model VARCHAR(100) NOT NULL,
    feature VARCHAR(50) NOT NULL,
    request_count INTEGER NOT NULL,
    total_tokens BIGINT NOT NULL,
    estimated_cost DECIMAL(12,6) NOT NULL,
    blocked_request_count INTEGER NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_ai_usage_daily_dimension
        UNIQUE (usage_date, provider, model, feature)
);

CREATE TABLE admin_audit_logs (
    id UUID PRIMARY KEY,
    admin_id UUID NOT NULL REFERENCES users(id),
    action VARCHAR(50) NOT NULL,
    target_table VARCHAR(100),
    target_id UUID,
    details JSONB,
    created_at TIMESTAMPTZ NOT NULL
);

-- Idempotency lưu đúng một logical operation theo event_id.
CREATE TABLE idempotency_keys (
    event_id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    endpoint VARCHAR(100) NOT NULL,
    request_hash CHAR(64) NOT NULL,
    response_snapshot JSONB,
    response_status INTEGER,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_idempotency_keys_response_status
        CHECK (response_status IS NULL OR response_status BETWEEN 100 AND 599)
);
