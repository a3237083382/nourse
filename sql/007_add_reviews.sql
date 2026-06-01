CREATE TABLE IF NOT EXISTS service_order_review (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  service_order_id bigint NOT NULL COMMENT '服务订单 ID',
  user_id bigint NOT NULL COMMENT '用户 ID',
  staff_id bigint NOT NULL COMMENT '服务人员 ID',
  rating int NOT NULL COMMENT '评分 1-5',
  content varchar(512) DEFAULT NULL COMMENT '评价内容',
  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0 未删除, 1 已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_service_order_review_order (service_order_id, deleted),
  KEY idx_service_order_review_user_id (user_id),
  KEY idx_service_order_review_staff_id (staff_id),
  CONSTRAINT chk_service_order_review_rating CHECK (rating BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务订单评价表';

CREATE TABLE IF NOT EXISTS group_order_review (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  group_order_id bigint NOT NULL COMMENT '团购订单 ID',
  user_id bigint NOT NULL COMMENT '用户 ID',
  product_id bigint NOT NULL COMMENT '团购商品 ID',
  rating int NOT NULL COMMENT '评分 1-5',
  content varchar(512) DEFAULT NULL COMMENT '评价内容',
  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0 未删除, 1 已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_group_order_review_order (group_order_id, deleted),
  KEY idx_group_order_review_user_id (user_id),
  KEY idx_group_order_review_product_id (product_id),
  CONSTRAINT chk_group_order_review_rating CHECK (rating BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团购订单评价表';
