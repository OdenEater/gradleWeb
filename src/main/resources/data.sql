-- data.sql
-- 新しく生成された有効なBCryptハッシュ（平文: "password"）
INSERT INTO users (username, password, enabled) VALUES
('user1', '$2a$10$eO19yeYpJ/VgYT2iNKkIreAcf6FKelxykGvslJ4iZj7naHss3FSqu', true),
('user2', '$2a$10$eO19yeYpJ/VgYT2iNKkIreAcf6FKelxykGvslJ4iZj7naHss3FSqu', false),
('user3', '$2a$10$58rcDjn3OVrOipzO2HEcx.29vhp4BXGyUdLY7hpezl0ybkVouPlQ6', true);
