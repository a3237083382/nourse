USE `ry-vue`;

INSERT INTO app_user (openid, nickname, avatar_url, phone, status, created_at, updated_at, deleted)
VALUES
  ('mock-openid', '小程序测试用户', '', '13800000001', 'ENABLED', NOW(), NOW(), 0),
  ('mock-openid-assist', '拼团测试用户', '', '13800000002', 'ENABLED', NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE
  nickname = VALUES(nickname),
  avatar_url = VALUES(avatar_url),
  phone = VALUES(phone),
  status = 'ENABLED',
  updated_at = NOW(),
  deleted = 0;

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
  enabled = 1,
  updated_at = NOW(),
  deleted = 0;

UPDATE service_category
SET enabled = 0, deleted = 1, updated_at = NOW()
WHERE name LIKE '%?%';

SET @user_id := (SELECT id FROM app_user WHERE openid = 'mock-openid' LIMIT 1);
SET @assist_user_id := (SELECT id FROM app_user WHERE openid = 'mock-openid-assist' LIMIT 1);
SET @cat_yuesao := (SELECT id FROM service_category WHERE name = '月嫂' LIMIT 1);
SET @cat_baomu := (SELECT id FROM service_category WHERE name = '保姆' LIMIT 1);
SET @cat_yuying := (SELECT id FROM service_category WHERE name = '育婴师' LIMIT 1);
SET @cat_old := (SELECT id FROM service_category WHERE name = '居家养老' LIMIT 1);
SET @cat_clean := (SELECT id FROM service_category WHERE name = '保洁师' LIMIT 1);

INSERT INTO service_staff (category_id, name, avatar_url, gender, age, city, district, education, experience_years, salary_min, salary_max, salary_unit, service_desc, status, recommended, sort_no, created_at, updated_at, deleted)
SELECT @cat_yuesao, '测试月嫂王春华', '', '女', 42, '杭州', '西湖区', '高中', 8, 12800.00, 15800.00, '月', '擅长新生儿护理、月子餐和产后恢复，适合测试首页推荐、找阿姨筛选、收藏和预约。', 'ONLINE', 1, 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM service_staff WHERE name = '测试月嫂王春华' AND deleted = 0);
INSERT INTO service_staff (category_id, name, avatar_url, gender, age, city, district, education, experience_years, salary_min, salary_max, salary_unit, service_desc, status, recommended, sort_no, created_at, updated_at, deleted)
SELECT @cat_yuying, '测试育婴师李敏', '', '女', 36, '杭州', '拱墅区', '大专', 6, 9800.00, 12800.00, '月', '擅长婴幼儿早教、辅食添加和作息培养，用于测试需求推荐和面试预约。', 'ONLINE', 1, 2, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM service_staff WHERE name = '测试育婴师李敏' AND deleted = 0);
INSERT INTO service_staff (category_id, name, avatar_url, gender, age, city, district, education, experience_years, salary_min, salary_max, salary_unit, service_desc, status, recommended, sort_no, created_at, updated_at, deleted)
SELECT @cat_clean, '测试保洁师张洁', '', '女', 33, '杭州', '滨江区', '中专', 5, 180.00, 260.00, '次', '擅长深度保洁、厨房油污和家电表面清洁，用于测试分类筛选。', 'ONLINE', 0, 3, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM service_staff WHERE name = '测试保洁师张洁' AND deleted = 0);
INSERT INTO service_staff (category_id, name, avatar_url, gender, age, city, district, education, experience_years, salary_min, salary_max, salary_unit, service_desc, status, recommended, sort_no, created_at, updated_at, deleted)
SELECT @cat_old, '测试养老护理赵兰', '', '女', 48, '杭州', '上城区', '高中', 10, 7800.00, 9800.00, '月', '擅长老人陪护、日常照料和康复陪练，用于测试后台上下架状态。', 'OFFLINE', 0, 4, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM service_staff WHERE name = '测试养老护理赵兰' AND deleted = 0);

UPDATE service_staff SET
  category_id = @cat_yuesao, status = 'ONLINE', recommended = 1, sort_no = 1,
  city = '杭州', district = '西湖区', education = '高中',
  salary_min = 12800.00, salary_max = 15800.00, salary_unit = '月',
  service_desc = '擅长新生儿护理、月子餐和产后恢复，适合测试首页推荐、找阿姨筛选、收藏和预约。',
  updated_at = NOW()
