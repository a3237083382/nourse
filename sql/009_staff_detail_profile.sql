USE `ry-vue`;

DROP PROCEDURE IF EXISTS add_staff_detail_profile_columns;

DELIMITER //
CREATE PROCEDURE add_staff_detail_profile_columns()
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'service_staff' AND column_name = 'native_place') THEN
    ALTER TABLE service_staff ADD COLUMN native_place varchar(128) DEFAULT NULL COMMENT '籍贯' AFTER service_desc;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'service_staff' AND column_name = 'height_cm') THEN
    ALTER TABLE service_staff ADD COLUMN height_cm int DEFAULT NULL COMMENT '身高 cm' AFTER native_place;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'service_staff' AND column_name = 'weight_kg') THEN
    ALTER TABLE service_staff ADD COLUMN weight_kg int DEFAULT NULL COMMENT '体重 kg' AFTER height_cm;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'service_staff' AND column_name = 'birth_date') THEN
    ALTER TABLE service_staff ADD COLUMN birth_date date DEFAULT NULL COMMENT '出生年月' AFTER weight_kg;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'service_staff' AND column_name = 'marital_status') THEN
    ALTER TABLE service_staff ADD COLUMN marital_status varchar(32) DEFAULT NULL COMMENT '婚姻状态' AFTER birth_date;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'service_staff' AND column_name = 'self_intro') THEN
    ALTER TABLE service_staff ADD COLUMN self_intro text COMMENT '自我介绍' AFTER marital_status;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'service_staff' AND column_name = 'skills') THEN
    ALTER TABLE service_staff ADD COLUMN skills text COMMENT '个人技能' AFTER self_intro;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'service_staff' AND column_name = 'verification_note') THEN
    ALTER TABLE service_staff ADD COLUMN verification_note text COMMENT '平台核验说明' AFTER skills;
  END IF;
END//
DELIMITER ;

CALL add_staff_detail_profile_columns();
DROP PROCEDURE IF EXISTS add_staff_detail_profile_columns;

UPDATE service_staff
SET
  native_place = COALESCE(native_place, '湖南长沙'),
  height_cm = COALESCE(height_cm, 158),
  weight_kg = COALESCE(weight_kg, 56),
  birth_date = COALESCE(birth_date, '1988-06-01'),
  marital_status = COALESCE(marital_status, '已婚'),
  self_intro = COALESCE(NULLIF(self_intro, ''), '本人性格温和，做事细致，有多年家庭服务经验。熟悉新生儿护理、家庭保洁、老人照护和日常沟通，能根据客户家庭习惯安排服务细节，配合平台完成面试、签约和服务跟进。'),
  skills = COALESCE(NULLIF(skills, ''), '熟悉母婴护理、月子餐基础搭配、婴幼儿作息照护、家庭日常清洁、衣物整理、老人陪护、简单营养餐制作和服务沟通。'),
  verification_note = COALESCE(NULLIF(verification_note, ''), '身份资料已在平台留档，证书资料由后台人员核验，预约后由工作人员线下跟进，合同与订单状态可持续查看。')
WHERE deleted = 0;
