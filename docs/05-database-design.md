# 数据库设计草案

## 1. 命名规范

- 表名使用小写下划线。
- 主键统一使用 `id`。
- 创建时间字段：`created_at`。
- 更新时间字段：`updated_at`。
- 逻辑删除字段：`deleted`，0 表示未删除，1 表示已删除。
- 状态字段使用字符串枚举，便于阅读和调试。

## 2. 核心表

### 2.1 用户表 `app_user`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| openid | varchar(64) | 微信 openid |
| unionid | varchar(64) | 微信 unionid，可空 |
| nickname | varchar(64) | 昵称 |
| avatar_url | varchar(512) | 头像 |
| phone | varchar(32) | 手机号 |
| status | varchar(32) | ENABLED, DISABLED |
| created_at | datetime | 创建时间 |
| updated_at | datetime | 更新时间 |
| deleted | tinyint | 逻辑删除 |

### 2.2 服务分类表 `service_category`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| name | varchar(64) | 分类名称 |
| icon_url | varchar(512) | 图标 |
| sort_no | int | 排序 |
| enabled | tinyint | 是否启用 |

默认分类：

- 月嫂
- 保姆
- 育婴师
- 居家养老
- 保洁师
- 钟点工
- 成长陪伴师
- 家电清洗师

### 2.3 服务人员表 `service_staff`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| category_id | bigint | 主服务分类 |
| name | varchar(64) | 姓名 |
| avatar_url | varchar(512) | 头像 |
| gender | varchar(16) | 性别 |
| age | int | 年龄 |
| city | varchar(64) | 城市 |
| district | varchar(64) | 区县 |
| education | varchar(64) | 学历 |
| experience_years | int | 工作年限 |
| salary_min | decimal(10,2) | 最低薪资 |
| salary_max | decimal(10,2) | 最高薪资 |
| salary_unit | varchar(16) | 月、天、次、小时 |
| service_desc | text | 服务说明 |
| status | varchar(32) | DRAFT, ONLINE, OFFLINE |
| recommended | tinyint | 是否推荐 |
| sort_no | int | 排序 |

### 2.4 服务人员标签表 `staff_tag`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| staff_id | bigint | 服务人员 ID |
| tag_name | varchar(64) | 标签名称 |

### 2.5 服务人员证书表 `staff_certificate`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| staff_id | bigint | 服务人员 ID |
| certificate_name | varchar(128) | 证书名称 |
| file_url | varchar(512) | 证书图片 |
| sort_no | int | 排序 |

### 2.6 服务人员照片表 `staff_photo`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| staff_id | bigint | 服务人员 ID |
| photo_url | varchar(512) | 图片地址 |
| sort_no | int | 排序 |

### 2.7 工作经历表 `staff_work_experience`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| staff_id | bigint | 服务人员 ID |
| start_date | date | 开始日期 |
| end_date | date | 结束日期 |
| description | text | 经历描述 |

### 2.8 用户服务人员收藏表 `user_staff_favorite`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| user_id | bigint | 用户 ID |
| staff_id | bigint | 服务人员 ID |
| created_at | datetime | 创建时间 |
| updated_at | datetime | 更新时间 |
| deleted | tinyint | 逻辑删除 |

### 2.9 用户需求表 `user_demand`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| user_id | bigint | 用户 ID |
| title | varchar(128) | 标题 |
| category_id | bigint | 服务类型 |
| maternity_period | varchar(32) | 月嫂周期，可空 |
| contact_name | varchar(64) | 联系人 |
| contact_phone | varchar(32) | 联系电话 |
| gender | varchar(16) | 用户性别 |
| live_in | tinyint | 是否住家 |
| expected_salary | varchar(64) | 薪资待遇 |
| city | varchar(64) | 城市 |
| district | varchar(64) | 区县 |
| address | varchar(255) | 详细地址 |
| remark | text | 补充说明 |
| audit_status | varchar(32) | 审核状态 |
| follow_status | varchar(32) | 后台跟进状态 |

需求状态：

- PENDING：审核中。
- APPROVED：已通过。
- REJECTED：已拒绝。
- CANCELED：已取消。

跟进状态：

- TO_FOLLOW：待跟进。
- CONTACTED：已联系。
- MATCHED：已匹配。
- SIGNED：已签约。
- CLOSED：已关闭。

### 2.10 推荐阿姨表 `demand_recommendation`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| demand_id | bigint | 需求 ID |
| staff_id | bigint | 服务人员 ID |
| reason | varchar(255) | 推荐理由 |
| status | varchar(32) | 推荐状态 |
| sort_no | int | 排序 |

推荐状态：

- RECOMMENDED：已推荐。
- VIEWED：用户已查看。
- INTERVIEWED：已预约面试。
- IGNORED：已忽略。

### 2.11 预约面试表 `interview_appointment`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| user_id | bigint | 用户 ID |
| staff_id | bigint | 服务人员 ID |
| demand_id | bigint | 需求 ID，可空 |
| contact_name | varchar(64) | 联系人 |
| contact_phone | varchar(32) | 联系电话 |
| status | varchar(32) | 状态 |
| admin_note | text | 后台备注 |

状态：

- PENDING：待处理。
- CONTACTED：已联系。
- ARRANGED：已安排。
- COMPLETED：已完成。
- CANCELED：已取消。

