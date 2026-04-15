CREATE INDEX IF NOT EXISTS idx_audit_action_type ON audit_log(action_type);
CREATE INDEX IF NOT EXISTS idx_audit_actor_created ON audit_log(actor_id, created_at);
CREATE INDEX IF NOT EXISTS idx_audit_target_created ON audit_log(target_id, created_at);
