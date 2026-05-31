CREATE DATABASE IF NOT EXISTS `ry-vue`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `ry-vue`;

CREATE TABLE IF NOT EXISTS app_user (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  openid varchar(64) NOT NULL COMMENT '微信 openid',
  unionid varchar(64) DEFAULT NULL COMMENT '微信 unionid',
  nickname varchar(64) DEFAULT NULL COMMENT '昵称',
  avatar_url varchar(512) DEFAULT NULL COMMENT '头像',
  phone varchar(32) DEFAULT NULL COMMENT '手机号',
  status varchar(32) NOT NULL DEFAULT 'ENABLED' COMMENT '状态: ENABLED, DISABLED',
  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0 未删除, 1 已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_app_user_openid (openid),
  KEY idx_app_user_phone (phone),
  CONSTRAINT chk_app_user_status CHECK (status IN ('ENABLED', 'DISABLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小程序用户表';

CREATE TABLE IF NOT EXISTS service_category (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  name varchar(64) NOT NULL COMMENT '分类名称',
  icon_url varchar(512) DEFAULT NULL COMMENT '图标',
  sort_no int NOT NULL DEFAULT 0 COMMENT '排序',
  enabled tinyint NOT NULL DEFAULT 1 COMMENT '是否启用',
  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0 未删除, 1 已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_service_category_name (name),
  KEY idx_service_category_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务分类表';

CREATE TABLE IF NOT EXISTS service_staff (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  category_id bigint NOT NULL COMMENT '主服务分类',
  name varchar(64) NOT NULL COMMENT '姓名',
  avatar_url varchar(512) DEFAULT NULL COMMENT '头像',
  gender varchar(16) DEFAULT NULL COMMENT '性别',
  age int DEFAULT NULL COMMENT '年龄',
  city varchar(64) DEFAULT NULL COMMENT '城市',
  district varchar(64) DEFAULT NULL COMMENT '区县',
  education varchar(64) DEFAULT NULL COMMENT '学历',
  experience_years int DEFAULT NULL COMMENT '工作年限',
  salary_min decimal(10,2) DEFAULT NULL COMMENT '最低薪资',
  salary_max decimal(10,2) DEFAULT NULL COMMENT '最高薪资',
  salary_unit varchar(16) DEFAULT NULL COMMENT '月、天、次、小时',
  service_desc text COMMENT '服务说明',
  status varchar(32) NOT NULL DEFAULT 'DRAFT' COMMENT '状态: DRAFT, ONLINE, OFFLINE',
  recommended tinyint NOT NULL DEFAULT 0 COMMENT '是否推荐',
  sort_no int NOT NULL DEFAULT 0 COMMENT '排序',
  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0 未删除, 1 已删除',
  PRIMARY KEY (id),
  KEY idx_service_staff_category_id (category_id),
  KEY idx_service_staff_status (status),
  KEY idx_service_staff_recommended (recommended),
  KEY idx_service_staff_city_district (city, district),
  CONSTRAINT chk_service_staff_status CHECK (status IN ('DRAFT', 'ONLINE', 'OFFLINE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务人员表';

CREATE TABLE IF NOT EXISTS staff_tag (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  staff_id bigint NOT NULL COMMENT '服务人员 ID',
  tag_name varchar(64) NOT NULL COMMENT '标签名称',
  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0 未删除, 1 已删除',
  PRIMARY KEY (id),
  KEY idx_staff_tag_staff_id (staff_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务人员标签表';

CREATE TABLE IF NOT EXISTS staff_certificate (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  staff_id bigint NOT NULL COMMENT '服务人员 ID',
  certificate_name varchar(128) NOT NULL COMMENT '证书名称',
  file_url varchar(512) NOT NULL COMMENT '证书图片',
  sort_no int NOT NULL DEFAULT 0 COMMENT '排序',
  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0 未删除, 1 已删除',
  PRIMARY KEY (id),
  KEY idx_staff_certificate_staff_id (staff_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务人员证书表';

CREATE TABLE IF NOT EXISTS staff_photo (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  staff_id bigint NOT NULL COMMENT '服务人员 ID',
  photo_url varchar(512) NOT NULL COMMENT '图片地址',
  sort_no int NOT NULL DEFAULT 0 COMMENT '排序',
  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0 未删除, 1 已删除',
  PRIMARY KEY (id),
  KEY idx_staff_photo_staff_id (staff_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务人员照片表';

CREATE TABLE IF NOT EXISTS staff_work_experience (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  staff_id bigint NOT NULL COMMENT '服务人员 ID',
  start_date date DEFAULT NULL COMMENT '开始日期',
  end_date date DEFAULT NULL COMMENT '结束日期',
  description text COMMENT '经历描述',
  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0 未删除, 1 已删除',
  PRIMARY KEY (id),
  KEY idx_staff_work_experience_staff_id (staff_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务人员工作经历表';

CREATE TABLE IF NOT EXISTS user_staff_favorite (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id bigint NOT NULL COMMENT '用户 ID',
  staff_id bigint NOT NULL COMMENT '服务人员 ID',
  created_at datetime NOT NULL COMMENT '创建时间',
  updated_at datetime NOT NULL COMMENT '更新时间',
  deleted tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_staff_favorite (user_id, staff_id),
  KEY idx_user_staff_favorite_user_id (user_id),
  KEY idx_user_staff_favorite_staff_id (staff_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户服务人员收藏表';

CREATE TABLE IF NOT EXISTS user_demand (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id bigint NOT NULL COMMENT '用户 ID',
  title varchar(128) NOT NULL COMMENT '标题',
  category_id bigint NOT NULL COMMENT '服务类型',
  maternity_period varchar(32) DEFAULT NULL COMMENT '月嫂周期',
  contact_name varchar(64) NOT NULL COMMENT '联系人',
  contact_phone varchar(32) NOT NULL COMMENT '联系电话',
  gender varchar(16) DEFAULT NULL COMMENT '用户性别',
  live_in tinyint DEFAULT NULL COMMENT '是否住家',
  expected_salary varchar(64) DEFAULT NULL COMMENT '薪资待遇',
  city varchar(64) DEFAULT NULL COMMENT '城市',
  district varchar(64) DEFAULT NULL COMMENT '区县',
  address varchar(255) DEFAULT NULL COMMENT '详细地址',
  remark text COMMENT '补充说明',
  audit_status varchar(32) NOT NULL DEFAULT 'PENDING' COMMENT '审核状态',
  follow_status varchar(32) NOT NULL DEFAULT 'TO_FOLLOW' COMMENT '后台跟进状态',
  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0 未删除, 1 已删除',
  PRIMARY KEY (id),
  KEY idx_user_demand_user_id (user_id),
  KEY idx_user_demand_category_id (category_id),
  KEY idx_user_demand_audit_status (audit_status),
  KEY idx_user_demand_follow_status (follow_status),
  CONSTRAINT chk_user_demand_audit_status CHECK (audit_status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELED')),
  CONSTRAINT chk_user_demand_follow_status CHECK (follow_status IN ('TO_FOLLOW', 'CONTACTED', 'MATCHED', 'SIGNED', 'CLOSED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户需求表';

CREATE TABLE IF NOT EXISTS demand_recommendation (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  demand_id bigint NOT NULL COMMENT '需求 ID',
  staff_id bigint NOT NULL COMMENT '服务人员 ID',
  reason varchar(255) DEFAULT NULL COMMENT '推荐理由',
  status varchar(32) NOT NULL DEFAULT 'RECOMMENDED' COMMENT '推荐状态',
  sort_no int NOT NULL DEFAULT 0 COMMENT '排序',
  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0 未删除, 1 已删除',
  PRIMARY KEY (id),
  KEY idx_demand_recommendation_demand_id (demand_id),
  KEY idx_demand_recommendation_staff_id (staff_id),
  CONSTRAINT chk_demand_recommendation_status CHECK (status IN ('RECOMMENDED', 'VIEWED', 'INTERVIEWED', 'IGNORED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推荐阿姨表';

CREATE TABLE IF NOT EXISTS interview_appointment (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id bigint NOT NULL COMMENT '用户 ID',
  staff_id bigint NOT NULL COMMENT '服务人员 ID',
  demand_id bigint DEFAULT NULL COMMENT '需求 ID',
  contact_name varchar(64) NOT NULL COMMENT '联系人',
  contact_phone varchar(32) NOT NULL COMMENT '联系电话',
  status varchar(32) NOT NULL DEFAULT 'PENDING' COMMENT '状态',
  admin_note text COMMENT '后台备注',
  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0 未删除, 1 已删除',
  PRIMARY KEY (id),
  KEY idx_interview_appointment_user_id (user_id),
  KEY idx_interview_appointment_staff_id (staff_id),
  KEY idx_interview_appointment_demand_id (demand_id),
  KEY idx_interview_appointment_status (status),
  CONSTRAINT chk_interview_appointment_status CHECK (status IN ('PENDING', 'CONTACTED', 'ARRANGED', 'COMPLETED', 'CANCELED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约面试表';

CREATE TABLE IF NOT EXISTS contract (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id bigint NOT NULL COMMENT '用户 ID',
  staff_id bigint NOT NULL COMMENT '服务人员 ID',
  demand_id bigint DEFAULT NULL COMMENT '需求 ID',
  service_order_id bigint DEFAULT NULL COMMENT '服务订单 ID',
  contract_no varchar(64) NOT NULL COMMENT '合同编号',
  title varchar(128) NOT NULL COMMENT '合同标题',
  file_url varchar(512) NOT NULL COMMENT '合同文件',
  status varchar(32) NOT NULL DEFAULT 'SIGNED' COMMENT '状态',
  signed_at datetime DEFAULT NULL COMMENT '签署时间',
  terminated_at datetime DEFAULT NULL COMMENT '终止时间',
  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0 未删除, 1 已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_contract_no (contract_no),
  KEY idx_contract_user_id (user_id),
  KEY idx_contract_staff_id (staff_id),
  KEY idx_contract_demand_id (demand_id),
  KEY idx_contract_service_order_id (service_order_id),
  CONSTRAINT chk_contract_status CHECK (status IN ('SIGNED', 'TERMINATED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='合同表';

CREATE TABLE IF NOT EXISTS service_order (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  order_no varchar(64) NOT NULL COMMENT '订单号',
  user_id bigint NOT NULL COMMENT '用户 ID',
  staff_id bigint NOT NULL COMMENT '服务人员 ID',
  demand_id bigint DEFAULT NULL COMMENT '需求 ID',
  category_id bigint NOT NULL COMMENT '服务类型',
  amount decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '金额',
  start_date date DEFAULT NULL COMMENT '服务开始日期',
  end_date date DEFAULT NULL COMMENT '服务结束日期',
  status varchar(32) NOT NULL DEFAULT 'WAIT_START' COMMENT '状态',
  admin_note text COMMENT '后台备注',
  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0 未删除, 1 已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_service_order_no (order_no),
  KEY idx_service_order_user_id (user_id),
  KEY idx_service_order_staff_id (staff_id),
  KEY idx_service_order_demand_id (demand_id),
  KEY idx_service_order_category_id (category_id),
  KEY idx_service_order_status (status),
  CONSTRAINT chk_service_order_status CHECK (status IN ('WAIT_START', 'SERVING', 'COMPLETED', 'CANCELED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务订单表';

CREATE TABLE IF NOT EXISTS group_product (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  title varchar(128) NOT NULL COMMENT '商品名称',
  cover_url varchar(512) DEFAULT NULL COMMENT '封面图',
  original_price decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '原价',
  single_price decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '单独购买价',
  group_price decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '拼团价',
  group_size int NOT NULL DEFAULT 2 COMMENT '成团人数',
  valid_days int NOT NULL DEFAULT 1 COMMENT '购买后有效天数',
  sold_count int NOT NULL DEFAULT 0 COMMENT '已售数量',
  notice text COMMENT '消费须知',
  guarantee text COMMENT '服务保障',
  description text COMMENT '产品介绍',
  status varchar(32) NOT NULL DEFAULT 'DRAFT' COMMENT '状态',
  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0 未删除, 1 已删除',
  PRIMARY KEY (id),
  KEY idx_group_product_status (status),
  CONSTRAINT chk_group_product_status CHECK (status IN ('DRAFT', 'ONLINE', 'OFFLINE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团购商品表';

CREATE TABLE IF NOT EXISTS group_team (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  product_id bigint NOT NULL COMMENT '商品 ID',
  leader_user_id bigint NOT NULL COMMENT '团长用户 ID',
  group_size int NOT NULL COMMENT '成团人数',
  joined_count int NOT NULL DEFAULT 1 COMMENT '已加入人数',
  expire_at datetime NOT NULL COMMENT '过期时间',
  status varchar(32) NOT NULL DEFAULT 'GROUPING' COMMENT '状态',
  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0 未删除, 1 已删除',
  PRIMARY KEY (id),
  KEY idx_group_team_product_id (product_id),
  KEY idx_group_team_leader_user_id (leader_user_id),
  KEY idx_group_team_status (status),
  CONSTRAINT chk_group_team_status CHECK (status IN ('GROUPING', 'SUCCESS', 'FAILED', 'CANCELED')),
  CONSTRAINT chk_group_team_joined_count CHECK (joined_count <= group_size)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='拼团表';

CREATE TABLE IF NOT EXISTS group_order (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  order_no varchar(64) NOT NULL COMMENT '订单号',
  user_id bigint NOT NULL COMMENT '用户 ID',
  product_id bigint NOT NULL COMMENT '商品 ID',
  group_team_id bigint DEFAULT NULL COMMENT '拼团 ID',
  buy_type varchar(32) NOT NULL COMMENT '购买方式: SINGLE, GROUP',
  quantity int NOT NULL DEFAULT 1 COMMENT '数量',
  amount decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '实付金额',
  status varchar(32) NOT NULL COMMENT '状态',
  paid_at datetime DEFAULT NULL COMMENT '模拟支付时间',
  valid_until datetime DEFAULT NULL COMMENT '有效期截止',
  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0 未删除, 1 已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_group_order_no (order_no),
  KEY idx_group_order_user_id (user_id),
  KEY idx_group_order_product_id (product_id),
  KEY idx_group_order_group_team_id (group_team_id),
  KEY idx_group_order_status (status),
  CONSTRAINT chk_group_order_buy_type CHECK (buy_type IN ('SINGLE', 'GROUP')),
  CONSTRAINT chk_group_order_status CHECK (status IN ('WAIT_SHARE', 'WAIT_USE', 'USED', 'EXPIRED', 'AFTER_SALE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团购订单表';

CREATE TABLE IF NOT EXISTS content_config (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  content_type varchar(64) NOT NULL COMMENT '内容类型',
  title varchar(128) DEFAULT NULL COMMENT '标题',
  image_url varchar(512) DEFAULT NULL COMMENT '图片',
  content text COMMENT '内容',
  sort_no int NOT NULL DEFAULT 0 COMMENT '排序',
  enabled tinyint NOT NULL DEFAULT 1 COMMENT '是否启用',
  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0 未删除, 1 已删除',
  PRIMARY KEY (id),
  KEY idx_content_config_type (content_type),
  KEY idx_content_config_enabled (enabled),
  CONSTRAINT chk_content_config_type CHECK (content_type IN ('BANNER', 'SIGN_TIP', 'FAQ', 'AGREEMENT', 'PRIVACY', 'ABOUT'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容配置表';

CREATE TABLE IF NOT EXISTS file_asset (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  original_name varchar(255) NOT NULL COMMENT '原始文件名',
  object_key varchar(255) NOT NULL COMMENT 'OSS object key',
  url varchar(512) NOT NULL COMMENT '访问地址',
  content_type varchar(128) DEFAULT NULL COMMENT '文件类型',
  size_bytes bigint DEFAULT NULL COMMENT '文件大小',
  usage_type varchar(64) NOT NULL COMMENT '用途类型',
  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0 未删除, 1 已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_file_asset_object_key (object_key),
  KEY idx_file_asset_usage_type (usage_type),
  CONSTRAINT chk_file_asset_usage_type CHECK (usage_type IN ('AVATAR', 'CERTIFICATE', 'CONTRACT', 'PRODUCT', 'BANNER'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件资源表';

CREATE TABLE IF NOT EXISTS system_message (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id bigint NOT NULL COMMENT '用户 ID',
  title varchar(128) NOT NULL COMMENT '标题',
  content varchar(512) NOT NULL COMMENT '内容',
  message_type varchar(64) NOT NULL COMMENT '消息类型',
  read_flag tinyint NOT NULL DEFAULT 0 COMMENT '是否已读',
  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0 未删除, 1 已删除',
  PRIMARY KEY (id),
  KEY idx_system_message_user_id (user_id),
  KEY idx_system_message_read_flag (read_flag),
  KEY idx_system_message_type (message_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统消息表';