WHERE name = '测试月嫂王春华';
UPDATE service_staff SET
  category_id = @cat_yuying, status = 'ONLINE', recommended = 1, sort_no = 2,
  city = '杭州', district = '拱墅区', education = '大专',
  salary_min = 9800.00, salary_max = 12800.00, salary_unit = '月',
  service_desc = '擅长婴幼儿早教、辅食添加和作息培养，用于测试需求推荐和面试预约。',
  updated_at = NOW()
WHERE name = '测试育婴师李敏';
UPDATE service_staff SET
  category_id = @cat_clean, status = 'ONLINE', recommended = 0, sort_no = 3,
  city = '杭州', district = '滨江区', education = '中专',
  salary_min = 180.00, salary_max = 260.00, salary_unit = '次',
  service_desc = '擅长深度保洁、厨房油污和家电表面清洁，用于测试分类筛选。',
  updated_at = NOW()
WHERE name = '测试保洁师张洁';
UPDATE service_staff SET
  category_id = @cat_old, status = 'OFFLINE', recommended = 0, sort_no = 4,
  city = '杭州', district = '上城区', education = '高中',
  salary_min = 7800.00, salary_max = 9800.00, salary_unit = '月',
  service_desc = '擅长老人陪护、日常照料和康复陪练，用于测试后台上下架状态。',
  updated_at = NOW()
WHERE name = '测试养老护理赵兰';
UPDATE service_staff
SET status = 'OFFLINE', recommended = 0, deleted = 1, updated_at = NOW()
WHERE name LIKE '%?%' OR name IN ('测试月嫂', 'Codex排序测试阿姨');

SET @staff_yuesao := (SELECT id FROM service_staff WHERE name = '测试月嫂王春华' AND deleted = 0 LIMIT 1);
SET @staff_yuying := (SELECT id FROM service_staff WHERE name = '测试育婴师李敏' AND deleted = 0 LIMIT 1);
SET @staff_clean := (SELECT id FROM service_staff WHERE name = '测试保洁师张洁' AND deleted = 0 LIMIT 1);
SET @staff_old := (SELECT id FROM service_staff WHERE name = '测试养老护理赵兰' AND deleted = 0 LIMIT 1);

INSERT INTO staff_tag (staff_id, tag_name, created_at, updated_at, deleted)
SELECT @staff_yuesao, '月子餐', NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM staff_tag WHERE staff_id = @staff_yuesao AND tag_name = '月子餐' AND deleted = 0);
INSERT INTO staff_tag (staff_id, tag_name, created_at, updated_at, deleted)
SELECT @staff_yuesao, '新生儿护理', NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM staff_tag WHERE staff_id = @staff_yuesao AND tag_name = '新生儿护理' AND deleted = 0);
INSERT INTO staff_tag (staff_id, tag_name, created_at, updated_at, deleted)
SELECT @staff_yuying, '辅食添加', NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM staff_tag WHERE staff_id = @staff_yuying AND tag_name = '辅食添加' AND deleted = 0);
INSERT INTO staff_tag (staff_id, tag_name, created_at, updated_at, deleted)
SELECT @staff_clean, '深度保洁', NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM staff_tag WHERE staff_id = @staff_clean AND tag_name = '深度保洁' AND deleted = 0);

INSERT INTO staff_certificate (staff_id, certificate_name, file_url, sort_no, created_at, updated_at, deleted)
SELECT @staff_yuesao, '母婴护理证', '本地测试证书-母婴护理证.jpg', 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM staff_certificate WHERE staff_id = @staff_yuesao AND certificate_name = '母婴护理证' AND deleted = 0);
INSERT INTO staff_certificate (staff_id, certificate_name, file_url, sort_no, created_at, updated_at, deleted)
SELECT @staff_yuying, '育婴员证', '本地测试证书-育婴员证.jpg', 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM staff_certificate WHERE staff_id = @staff_yuying AND certificate_name = '育婴员证' AND deleted = 0);
UPDATE staff_certificate SET file_url = '本地测试证书-母婴护理证.jpg', updated_at = NOW()
WHERE staff_id = @staff_yuesao AND certificate_name = '母婴护理证' AND deleted = 0;
UPDATE staff_certificate SET file_url = '本地测试证书-育婴员证.jpg', updated_at = NOW()
WHERE staff_id = @staff_yuying AND certificate_name = '育婴员证' AND deleted = 0;

