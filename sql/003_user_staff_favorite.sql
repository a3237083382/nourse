USE `ry-vue`;

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
