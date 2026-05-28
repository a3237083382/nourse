# 项目架构设计

## 1. 总体架构

```text
uni-app 微信小程序
   |
   | HTTPS REST API
   v
RuoYi-Vue-Plus 后端
   |
   | MyBatis-Plus
   v
MySQL 8
   |
   | 文件元数据
   v
阿里云 OSS

RuoYi-Vue-Plus 管理后台
   |
   | HTTPS REST API
   v
RuoYi-Vue-Plus 后端
```

## 2. 技术选型

### 2.1 小程序端

使用 uni-app，当前工程为 `miniapp/`，构建目标为微信小程序 `mp-weixin`。

原因：

- 目标运行环境是微信小程序，但 uni-app 可以用 Vue 语法快速组织页面。
- 当前已经通过 `npm run build:mp-weixin` 产出 `dist/build/mp-weixin`。
- 第一版仍只发布微信小程序，不做多端复杂适配。

### 2.2 后端

使用 RuoYi-Vue-Plus，当前工程为 `ruoyi/`，底层为 Spring Boot 3.5.x。

原因：

- 内置登录认证、统一响应、权限、MyBatis-Plus、OSS、接口文档等后台基础能力。
- 第一版业务模块在若依框架基础上新增，避免重复造后台基础设施。
- 本地开发端口使用 `8081`，因为本机 `8080` 已被其他 Node 服务占用。

### 2.3 数据访问

使用 MyBatis-Plus。

原因：

- 管理后台 CRUD 多，MyBatis-Plus 可以减少重复代码。
- 支持分页插件、条件构造器、Mapper 通用能力。
- 适合 MySQL 表结构清晰的业务系统。

### 2.4 管理后台

使用 RuoYi-Vue-Plus 配套 `plus-ui`，当前工程为 `ruoyi-ui/`，底层为 Vue 3 + Vite + Element Plus。

原因：

- 与后端接口加密、登录、租户、菜单等能力匹配。
- Element Plus 提供表格、表单、弹窗、分页、上传、布局等后台常用组件。
- 本地开发端口使用 `8082`，代理 `/dev-api` 到 `http://localhost:8081`。

### 2.5 文件存储

使用阿里云 OSS。

用途：

- 服务人员头像。
- 服务人员证书。
- 服务人员照片。
- 团购商品图片。
- 首页轮播图。
- 合同 PDF/图片。

后端负责上传，前端只拿文件 URL，不保存 OSS 密钥。

## 3. 后端模块划分

后端当前采用 RuoYi-Vue-Plus 目录结构。后续业务模块优先放在若依既有模块体系中，避免破坏框架边界：

```text
ruoyi
├─ ruoyi-admin
├─ ruoyi-common
├─ ruoyi-modules
│  ├─ ruoyi-system
│  ├─ ruoyi-generator
│  ├─ ruoyi-job
│  └─ ruoyi-workflow
└─ script/sql
```

模块职责：

- `ruoyi-admin`：应用启动、配置、入口接口。
- `ruoyi-common`：框架公共能力。
- `ruoyi-modules`：系统模块和后续业务模块承载位置。
- 后续新增家政业务模块时，命名要和业务文档一致：服务分类、服务人员、需求、推荐、预约、合同、订单、团购、消息。

## 4. 小程序目录建议

```text
miniapp
├─ src
│  ├─ App.vue
│  ├─ main.js
│  ├─ manifest.json
│  ├─ pages.json
│  ├─ pages
│  │  ├─ home
│  │  ├─ contract
│  │  ├─ staff
│  │  ├─ message
│  │  └─ mine
│  └─ static
└─ dist/build/mp-weixin
```

后续页面规划：

```text
miniapp/src
├─ pages
│  ├─ home
│  ├─ contract
│  ├─ staff
│  ├─ staff-detail
│  ├─ demand-publish
│  ├─ demand-list
│  ├─ demand-detail
│  ├─ group-list
│  ├─ group-detail
│  ├─ group-order
│  ├─ message
│  └─ mine
├─ components
├─ services
└─ utils
```

底部 Tab：

- `pages/home/index`
- `pages/contract/index`
- `pages/staff/index`
- `pages/message/index`
- `pages/mine/index`

## 5. 管理后台目录建议

```text
ruoyi-ui
├─ src
│  ├─ api
│  ├─ router
│  ├─ stores
│  ├─ layouts
│  ├─ views
│  │  ├─ login
│  │  ├─ dashboard
│  │  ├─ users
│  │  ├─ staff
│  │  ├─ demands
│  │  ├─ interviews
│  │  ├─ contracts
│  │  ├─ service-orders
│  │  ├─ group-products
│  │  ├─ group-teams
│  │  ├─ group-orders
│  │  └─ content
│  └─ utils
└─ package.json
```

## 6. 接口规范

接口前缀：

- 小程序端：`/api/app/**`
- 管理后台：`/api/admin/**`
- 公共文件：`/api/file/**`

统一响应：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

分页响应：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "records": [],
    "total": 0,
    "page": 1,
    "size": 10
  }
}
```

## 7. 登录与安全

小程序用户：

- 使用微信登录获取登录凭证。
- 后端换取 openid。
- 用户首次登录创建用户记录。
- 绑定手机号后完善资料。

后台管理员：

- 第一版一个管理员角色。
- 使用账号密码登录。
- 后端返回 token。
- 管理后台请求携带 token。

安全要求：

- OSS 密钥只放后端环境变量。
- 管理接口必须鉴权。
- 小程序用户只能访问自己的需求、合同和订单。
- 文件上传限制类型和大小。

## 8. 部署架构

开发环境：

```text
微信开发者工具 -> 本地后端 localhost:8081 -> 本地 MySQL
管理后台 localhost:8082 -> 本地后端 localhost:8081
```

生产环境：

```text
微信小程序 -> HTTPS API 域名 -> 后端服务 -> MySQL
管理后台 -> HTTPS API 域名 -> 后端服务 -> MySQL
后端服务 -> 阿里云 OSS
```
