# Home Service Platform Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a WeChat mini program home-care service platform with a RuoYi-Vue-Plus backend, MySQL database, Vue admin panel, staff search, demand publishing, staff recommendations, interviews, contracts, group buying, and simulated payment.

**Architecture:** The system has three applications: uni-app WeChat Mini Program, RuoYi-Vue-Plus REST API, and RuoYi-Vue-Plus `plus-ui` admin web. The backend owns all business rules and database writes; the mini program and admin web call backend APIs.

**Tech Stack:** uni-app, Java 17, RuoYi-Vue-Plus 5.6.1, Spring Boot 3.5.x, MyBatis-Plus, MySQL 8, Redis, Vue 3, Vite, Element Plus, Alibaba Cloud OSS.

---

## File Structure

Create these top-level directories:

- `ruoyi/`: RuoYi-Vue-Plus backend.
- `ruoyi-ui/`: RuoYi-Vue-Plus admin frontend.
- `miniapp/`: uni-app WeChat Mini Program.
- `sql/`: database migration and seed scripts.
- `docs/`: product, architecture, and development documents.

## Task 1: Backend Bootstrap

**Files:**

- Use: `ruoyi/pom.xml`
- Use: `ruoyi/ruoyi-admin/src/main/resources/application.yml`
- Use: `ruoyi/ruoyi-admin/src/main/resources/application-dev.yml`
- Use: `ruoyi/ruoyi-admin/target/ruoyi-admin.jar`

- [x] Create RuoYi-Vue-Plus backend project with Web, Validation, MySQL driver, MyBatis-Plus, Redis, and admin dependencies.
- [x] Use RuoYi built-in `/auth/code` and `/auth/tenant/list` endpoints as bootstrap verification endpoints.
- [x] Run `mvn -DskipTests package`.
- [x] Run backend locally on `http://localhost:8081/` and verify bootstrap APIs.

## Task 2: Database Schema

**Files:**

- Create: `sql/001_schema.sql`
- Create: `sql/002_seed_categories.sql`

- [ ] Create tables from `docs/05-database-design.md`.
- [ ] Seed service categories: 月嫂、保姆、育婴师、居家养老、保洁师、钟点工、成长陪伴师、家电清洗师.
- [ ] Apply scripts to local MySQL.
- [ ] Verify all tables exist.

## Task 3: Admin Authentication

**Files:**

- Use RuoYi-Vue-Plus built-in auth module and system user tables.
- Add project business admin configuration only when it differs from RuoYi defaults.

- [x] Add administrator login by username and password.
- [x] Return token after successful login.
- [ ] Protect `/api/admin/**` routes.
- [ ] Verify unauthorized admin request returns 401.

## Task 4: Staff and Category Management

**Files:**

- Create backend modules `servicecategory` and `staff`.
- Create admin pages under `ruoyi-ui/src/views/staff/`.

- [ ] Implement category CRUD APIs.
- [ ] Implement staff CRUD APIs including tags, photos, certificates, and work experiences.
- [ ] Implement admin staff list, create, edit, detail pages.
- [ ] Verify staff created in admin appears in app API.

## Task 5: Mini Program Home and Staff Search

**Files:**

- Create `miniapp/src/pages.json`, `miniapp/src/App.vue`, `miniapp/src/main.js`.
- Create pages: home, contract, staff, message, mine.
- Create `miniapp/services/request.js`.

- [x] Configure five tab pages: 首页、合同、找阿姨、消息、我的.
- [ ] Implement home page categories and group product preview.
- [ ] Implement staff list filters by category, age, education, salary, region, keyword.
- [ ] Implement staff detail with certificates, photos, work experience, favorite, share, interview button.

## Task 6: Demand Publishing and Recommendations

**Files:**

- Create backend modules `demand` and `recommendation`.
- Create miniapp pages demand-publish, demand-list, demand-detail.
- Create admin demand pages.

- [ ] User submits demand with confirmed fields from `docs/02-requirements.md`.
- [ ] Admin reviews demand as approved or rejected.
- [ ] Admin recommends one or more staff to an approved demand.
- [ ] User demand detail shows recommended staff.

## Task 7: Interview Appointments

**Files:**

- Create backend module `interview`.
- Create admin interview page.
- Extend miniapp staff-detail and mine pages.

- [ ] User submits interview appointment from staff detail.
- [ ] Admin views and updates interview status.
- [ ] User sees appointment list under 我的邀约.

## Task 8: Contracts and Service Orders

**Files:**

- Create backend modules `contract` and `serviceorder`.
- Create admin contract and service order pages.
- Create miniapp contract and service order pages.

- [ ] Admin uploads contract file and creates contract.
- [ ] User sees contracts under 合同 tab.
- [ ] Admin creates service order.
- [ ] User sees service orders under 我的.

## Task 9: Group Buying

**Files:**

- Create backend modules `groupbuy` and `grouporder`.
- Create admin group product, team, and order pages.
- Create miniapp group list, group detail, and group order pages.

- [ ] Admin creates group product with single price, group price, group size, valid days, and content.
- [ ] User buys alone and receives WAIT_USE order.
- [ ] User starts group and receives WAIT_SHARE order.
- [ ] Product detail shows active teams.
- [ ] Another user joins active team.
- [ ] When joined count reaches group size, team becomes SUCCESS and orders become WAIT_USE.

## Task 10: Content, Message, and Final Verification

**Files:**

- Create backend modules `content` and `message`.
- Create admin content pages.
- Create miniapp message and content pages.

- [ ] Admin edits banners, FAQ, agreement, privacy, about us, sign success tips.
- [ ] App home and mine pages display content.
- [ ] Demand approval, staff recommendation, contract creation, and group success create system messages.
- [ ] Run backend tests.
- [ ] Run admin build.
- [ ] Open mini program in WeChat Developer Tools and verify main flows.

## Handoff

Implementation should proceed in task order. Do not add real WeChat Pay, electronic signature, service-staff login, or multi-admin role permissions in the first version.
