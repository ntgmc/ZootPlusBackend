create index if not exists idx_copilot_public_uploader_stats
  on copilot (uploader_id, copilot_id) include (like_count)
  where delete = false and status = 'PUBLIC';
