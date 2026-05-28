# 到家服务小程序项目文档目录

本文档集用于指导一个参考“金职到家”结构和业务逻辑的原创家政服务平台开发。项目包含微信小程序用户端、Java 后端、Vue 管理后台和 MySQL 数据库。

## 文档清单

- [01-project-preparation.md](01-project-preparation.md)：项目准备、软件安装、账号准备、环境约定。
- [00-project-requirements-overview.md](00-project-requirements-overview.md)：项目总需求说明书和总 TODO 清单。
- [02-requirements.md](02-requirements.md)：用户端、后台端、业务角色、功能范围。
- [03-business-flows.md](03-business-flows.md)：找阿姨、发布需求、预约面试、合同、团购业务流程。
- [04-architecture.md](04-architecture.md)：整体项目架构、模块边界、技术选型。
- [05-database-design.md](05-database-design.md)：核心数据表、字段建议、状态枚举。
- [06-api-design.md](06-api-design.md)：小程序接口、后台接口、文件上传接口。
- [07-admin-requirements.md](07-admin-requirements.md)：管理后台菜单、页面、操作规则。
- [08-development-plan.md](08-development-plan.md)：阶段计划、开发顺序、验收标准。
- [09-prototype-design-work-plan.md](09-prototype-design-work-plan.md)：原型设计阶段的目的、范围、产出物、注意事项和 Todo。
- [10-database-business-table-design-work-plan.md](10-database-business-table-design-work-plan.md)：数据库业务表设计阶段的表结构、SQL、验证规划。
- [11-admin-module-development-work-plan.md](11-admin-module-development-work-plan.md)：管理后台业务模块开发阶段的开发顺序、规则和验收标准。
- [12-miniapp-page-development-work-plan.md](12-miniapp-page-development-work-plan.md)：小程序页面开发阶段的页面范围、接口接入和微信端注意事项。
- [13-integration-test-work-plan.md](13-integration-test-work-plan.md)：联调测试阶段的测试范围、关键链路和验收规则。
- [superpowers/plans/2026-05-27-home-service-platform.md](superpowers/plans/2026-05-27-home-service-platform.md)：可执行开发计划。

## 推荐执行顺序

```text
原型设计
-> 数据库业务表设计
-> 后台业务模块开发
-> 小程序页面开发
-> 联调测试
```

## 当前已确认范围

- 参考目标小程序的结构和业务逻辑，不复制对方品牌、素材、代码或私有接口。
- 第一版包含找阿姨、发布需求、团购、合同、订单、客服、我的页面。
- 服务人员由平台后台录入，不做服务人员独立登录端。
- 管理后台第一版只有一个管理员角色。
- 第一版不接真实微信支付，使用模拟支付跑通流程。
- 团购支持单独购买和拼团购买，商品详情页展示正在拼团的人。
- 合同由后台上传文件，用户端查看合同状态和文件。
- 图片、证书、合同文件后续存储到阿里云 OSS。
