# 项目准备文档

## 1. 项目目标

建设一个家政、月嫂、育婴、养老、保洁等本地生活服务平台。系统参考“金职到家”的产品结构，形成自己的原创小程序和后台系统。

第一版目标不是做全量商业系统，而是跑通核心闭环：

用户浏览服务人员或团购商品 -> 发布需求或购买团购 -> 管理员后台处理 -> 用户查看推荐阿姨、订单、合同和状态。

## 2. 项目组成

项目分为四个部分：

- uni-app 微信小程序用户端：用户浏览服务、发布需求、预约面试、查看推荐阿姨、购买团购、查看订单和合同。
- RuoYi-Vue-Plus Java 后端服务：提供 REST API、业务流程、数据持久化、文件上传、模拟支付。
- RuoYi-Vue-Plus 配套 Vue 管理后台：管理员维护人员、需求、预约、合同、团购、订单、内容配置。
- MySQL 数据库：保存用户、服务人员、需求、合同、订单、拼团等业务数据。

## 3. 需要安装的软件

开发必装：

- 微信开发者工具：开发和预览小程序。
- JDK 17 或 JDK 21：运行 Spring Boot 后端。
- Maven 3.9+：构建 Java 项目。
- MySQL 8.x：业务数据库。
- IntelliJ IDEA：Java 后端开发。
- Node.js LTS：运行 Vue 管理后台。
- DBeaver 或 Navicat：查看和维护数据库。
- Apifox 或 Postman：接口调试。

后期上线准备：

- 阿里云 OSS：存储头像、证书、服务照片、合同文件。
- HTTPS 域名：小程序上线必须使用合法 HTTPS 域名。
- 微信小程序账号：配置 AppID、合法域名、隐私协议。
- 微信商户号：第二版接入真实微信支付时再准备。

## 4. 推荐技术版本

- 微信小程序：uni-app，构建目标为微信小程序 `mp-weixin`。
- 后端：RuoYi-Vue-Plus，基于 Spring Boot 3.5.x。
- ORM：MyBatis-Plus。
- 数据库：MySQL 8.x。
- 管理后台：RuoYi-Vue-Plus 配套 `plus-ui`，基于 Vue 3 + Vite + Element Plus。
- 文件存储：阿里云 OSS Java SDK。
- 接口文档：Knife4j 或 Swagger/OpenAPI。

## 5. 目录规划

建议后续项目结构：

```text
D:\work\miniprogram
├─ docs                     # 项目文档
├─ miniapp                  # uni-app 小程序用户端
├─ ruoyi                    # RuoYi-Vue-Plus 后端
├─ ruoyi-ui                 # RuoYi-Vue-Plus 管理后台
├─ sql                      # 后续业务数据库脚本
└─ deploy                   # 部署配置
```

当前本地端口：

- 后端：`http://localhost:8081/`。
- 管理后台：`http://localhost:8082/`。
- 小程序构建产物：`miniapp/dist/build/mp-weixin`。

## 6. 开发原则

- 第一版先跑通主流程，不做复杂营销、分账、真实支付和电子签。
- 后台先行，因为小程序展示的数据主要来自后台录入。
- 图片、证书、合同文件统一通过后端上传到 OSS，小程序和后台不直接暴露 OSS 密钥。
- 订单、需求、拼团、合同都必须有状态日志，方便后台追踪。
- 用户端功能保持简单，复杂跟进工作放在后台管理。

## 7. 开发前需确认的信息

- 平台正式名称、Logo、客服电话、公司介绍。
- 阿里云 OSS bucket、region、自定义域名。
- 微信小程序 AppID。
- 生产环境域名。
- 是否需要短信验证码。
- 是否需要多城市运营。