INSERT INTO staff_photo (staff_id, photo_url, sort_no, created_at, updated_at, deleted)
SELECT @staff_yuesao, '', 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM staff_photo WHERE staff_id = @staff_yuesao AND sort_no = 1 AND deleted = 0);
INSERT INTO staff_photo (staff_id, photo_url, sort_no, created_at, updated_at, deleted)
SELECT @staff_yuying, '', 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM staff_photo WHERE staff_id = @staff_yuying AND sort_no = 1 AND deleted = 0);
UPDATE staff_photo SET photo_url = '', updated_at = NOW()
WHERE photo_url LIKE 'https://example.com/test/%' AND deleted = 0;

INSERT INTO staff_work_experience (staff_id, start_date, end_date, description, created_at, updated_at, deleted)
SELECT @staff_yuesao, '2021-01-01', '2025-12-31', '服务过 40 多个新生儿家庭，熟悉月子期护理流程。', NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM staff_work_experience WHERE staff_id = @staff_yuesao AND description LIKE '服务过 40 多个新生儿家庭%' AND deleted = 0);
INSERT INTO staff_work_experience (staff_id, start_date, end_date, description, created_at, updated_at, deleted)
SELECT @staff_yuying, '2022-03-01', '2025-12-31', '长期照护 0-3 岁婴幼儿，擅长作息建立。', NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM staff_work_experience WHERE staff_id = @staff_yuying AND description LIKE '长期照护 0-3 岁婴幼儿%' AND deleted = 0);

INSERT INTO user_staff_favorite (user_id, staff_id, created_at, updated_at, deleted)
VALUES
  (@user_id, @staff_yuesao, NOW(), NOW(), 0),
  (@user_id, @staff_yuying, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE updated_at = NOW(), deleted = 0;

INSERT INTO user_demand (user_id, title, category_id, maternity_period, contact_name, contact_phone, gender, live_in, expected_salary, city, district, address, remark, audit_status, follow_status, created_at, updated_at, deleted)
SELECT @user_id, '测试需求-待审核月嫂', @cat_yuesao, '26天', '陈女士', '13800000001', '女', 1, '12000-15000/月', '杭州', '西湖区', '文三路测试小区', '用于测试小程序取消需求和后台审核通过。', 'PENDING', 'TO_FOLLOW', NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM user_demand WHERE user_id = @user_id AND title = '测试需求-待审核月嫂' AND deleted = 0);
INSERT INTO user_demand (user_id, title, category_id, maternity_period, contact_name, contact_phone, gender, live_in, expected_salary, city, district, address, remark, audit_status, follow_status, created_at, updated_at, deleted)
SELECT @user_id, '测试需求-已通过育婴', @cat_yuying, NULL, '陈女士', '13800000001', '女', 0, '9000-12000/月', '杭州', '拱墅区', '祥符街道测试小区', '已通过并带两条推荐，用于测试推荐阿姨列表和预约面试。', 'APPROVED', 'MATCHED', DATE_SUB(NOW(), INTERVAL 2 DAY), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM user_demand WHERE user_id = @user_id AND title = '测试需求-已通过育婴' AND deleted = 0);
INSERT INTO user_demand (user_id, title, category_id, maternity_period, contact_name, contact_phone, gender, live_in, expected_salary, city, district, address, remark, audit_status, follow_status, created_at, updated_at, deleted)
SELECT @user_id, '测试需求-已拒绝保洁', @cat_clean, NULL, '陈女士', '13800000001', '女', 0, '200-300/次', '杭州', '滨江区', '长河街道测试小区', '资料不完整的拒绝示例，用于测试审核结果消息。', 'REJECTED', 'CLOSED', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM user_demand WHERE user_id = @user_id AND title = '测试需求-已拒绝保洁' AND deleted = 0);

SET @demand_pending := (SELECT id FROM user_demand WHERE user_id = @user_id AND title = '测试需求-待审核月嫂' AND deleted = 0 LIMIT 1);
SET @demand_approved := (SELECT id FROM user_demand WHERE user_id = @user_id AND title = '测试需求-已通过育婴' AND deleted = 0 LIMIT 1);
SET @demand_rejected := (SELECT id FROM user_demand WHERE user_id = @user_id AND title = '测试需求-已拒绝保洁' AND deleted = 0 LIMIT 1);