### 2.12 合同表 `contract`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| user_id | bigint | 用户 ID |
| staff_id | bigint | 服务人员 ID |
| demand_id | bigint | 需求 ID，可空 |
| service_order_id | bigint | 服务订单 ID，可空 |
| contract_no | varchar(64) | 合同编号 |
| title | varchar(128) | 合同标题 |
| file_url | varchar(512) | 合同文件 |
| status | varchar(32) | SIGNED, TERMINATED |
| signed_at | datetime | 签署时间 |
| terminated_at | datetime | 终止时间 |

### 2.13 服务订单表 `service_order`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| order_no | varchar(64) | 订单号 |
| user_id | bigint | 用户 ID |
| staff_id | bigint | 服务人员 ID |
| demand_id | bigint | 需求 ID，可空 |
| category_id | bigint | 服务类型 |
| amount | decimal(10,2) | 金额 |
| start_date | date | 服务开始日期 |
| end_date | date | 服务结束日期 |
| status | varchar(32) | 状态 |
| admin_note | text | 后台备注 |

状态：

- WAIT_START：待开始。
- SERVING：服务中。
- COMPLETED：已完成。
- CANCELED：已取消。

### 2.14 服务订单评价表 `service_order_review`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| service_order_id | bigint | 服务订单 ID |
| user_id | bigint | 用户 ID |
| staff_id | bigint | 服务人员 ID |
| rating | int | 评分，1-5 |
| content | varchar(512) | 评价内容 |

约束：

- 同一服务订单只能有一条有效评价。
- 只有服务订单状态为 `COMPLETED` 时允许创建评价。

### 2.15 团购商品表 `group_product`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| title | varchar(128) | 商品名称 |
| cover_url | varchar(512) | 封面图 |
| original_price | decimal(10,2) | 原价 |
| single_price | decimal(10,2) | 单独购买价 |
| group_price | decimal(10,2) | 拼团价 |
| group_size | int | 成团人数 |
| valid_days | int | 购买后有效天数 |
| sold_count | int | 已售数量 |
| notice | text | 消费须知 |
| guarantee | text | 服务保障 |
| description | text | 产品介绍 |
| status | varchar(32) | DRAFT, ONLINE, OFFLINE |

### 2.16 拼团表 `group_team`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| product_id | bigint | 商品 ID |
| leader_user_id | bigint | 团长用户 ID |
| group_size | int | 成团人数 |
| joined_count | int | 已加入人数 |
| expire_at | datetime | 过期时间 |
| status | varchar(32) | 状态 |

状态：

- GROUPING：拼团中。
- SUCCESS：拼团成功。
- FAILED：拼团失败。
- CANCELED：已取消。

### 2.17 团购订单表 `group_order`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| order_no | varchar(64) | 订单号 |
| user_id | bigint | 用户 ID |
| product_id | bigint | 商品 ID |
| group_team_id | bigint | 拼团 ID，可空 |
| buy_type | varchar(32) | SINGLE, GROUP |
| quantity | int | 数量 |
| amount | decimal(10,2) | 实付金额 |
| status | varchar(32) | 状态 |
| paid_at | datetime | 模拟支付时间 |
| valid_until | datetime | 有效期截止 |

状态：

- WAIT_SHARE：待分享。
- WAIT_USE：待使用。
- USED：已使用。
- EXPIRED：到期。
- AFTER_SALE：售后。

### 2.18 团购订单评价表 `group_order_review`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| group_order_id | bigint | 团购订单 ID |
| user_id | bigint | 用户 ID |
| product_id | bigint | 团购商品 ID |
| rating | int | 评分，1-5 |
| content | varchar(512) | 评价内容 |

约束：

- 同一团购订单只能有一条有效评价。
- 只有团购订单状态为 `USED` 时允许创建评价。

### 2.19 内容配置表 `content_config`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| content_type | varchar(64) | BANNER, FAQ, AGREEMENT, PRIVACY, ABOUT |
| title | varchar(128) | 标题 |
| image_url | varchar(512) | 图片 |
| content | text | 内容 |
| sort_no | int | 排序 |
| enabled | tinyint | 是否启用 |

### 2.20 文件表 `file_asset`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| original_name | varchar(255) | 原始文件名 |
| object_key | varchar(255) | OSS object key |
| url | varchar(512) | 访问地址 |
| content_type | varchar(128) | 文件类型 |
| size_bytes | bigint | 文件大小 |
| usage_type | varchar(64) | AVATAR, CERTIFICATE, CONTRACT, PRODUCT, BANNER |

### 2.21 系统消息表 `system_message`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| user_id | bigint | 用户 ID |
| title | varchar(128) | 标题 |
| content | varchar(512) | 内容 |
| message_type | varchar(64) | 消息类型 |
| read_flag | tinyint | 是否已读 |

## 3. 索引建议

- `app_user.openid` 唯一索引。
- `service_staff.category_id` 普通索引。
- `service_staff.status` 普通索引。
- `user_demand.user_id` 普通索引。
- `user_demand.audit_status` 普通索引。
- `demand_recommendation.demand_id` 普通索引。
- `interview_appointment.user_id` 普通索引。
- `contract.user_id` 普通索引。
- `service_order.user_id` 普通索引。
- `service_order_review.service_order_id` 唯一索引。
- `service_order_review.staff_id` 普通索引。
- `group_order.user_id` 普通索引。
- `group_order_review.group_order_id` 唯一索引。
- `group_order_review.product_id` 普通索引。
- `group_team.product_id` 普通索引。
