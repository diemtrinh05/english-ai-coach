-- Bổ sung các index canonical theo Database Schema v1.6 mà không sửa migration V1 đã áp dụng.

-- Tài khoản và xác thực.
CREATE UNIQUE INDEX uq_users_provider
    ON users (auth_provider, provider_user_id)
    WHERE provider_user_id IS NOT NULL;

CREATE INDEX idx_users_auth_provider ON users (auth_provider);
CREATE INDEX idx_users_status ON users (status);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);
CREATE INDEX idx_refresh_tokens_user_revoked_at ON refresh_tokens (user_id, revoked_at);
CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens (token_hash);
CREATE INDEX idx_refresh_tokens_user_expires_at ON refresh_tokens (user_id, expires_at);

-- Hồ sơ, mục tiêu và assessment.
CREATE INDEX idx_user_profiles_current_cefr_level
    ON user_profiles (current_cefr_level_id);

CREATE INDEX idx_user_goals_goal_id ON user_goals (goal_id);

CREATE UNIQUE INDEX uq_user_goals_primary
    ON user_goals (user_id)
    WHERE is_primary = true;

CREATE INDEX idx_user_level_assessments_user_id
    ON user_level_assessments (user_id);

CREATE INDEX idx_user_level_assessments_current_cefr
    ON user_level_assessments (current_cefr_level_id);

CREATE INDEX idx_user_level_assessments_final_cefr
    ON user_level_assessments (final_cefr_level_id);

CREATE INDEX idx_user_level_assessments_status
    ON user_level_assessments (status);

CREATE UNIQUE INDEX uq_user_assessment_in_progress
    ON user_level_assessments (user_id)
    WHERE status = 'IN_PROGRESS';

-- Vocabulary và learning.
CREATE INDEX idx_topics_parent_topic_id ON topics (parent_topic_id);
CREATE INDEX idx_vocabulary_word ON vocabulary (word);
CREATE INDEX idx_vocabulary_cefr_level_id ON vocabulary (cefr_level_id);
CREATE INDEX idx_vocabulary_topics_topic_id ON vocabulary_topics (topic_id);
CREATE INDEX idx_vocabulary_examples_vocabulary_id ON vocabulary_examples (vocabulary_id);

CREATE INDEX idx_user_vocabulary_progress_user_next_review
    ON user_vocabulary_progress (user_id, next_review_at);

CREATE INDEX idx_user_vocabulary_progress_vocabulary_id
    ON user_vocabulary_progress (vocabulary_id);

CREATE INDEX idx_user_vocabulary_progress_user_status
    ON user_vocabulary_progress (user_id, status);

CREATE INDEX idx_learning_sessions_user_id ON learning_sessions (user_id);
CREATE INDEX idx_learning_sessions_started_at ON learning_sessions (started_at);
CREATE INDEX idx_session_attempts_session_id ON session_attempts (session_id);
CREATE INDEX idx_session_attempts_vocabulary_id ON session_attempts (vocabulary_id);
CREATE INDEX idx_session_attempts_attempted_at ON session_attempts (attempted_at);

CREATE INDEX idx_session_attempts_session_attempted_at
    ON session_attempts (session_id, attempted_at);

CREATE INDEX idx_session_attempts_vocabulary_attempted_at
    ON session_attempts (vocabulary_id, attempted_at);

CREATE INDEX idx_daily_plans_user_status ON daily_plans (user_id, status);

-- Quiz và gamification.
CREATE INDEX idx_quizzes_topic_id ON quizzes (topic_id);
CREATE INDEX idx_quizzes_cefr_level_id ON quizzes (cefr_level_id);
CREATE INDEX idx_quiz_questions_quiz_id ON quiz_questions (quiz_id);
CREATE INDEX idx_quiz_questions_vocabulary_id ON quiz_questions (vocabulary_id);
CREATE INDEX idx_quiz_attempts_user_id ON quiz_attempts (user_id);
CREATE INDEX idx_quiz_attempts_quiz_id ON quiz_attempts (quiz_id);
CREATE INDEX idx_quiz_attempts_user_started_at ON quiz_attempts (user_id, started_at);
CREATE INDEX idx_quiz_attempt_answers_attempt_id ON quiz_attempt_answers (quiz_attempt_id);
CREATE INDEX idx_quiz_attempt_answers_question_id ON quiz_attempt_answers (quiz_question_id);
CREATE INDEX idx_xp_logs_user_id ON xp_logs (user_id);
CREATE INDEX idx_xp_logs_created_at ON xp_logs (created_at);
CREATE INDEX idx_xp_logs_user_created_at ON xp_logs (user_id, created_at);
CREATE INDEX idx_user_badges_badge_id ON user_badges (badge_id);

-- Notification dedupe và các truy vấn gửi notification.
CREATE UNIQUE INDEX uq_notifications_user_type_local_date
    ON notifications (user_id, type, local_notification_date)
    WHERE local_notification_date IS NOT NULL
      AND type IN ('REVIEW_REMINDER', 'DAILY_PLAN', 'STREAK');

CREATE INDEX idx_notifications_user_status ON notifications (user_id, status);
CREATE INDEX idx_notifications_scheduled_status ON notifications (scheduled_at, status);
CREATE INDEX idx_notifications_user_created_at ON notifications (user_id, created_at);

-- AI, audit và idempotency retention/lookup.
CREATE UNIQUE INDEX uq_ai_reusable_generation_key
    ON ai_generated_content (generation_key)
    WHERE content_scope = 'REUSABLE'
      AND generation_key IS NOT NULL;

CREATE INDEX idx_ai_generated_content_user_id ON ai_generated_content (user_id);
CREATE INDEX idx_ai_generated_content_vocabulary_id ON ai_generated_content (vocabulary_id);
CREATE INDEX idx_ai_generated_content_topic_id ON ai_generated_content (topic_id);
CREATE INDEX idx_ai_generated_content_status ON ai_generated_content (status);
CREATE INDEX idx_ai_generated_content_scope_status
    ON ai_generated_content (content_scope, status);
CREATE INDEX idx_ai_generated_content_reviewed_by ON ai_generated_content (reviewed_by);
CREATE INDEX idx_ai_generated_content_expires_at ON ai_generated_content (expires_at);

CREATE INDEX idx_ai_requests_user_id ON ai_requests (user_id);
CREATE INDEX idx_ai_requests_feature ON ai_requests (feature);
CREATE INDEX idx_ai_requests_created_at ON ai_requests (created_at);
CREATE INDEX idx_ai_requests_provider_model ON ai_requests (provider, model);
CREATE INDEX idx_ai_requests_status_created_at ON ai_requests (status, created_at);

CREATE INDEX idx_ai_usage_daily_usage_date ON ai_usage_daily (usage_date);
CREATE INDEX idx_ai_usage_daily_provider_model ON ai_usage_daily (provider, model);
CREATE INDEX idx_ai_usage_daily_feature ON ai_usage_daily (feature);

CREATE INDEX idx_admin_audit_logs_admin_id ON admin_audit_logs (admin_id);
CREATE INDEX idx_admin_audit_logs_target ON admin_audit_logs (target_table, target_id);
CREATE INDEX idx_admin_audit_logs_created_at ON admin_audit_logs (created_at);
CREATE INDEX idx_admin_audit_logs_admin_created_at
    ON admin_audit_logs (admin_id, created_at);

CREATE INDEX idx_idempotency_keys_user_created_at
    ON idempotency_keys (user_id, created_at);

CREATE INDEX idx_idempotency_keys_endpoint_created_at
    ON idempotency_keys (endpoint, created_at);
