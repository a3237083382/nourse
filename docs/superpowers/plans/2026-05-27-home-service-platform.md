# Home Service Platform Implementation Plan

> **Status note:** This file is an archived implementation outline. Do not use it as a task board. All current task status is maintained only in `docs/08-development-plan.md`.

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

- Done: Create RuoYi-Vue-Plus backend project with Web, Validation, MySQL driver, MyBatis-Plus, Redis, and admin dependencies.
- Done: Use RuoYi built-in `/auth/code` and `/auth/tenant/list` endpoints as bootstrap verification endpoints.
- Done: Run `mvn -DskipTests package`.
- Done: Run backend locally on `http://localhost:8081/` and verify bootstrap APIs.

## Task 2: Database Schema

**Files:**

- Create: `sql/001_schema.sql`
- Create: `sql/002_seed_categories.sql`

- Done: Create tables from `docs/05-database-design.md`.
- Done: Seed service categories: 月嫂、保姆、育婴师、居家养老、保洁师、钟点工、成长陪伴师、家电清洗师.
- Done: Apply scripts to local MySQL.
- Done: Verify all tables exist.

## Task 3: Admin Authentication

**Files:**

- Use RuoYi-Vue-Plus built-in auth module and system user tables.
- Add project business admin configuration only when it differs from RuoYi defaults.

- Done: Add administrator login by username and password.
- Done: Return token after successful login.
- Done: Protect `/api/admin/**` routes.
- Done: Verify unauthorized admin request returns 401.
- Done: Verify authenticated admin request can access protected admin API.

## Task 4: Staff and Category Management

**Files:**

- Create backend modules `servicecategory` and `staff`.
- Create admin pages under `ruoyi-ui/src/views/staff/`.

- Done: Implement category CRUD APIs.
- Done: Implement staff CRUD APIs including tags, photos, certificates, and work experiences.
- Done: Implement admin staff list, create, edit, detail pages.
- Done: Verify staff created in admin appears in app API.

## Task 5: Mini Program Home and Staff Search

**Files:**

- Create `miniapp/src/pages.json`, `miniapp/src/App.vue`, `miniapp/src/main.js`.
- Create pages: home, contract, staff, message, mine.
- Create `miniapp/services/request.js`.

- Done: Configure five tab pages: 首页、合同、找阿姨、消息、我的.
- Done: Implement home page categories and group product preview.
- Done: Implement staff list filters by category, age, education, salary, region, keyword.
- Done: Implement staff detail with certificates, photos, work experience, favorite, share, interview button.

## Task 6: Demand Publishing and Recommendations

**Files:**

- Create backend modules `demand` and `recommendation`.
- Create miniapp pages demand-publish, demand-list, demand-detail.
- Create admin demand pages.

- Done: User submits demand with confirmed fields from `docs/02-requirements.md`.
- Done: Admin reviews demand as approved or rejected.
- Done: Admin recommends one or more staff to an approved demand.
- Done: User demand detail shows recommended staff.

## Task 7: Interview Appointments

**Files:**

- Create backend module `interview`.
- Create admin interview page.
- Extend miniapp staff-detail and mine pages.

- Done: User submits interview appointment from staff detail.
- Done: Admin views and updates interview status.
- Done: User sees appointment list under 我的邀约.

## Task 8: Contracts and Service Orders

**Files:**

- Create backend modules `contract` and `serviceorder`.
- Create admin contract and service order pages.
- Create miniapp contract and service order pages.

- Planned in `docs/08-development-plan.md`: Admin uploads contract file and creates contract.
- Planned in `docs/08-development-plan.md`: User sees contracts under 合同 tab.
- Planned in `docs/08-development-plan.md`: Admin creates service order.
- Planned in `docs/08-development-plan.md`: User sees service orders under 我的.

## Task 9: Group Buying

**Files:**

- Create backend modules `groupbuy` and `grouporder`.
- Create admin group product, team, and order pages.
- Create miniapp group list, group detail, and group order pages.

- Planned in `docs/08-development-plan.md`: Admin creates group product with single price, group price, group size, valid days, and content.
- Planned in `docs/08-development-plan.md`: User buys alone and receives WAIT_USE order.
- Planned in `docs/08-development-plan.md`: User starts group and receives WAIT_SHARE order.
- Planned in `docs/08-development-plan.md`: Product detail shows active teams.
- Planned in `docs/08-development-plan.md`: Another user joins active team.
- Planned in `docs/08-development-plan.md`: When joined count reaches group size, team becomes SUCCESS and orders become WAIT_USE.

## Task 10: Content, Message, and Final Verification

**Files:**

- Create backend modules `content` and `message`.
- Create admin content pages.
- Create miniapp message and content pages.

- Planned in `docs/08-development-plan.md`: Admin edits banners, FAQ, agreement, privacy, about us, sign success tips.
- Planned in `docs/08-development-plan.md`: App home and mine pages display content.
- Planned in `docs/08-development-plan.md`: Demand approval, staff recommendation, contract creation, and group success create system messages.
- Planned in `docs/08-development-plan.md`: Run backend tests.
- Planned in `docs/08-development-plan.md`: Run admin build.
- Planned in `docs/08-development-plan.md`: Open mini program in WeChat Developer Tools and verify main flows.

## Handoff

Implementation should proceed in task order. Do not add real WeChat Pay, electronic signature, service-staff login, or multi-admin role permissions in the first version.
