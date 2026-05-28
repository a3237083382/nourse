USE `ry-vue`;

INSERT INTO service_category (name, icon_url, sort_no, enabled, created_at, updated_at, deleted)
VALUES
  ('月嫂', '', 10, 1, NOW(), NOW(), 0),
  ('保姆', '', 20, 1, NOW(), NOW(), 0),
  ('育婴师', '', 30, 1, NOW(), NOW(), 0),
  ('居家养老', '', 40, 1, NOW(), NOW(), 0),
  ('保洁师', '', 50, 1, NOW(), NOW(), 0),
  ('钟点工', '', 60, 1, NOW(), NOW(), 0),
  ('成长陪伴师', '', 70, 1, NOW(), NOW(), 0),
  ('家电清洗师', '', 80, 1, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE
  icon_url = VALUES(icon_url),
  sort_no = VALUES(sort_no),
  enabled = VALUES(enabled),
  updated_at = NOW(),
  deleted = 0;
