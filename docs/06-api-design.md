# 接口设计草案

## 1. 通用规则

接口前缀：

- 小程序端：`/api/app`
- 管理后台：`/api/admin`
- 文件上传：`/api/file`

认证：

- 小程序接口使用用户 token。
- 后台接口使用管理员 token。
- 登录、公开内容、公开服务列表可不登录访问。

统一响应：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

## 2. 小程序接口

### 2.1 登录

- `POST /api/app/auth/wechat-login`：微信登录。
- `POST /api/app/auth/bind-phone`：绑定手机号。
- `GET /api/app/user/profile`：获取个人资料。
- `PUT /api/app/user/profile`：更新个人资料。

### 2.2 首页

- `GET /api/app/home`：首页聚合数据。
- `GET /api/app/banners`：轮播图列表。
- `GET /api/app/categories`：服务分类。
- `GET /api/app/sign-success-tips`：签约成功提示。

首页聚合数据包含：

- 自动轮播图。
- 签约成功提示轮播。
- 服务分类/职业入口。
- 首页团购活动列表，前端首页展示前 2 条，更多进入团购列表；每条团购返回 `soldCount`、`activeTeamExpireAt`、`activeTeamCount`，用于展示已售数量或进行中拼团倒计时。
- 服务人员可按 `categoryId` 分页加载，用于首页分类切换和上拉加载。

### 2.3 服务人员

- `GET /api/app/staff`：服务人员列表。

查询参数：

- `categoryId`
- `keyword`
- `ageRange`
- `education`
- `salaryRange`
- `city`
- `district`
- `page`
- `size`

- `GET /api/app/staff/{id}`：服务人员详情。
- `POST /api/app/staff/{id}/favorite`：收藏。
- `DELETE /api/app/staff/{id}/favorite`：取消收藏。
- `GET /api/app/favorites/staff`：我的收藏。

### 2.4 预约面试

- `POST /api/app/interviews`：提交预约面试。
- `GET /api/app/interviews`：我的邀约/预约列表。
- `GET /api/app/interviews/{id}`：预约详情。

### 2.5 需求

- `POST /api/app/demands`：发布需求。
- `GET /api/app/demands`：我的需求列表。
- `GET /api/app/demands/{id}`：我的需求详情。
- `POST /api/app/demands/{id}/cancel`：取消需求。
- `GET /api/app/demands/{id}/recommendations`：需求推荐阿姨列表。

### 2.6 合同

- `GET /api/app/contracts`：我的合同列表。
- `GET /api/app/contracts/{id}`：合同详情。

### 2.7 服务订单

- `GET /api/app/service-orders`：服务订单列表。
- `GET /api/app/service-orders/{id}`：服务订单详情。
- `POST /api/app/service-orders/{id}/review`：服务完成后提交评价。

服务订单评价请求体：

```json
{
  "rating": 5,
  "content": "服务认真，沟通顺畅"
}
```

### 2.8 团购商品

- `GET /api/app/group-products`：团购商品列表。
- `GET /api/app/group-products/{id}`：团购商品详情。
- `GET /api/app/group-products/{id}/active-teams`：正在拼团的人。

### 2.9 拼团和团购订单

- `POST /api/app/group-orders/single`：单独购买。
- `POST /api/app/group-orders/group/start`：发起拼团。
- `POST /api/app/group-orders/group/join`：加入拼团。
- `POST /api/app/group-orders/{id}/mock-pay`：模拟支付。
- `GET /api/app/group-orders`：团购订单列表。
- `GET /api/app/group-orders/{id}`：团购订单详情。
- `POST /api/app/group-orders/{id}/review`：团购服务使用完成后提交评价。

团购订单评价请求体：

```json
{
  "rating": 5,
  "content": "服务体验良好"
}
```

### 2.10 消息和内容

- `GET /api/app/messages`：系统消息。
- `POST /api/app/messages/{id}/read`：标记已读。
- `GET /api/app/content/faq`：常见问题。
- `GET /api/app/content/agreement`：用户协议。
- `GET /api/app/content/privacy`：隐私政策。
- `GET /api/app/content/about`：关于我们。

## 3. 管理后台接口

### 3.1 管理员认证

- `POST /api/admin/auth/login`：管理员登录。
- `POST /api/admin/auth/logout`：退出登录。
- `GET /api/admin/auth/me`：当前管理员信息。

### 3.2 用户管理

- `GET /api/admin/users`：用户列表。
- `GET /api/admin/users/{id}`：用户详情。
- `PUT /api/admin/users/{id}/status`：启用或禁用用户。