INSERT INTO demand_recommendation (demand_id, staff_id, reason, status, sort_no, created_at, updated_at, deleted)
SELECT @demand_approved, @staff_yuying, '育婴经验匹配，距离服务地址近，可优先面试。', 'RECOMMENDED', 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM demand_recommendation WHERE demand_id = @demand_approved AND staff_id = @staff_yuying AND deleted = 0);
INSERT INTO demand_recommendation (demand_id, staff_id, reason, status, sort_no, created_at, updated_at, deleted)
SELECT @demand_approved, @staff_yuesao, '护理经验丰富，可兼顾新生儿照护建议。', 'RECOMMENDED', 2, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM demand_recommendation WHERE demand_id = @demand_approved AND staff_id = @staff_yuesao AND deleted = 0);

INSERT INTO interview_appointment (user_id, staff_id, demand_id, contact_name, contact_phone, status, admin_note, created_at, updated_at, deleted)
SELECT @user_id, @staff_yuying, @demand_approved, '陈女士', '13800000001', 'PENDING', '测试数据：待后台联系', NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM interview_appointment WHERE user_id = @user_id AND staff_id = @staff_yuying AND status = 'PENDING' AND deleted = 0);
INSERT INTO interview_appointment (user_id, staff_id, demand_id, contact_name, contact_phone, status, admin_note, created_at, updated_at, deleted)
SELECT @user_id, @staff_yuesao, @demand_approved, '陈女士', '13800000001', 'ARRANGED', '测试数据：已安排明天下午面试', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM interview_appointment WHERE user_id = @user_id AND staff_id = @staff_yuesao AND status = 'ARRANGED' AND deleted = 0);

INSERT INTO service_order (order_no, user_id, staff_id, demand_id, category_id, amount, start_date, end_date, status, admin_note, created_at, updated_at, deleted)
VALUES ('SO-TEST-001', @user_id, @staff_yuying, @demand_approved, @cat_yuying, 9800.00, DATE_ADD(CURDATE(), INTERVAL 3 DAY), DATE_ADD(CURDATE(), INTERVAL 33 DAY), 'WAIT_START', '测试服务订单', NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  staff_id = VALUES(staff_id),
  demand_id = VALUES(demand_id),
  category_id = VALUES(category_id),
  amount = VALUES(amount),
  start_date = VALUES(start_date),
  end_date = VALUES(end_date),
  status = VALUES(status),
  admin_note = VALUES(admin_note),
  updated_at = NOW(),
  deleted = 0;
SET @service_order_id := (SELECT id FROM service_order WHERE order_no = 'SO-TEST-001' LIMIT 1);

INSERT INTO contract (user_id, staff_id, demand_id, service_order_id, contract_no, title, file_url, status, signed_at, terminated_at, created_at, updated_at, deleted)
VALUES (@user_id, @staff_yuying, @demand_approved, @service_order_id, 'HT-TEST-001', '测试育婴师服务合同', '本地测试合同-HT-TEST-001.pdf', 'SIGNED', NOW(), NULL, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  staff_id = VALUES(staff_id),
  demand_id = VALUES(demand_id),
  service_order_id = VALUES(service_order_id),
  title = VALUES(title),
  file_url = VALUES(file_url),
  status = VALUES(status),
  signed_at = VALUES(signed_at),
  terminated_at = NULL,
  updated_at = NOW(),
  deleted = 0;

INSERT INTO group_product (title, cover_url, original_price, single_price, group_price, group_size, valid_days, sold_count, notice, guarantee, description, status, created_at, updated_at, deleted)
SELECT '测试团购-深度保洁体验', '', 429.00, 359.00, 299.00, 2, 30, 18, '提前一天预约，服务范围为普通住宅。', '未服务可改期，服务后不做真实退款。', '适合厨房、卫生间和全屋基础深度保洁。', 'ONLINE', NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM group_product WHERE title = '测试团购-深度保洁体验' AND deleted = 0);
UPDATE group_product SET
  cover_url = '',
  original_price = 429.00,
  single_price = 359.00,
  group_price = 299.00,
  group_size = 2,
  valid_days = 30,
  sold_count = 18,
  notice = '提前一天预约，服务范围为普通住宅。',
  guarantee = '未服务可改期，服务后不做真实退款。',
  description = '适合厨房、卫生间和全屋基础深度保洁。',
  status = 'ONLINE',
  updated_at = NOW()
WHERE title = '测试团购-深度保洁体验' AND deleted = 0;
UPDATE group_product SET cover_url = '', updated_at = NOW()
WHERE cover_url LIKE 'https://example.com/test/%' AND deleted = 0;
UPDATE group_product SET status = 'OFFLINE', deleted = 1, updated_at = NOW()
WHERE title LIKE '%?%';
SET @product_clean := (SELECT id FROM group_product WHERE title = '测试团购-深度保洁体验' AND deleted = 0 LIMIT 1);

INSERT INTO group_team (product_id, leader_user_id, group_size, joined_count, expire_at, status, created_at, updated_at, deleted)
SELECT @product_clean, @user_id, 2, 1, DATE_ADD(NOW(), INTERVAL 1 DAY), 'GROUPING', NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM group_team WHERE product_id = @product_clean AND leader_user_id = @user_id AND status = 'GROUPING' AND deleted = 0);
INSERT INTO group_team (product_id, leader_user_id, group_size, joined_count, expire_at, status, created_at, updated_at, deleted)
SELECT @product_clean, @assist_user_id, 2, 2, DATE_ADD(NOW(), INTERVAL 1 DAY), 'SUCCESS', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM group_team WHERE product_id = @product_clean AND leader_user_id = @assist_user_id AND status = 'SUCCESS' AND deleted = 0);
SET @team_grouping := (SELECT id FROM group_team WHERE product_id = @product_clean AND leader_user_id = @user_id AND status = 'GROUPING' AND deleted = 0 LIMIT 1);
SET @team_success := (SELECT id FROM group_team WHERE product_id = @product_clean AND leader_user_id = @assist_user_id AND status = 'SUCCESS' AND deleted = 0 LIMIT 1);

