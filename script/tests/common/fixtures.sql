INSERT INTO users (id,username,email,full_name,created_at) VALUES
(pg_temp.id(0,1),'alice01','alice@example.test','Nguyễn Minh An','2026-01-01 UTC'),
(pg_temp.id(0,2),'bob0002','bob@example.test','Trần Bình','2026-01-01 UTC');
INSERT INTO conversations (id,type,group_name,created_by_user_id,created_at) VALUES
(pg_temp.id(1,1),'DIRECT',NULL,pg_temp.id(0,1),'2026-01-01 UTC'),
(pg_temp.id(1,2),'GROUP','Nhóm học Java',pg_temp.id(0,1),'2026-01-01 UTC');
