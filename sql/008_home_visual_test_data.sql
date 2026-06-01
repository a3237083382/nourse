-- 首页展示测试数据：三类服务、三张轮播图、多条签约动态。

UPDATE service_category
SET enabled = 1,
    updated_at = NOW(),
    deleted = 0
WHERE deleted = 0;

UPDATE service_category
SET sort_no = CASE name
    WHEN '月嫂' THEN 10
    WHEN '保姆' THEN 20
    WHEN '育婴师' THEN 30
    ELSE sort_no
END,
updated_at = NOW()
WHERE name IN ('月嫂', '保姆', '育婴师') AND deleted = 0;

INSERT INTO service_staff (category_id, name, avatar_url, gender, age, city, district, education, experience_years,
    salary_min, salary_max, salary_unit, service_desc, status, recommended, sort_no, created_at, updated_at, deleted)
SELECT c.id, '测试保姆刘阿姨', '', '女', 45, '杭州', '拱墅区', '高中', 9,
       6800, 8800, '月', '擅长住家照护、家庭餐食和日常家务，适合测试首页保姆分类展示。', 'ONLINE', 1, 11, NOW(), NOW(), 0
FROM service_category c
WHERE c.name = '保姆' AND NOT EXISTS (
    SELECT 1 FROM service_staff s WHERE s.name = '测试保姆刘阿姨' AND s.deleted = 0
);

INSERT INTO service_staff (category_id, name, avatar_url, gender, age, city, district, education, experience_years,
    salary_min, salary_max, salary_unit, service_desc, status, recommended, sort_no, created_at, updated_at, deleted)
SELECT c.id, '测试育婴师赵老师', '', '女', 36, '杭州', '西湖区', '大专', 7,
       9800, 12800, '月', '熟悉辅食添加、作息培养和早教陪伴，适合测试育婴师分类展示。', 'ONLINE', 1, 12, NOW(), NOW(), 0
FROM service_category c
WHERE c.name = '育婴师' AND NOT EXISTS (
    SELECT 1 FROM service_staff s WHERE s.name = '测试育婴师赵老师' AND s.deleted = 0
);

UPDATE content_config
SET enabled = 0, deleted = 1, updated_at = NOW()
WHERE content_type IN ('BANNER', 'SIGN_TIP');

INSERT INTO content_config (content_type, title, image_url, content, sort_no, enabled, created_at, updated_at, deleted)
VALUES
('BANNER', '安心月嫂到家', '/static/banner-yuesao.png', '新生儿护理、月子餐和产后恢复，一站式安排。', 10, 1, NOW(), NOW(), 0),
('BANNER', '住家保姆服务', '/static/banner-baomu.png', '家庭餐食、老人陪护和日常家务，安心上门。', 20, 1, NOW(), NOW(), 0),
('BANNER', '专业育婴师', '/static/banner-yuying.png', '辅食喂养、早教陪伴和作息培养，科学照护。', 30, 1, NOW(), NOW(), 0);

INSERT INTO content_config (content_type, title, image_url, content, sort_no, enabled, created_at, updated_at, deleted)
VALUES
('SIGN_TIP', '签约动态-月嫂', '', '恭喜用户：李女士签约月嫂王春华成功！', 10, 1, NOW(), NOW(), 0),
('SIGN_TIP', '签约动态-保姆', '', '恭喜用户：张先生签约住家保姆刘阿姨成功！', 20, 1, NOW(), NOW(), 0),
('SIGN_TIP', '签约动态-育婴师', '', '恭喜用户：陈女士签约育婴师赵老师成功！', 30, 1, NOW(), NOW(), 0);

INSERT INTO group_product (title, cover_url, original_price, single_price, group_price, group_size,
    valid_days, sold_count, notice, guarantee, description, status, created_at, updated_at, deleted)
SELECT '测试团购-上户养老清洁', '/static/logo.png', 329.00, 269.00, 199.00, 2,
       7, 5, '下单后客服确认服务时间。', '平台服务人员上门，售后可联系平台。', '适合老人家庭的基础卫生清洁体验。', 'ONLINE', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM group_product WHERE title = '测试团购-上户养老清洁' AND deleted = 0
);