INSERT INTO group_order (order_no, user_id, product_id, group_team_id, buy_type, quantity, amount, status, paid_at, valid_until, created_at, updated_at, deleted)
VALUES
  ('GO-TEST-SINGLE-001', @user_id, @product_clean, NULL, 'SINGLE', 1, 359.00, 'WAIT_USE', NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), NOW(), NOW(), 0),
  ('GO-TEST-GROUP-001', @user_id, @product_clean, @team_grouping, 'GROUP', 1, 299.00, 'WAIT_SHARE', NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), NOW(), NOW(), 0),
  ('GO-TEST-GROUP-002', @assist_user_id, @product_clean, @team_success, 'GROUP', 1, 299.00, 'WAIT_USE', NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  product_id = VALUES(product_id),
  group_team_id = VALUES(group_team_id),
  buy_type = VALUES(buy_type),
  quantity = VALUES(quantity),
  amount = VALUES(amount),
  status = VALUES(status),
  paid_at = VALUES(paid_at),
  valid_until = VALUES(valid_until),
  updated_at = NOW(),
  deleted = 0;

INSERT INTO content_config (content_type, title, image_url, content, sort_no, enabled, created_at, updated_at, deleted)
SELECT 'BANNER', '测试首页轮播-安心到家', '', '新人发布需求后，平台后台审核并推荐合适阿姨。', 1, 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM content_config WHERE content_type = 'BANNER' AND title = '测试首页轮播-安心到家' AND deleted = 0);
INSERT INTO content_config (content_type, title, image_url, content, sort_no, enabled, created_at, updated_at, deleted)
SELECT 'SIGN_TIP', '测试签约动态', '', '测试用户已签约育婴师服务', 1, 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM content_config WHERE content_type = 'SIGN_TIP' AND title = '测试签约动态' AND deleted = 0);
INSERT INTO content_config (content_type, title, image_url, content, sort_no, enabled, created_at, updated_at, deleted)
SELECT 'FAQ', '如何测试需求审核和推荐', '', '先用小程序发布需求，再到后台审核通过并添加推荐阿姨，最后回到需求详情查看推荐列表。', 1, 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM content_config WHERE content_type = 'FAQ' AND title = '如何测试需求审核和推荐' AND deleted = 0);
INSERT INTO content_config (content_type, title, image_url, content, sort_no, enabled, created_at, updated_at, deleted)
SELECT 'AGREEMENT', '测试用户协议', '', '本内容用于本地联调展示，不代表正式协议文本。', 1, 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM content_config WHERE content_type = 'AGREEMENT' AND title = '测试用户协议' AND deleted = 0);
INSERT INTO content_config (content_type, title, image_url, content, sort_no, enabled, created_at, updated_at, deleted)
SELECT 'PRIVACY', '测试隐私政策', '', '本内容用于本地联调展示，不代表正式隐私政策。', 1, 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM content_config WHERE content_type = 'PRIVACY' AND title = '测试隐私政策' AND deleted = 0);
INSERT INTO content_config (content_type, title, image_url, content, sort_no, enabled, created_at, updated_at, deleted)
SELECT 'ABOUT', '关于本地测试平台', '', '这是家政到家服务小程序的本地测试内容，可用于验证内容管理和我的页面入口。', 1, 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM content_config WHERE content_type = 'ABOUT' AND title = '关于本地测试平台' AND deleted = 0);
UPDATE content_config SET enabled = 1, updated_at = NOW(), deleted = 0
WHERE title IN ('测试首页轮播-安心到家', '测试签约动态', '如何测试需求审核和推荐', '测试用户协议', '测试隐私政策', '关于本地测试平台');
UPDATE content_config SET image_url = '', updated_at = NOW()
WHERE image_url LIKE 'https://example.com/test/%' AND deleted = 0;
UPDATE content_config SET enabled = 0, deleted = 1, updated_at = NOW()
WHERE title LIKE '%?%';

