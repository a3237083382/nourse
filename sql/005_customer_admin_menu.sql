USE `ry-vue`;

CREATE TEMPORARY TABLE IF NOT EXISTS tmp_customer_hidden_menu_ids (
  menu_id bigint NOT NULL PRIMARY KEY
);

TRUNCATE TABLE tmp_customer_hidden_menu_ids;

INSERT INTO tmp_customer_hidden_menu_ids (menu_id)
WITH RECURSIVE hidden_menus AS (
  SELECT menu_id
  FROM sys_menu
  WHERE parent_id = 0
    AND menu_name IN ('系统管理', '租户管理', '系统监控', '系统工具', 'PLUS官网', '测试菜单', '工作流', '我的任务')
  UNION ALL
  SELECT child.menu_id
  FROM sys_menu child
  INNER JOIN hidden_menus parent ON child.parent_id = parent.menu_id
)
SELECT menu_id
FROM hidden_menus;

UPDATE sys_menu
SET status = '1',
    visible = '1',
    update_time = NOW()
WHERE menu_id IN (SELECT menu_id FROM tmp_customer_hidden_menu_ids);

DROP TEMPORARY TABLE tmp_customer_hidden_menu_ids;