### 3.3 分类管理

- `GET /api/admin/categories`：分类列表。
- `POST /api/admin/categories`：新增分类。
- `PUT /api/admin/categories/{id}`：编辑分类。
- `PUT /api/admin/categories/{id}/status`：启用或禁用。

### 3.4 服务人员管理

- `GET /api/admin/staff`：服务人员列表。
- `POST /api/admin/staff`：新增服务人员。
- `GET /api/admin/staff/{id}`：服务人员详情。
- `PUT /api/admin/staff/{id}`：编辑服务人员。
- `PUT /api/admin/staff/{id}/status`：上架、下架、草稿。
- `POST /api/admin/staff/{id}/certificates`：添加证书。
- `POST /api/admin/staff/{id}/photos`：添加照片。
- `POST /api/admin/staff/{id}/experiences`：添加工作经历。

### 3.5 需求管理

- `GET /api/admin/demands`：需求列表。
- `GET /api/admin/demands/{id}`：需求详情。
- `POST /api/admin/demands/{id}/approve`：审核通过。
- `POST /api/admin/demands/{id}/reject`：审核拒绝。
- `PUT /api/admin/demands/{id}/follow-status`：修改跟进状态。

### 3.6 推荐阿姨管理

- `POST /api/admin/demands/{id}/recommendations`：给需求推荐阿姨。
- `GET /api/admin/demands/{id}/recommendations`：查看推荐列表。
- `DELETE /api/admin/recommendations/{id}`：删除推荐。

### 3.7 预约面试管理

- `GET /api/admin/interviews`：预约列表。
- `GET /api/admin/interviews/{id}`：预约详情。
- `PUT /api/admin/interviews/{id}/status`：修改预约状态。
- `PUT /api/admin/interviews/{id}/note`：修改后台备注。

### 3.8 合同管理

- `GET /api/admin/contracts`：合同列表。
- `POST /api/admin/contracts`：创建合同。
- `GET /api/admin/contracts/{id}`：合同详情。
- `PUT /api/admin/contracts/{id}`：编辑合同。
- `PUT /api/admin/contracts/{id}/status`：修改合同状态。

### 3.9 服务订单管理

- `GET /api/admin/service-orders`：服务订单列表。
- `POST /api/admin/service-orders`：创建服务订单。
- `GET /api/admin/service-orders/{id}`：服务订单详情。
- `PUT /api/admin/service-orders/{id}/status`：修改订单状态。

### 3.10 团购商品管理

- `GET /api/admin/group-products`：商品列表。
- `POST /api/admin/group-products`：新增商品。
- `GET /api/admin/group-products/{id}`：商品详情。
- `PUT /api/admin/group-products/{id}`：编辑商品。
- `PUT /api/admin/group-products/{id}/status`：上架或下架。

### 3.11 拼团和团购订单管理

- `GET /api/admin/group-teams`：拼团列表。
- `GET /api/admin/group-teams/{id}`：拼团详情。
- `PUT /api/admin/group-teams/{id}/status`：修改拼团状态。
- `GET /api/admin/group-orders`：团购订单列表。
- `GET /api/admin/group-orders/{id}`：团购订单详情。
- `PUT /api/admin/group-orders/{id}/status`：修改订单状态。

### 3.12 内容管理

- `GET /api/admin/content`：内容列表。
- `POST /api/admin/content`：新增内容。
- `PUT /api/admin/content/{id}`：编辑内容。
- `PUT /api/admin/content/{id}/status`：启用或禁用。

说明：

- 首页轮播图等带图片内容在管理端通过上传控件选择文件，不要求管理员手工输入图片地址。

### 3.13 文件上传

- `POST /api/file/upload`：上传文件到 OSS。
- `POST /resource/oss/upload`：RuoYi 管理端通用 OSS 上传入口，内容配置和合同上传控件复用。

上传类型：

- 头像。
- 证书。
- 服务照片。
- 团购商品图。
- 首页轮播图。
- 合同文件。

## 4. 重点接口验收

第一版必须跑通：

- 小程序登录。
- 后台登录。
- 后台创建服务人员，小程序列表和详情可见。
- 用户发布需求，后台可审核。
- 后台推荐阿姨，用户需求详情可见。
- 用户预约面试，后台可跟进。
- 后台上传合同，用户合同页可见。
- 后台创建团购商品，用户可单独购买和拼团购买。
- 用户加入别人拼团，满员后拼团成功。