UPDATE user_demand SET deleted = 1, updated_at = NOW()
WHERE title LIKE '%?%';

UPDATE system_message
SET deleted = 1, updated_at = NOW()
WHERE user_id = @user_id
  AND (
    message_type IN ('DEMAND_APPROVED', 'DEMAND_REJECTED', 'STAFF_RECOMMENDED', 'CONTRACT_CREATED', 'GROUP_TEAM_SUCCESS', 'GROUP_SUCCESS')
    OR title LIKE '%?%'
    OR content LIKE '%?%'
  );

INSERT INTO system_message (user_id, title, content, message_type, read_flag, created_at, updated_at, deleted)
SELECT @user_id, '需求审核通过', '你的测试需求“测试需求-已通过育婴”已审核通过，平台已为你推荐阿姨。', 'DEMAND_APPROVED', 0, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM system_message WHERE user_id = @user_id AND title = '需求审核通过' AND message_type = 'DEMAND_APPROVED' AND deleted = 0);
INSERT INTO system_message (user_id, title, content, message_type, read_flag, created_at, updated_at, deleted)
SELECT @user_id, '需求审核未通过', '你的测试需求“测试需求-已拒绝保洁”资料不完整，请修改后重新发布。', 'DEMAND_REJECTED', 0, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM system_message WHERE user_id = @user_id AND title = '需求审核未通过' AND message_type = 'DEMAND_REJECTED' AND deleted = 0);
INSERT INTO system_message (user_id, title, content, message_type, read_flag, created_at, updated_at, deleted)
SELECT @user_id, '新的阿姨推荐', '平台已为你的育婴需求推荐 2 位阿姨，可进入需求详情查看。', 'STAFF_RECOMMENDED', 0, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM system_message WHERE user_id = @user_id AND title = '新的阿姨推荐' AND message_type = 'STAFF_RECOMMENDED' AND deleted = 0);
INSERT INTO system_message (user_id, title, content, message_type, read_flag, created_at, updated_at, deleted)
SELECT @user_id, '合同已创建', '测试育婴师服务合同已创建，可用于后台合同数据核对。', 'CONTRACT_CREATED', 0, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM system_message WHERE user_id = @user_id AND title = '合同已创建' AND message_type = 'CONTRACT_CREATED' AND deleted = 0);
INSERT INTO system_message (user_id, title, content, message_type, read_flag, created_at, updated_at, deleted)
SELECT @user_id, '拼团测试消息', '你的测试拼团订单已创建，可用于后续团购流程联调。', 'GROUP_TEAM_SUCCESS', 1, DATE_SUB(NOW(), INTERVAL 1 DAY), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM system_message WHERE user_id = @user_id AND title = '拼团测试消息' AND message_type = 'GROUP_TEAM_SUCCESS' AND deleted = 0);
