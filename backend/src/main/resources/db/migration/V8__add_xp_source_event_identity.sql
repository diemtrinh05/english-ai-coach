-- BE-GAME-001: bảo toàn ledger cũ; không suy lại hoặc gộp nguồn lịch sử.
ALTER TABLE xp_logs ADD COLUMN source_event_id UUID;
UPDATE xp_logs SET source_event_id = id;
ALTER TABLE xp_logs ALTER COLUMN source_event_id SET NOT NULL;
ALTER TABLE xp_logs ADD CONSTRAINT uq_xp_logs_source_event
    UNIQUE (user_id, reason, source_event_id);
