-- Shared fixture identities: UUID suffix 1=Alice, 2=Bob, 3=Carol, 4=Admin.
-- These are database-only fixtures, not login credentials; rollback removes them.
INSERT INTO users (id,username,email,full_name,role,created_at) VALUES
(pg_temp.id(0,1),'alice01','alice@example.test','Nguyễn Minh An','USER','2026-01-01 UTC'),
(pg_temp.id(0,2),'bob0002','bob@example.test','Trần Bình','USER','2026-01-01 UTC'),
(pg_temp.id(0,3),'carol03','carol@example.test','Lê Chi','USER','2026-01-01 UTC'),
(pg_temp.id(0,4),'admin04','admin@example.test','Phạm Duy','ADMIN','2026-01-01 UTC');
