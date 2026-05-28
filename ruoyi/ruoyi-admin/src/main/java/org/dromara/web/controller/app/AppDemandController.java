package org.dromara.web.controller.app;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/app/demands")
public class AppDemandController {

    private final JdbcTemplate jdbcTemplate;

    @PostMapping
    public R<Long> create(@RequestBody DemandRequest request) {
        Long userId = LoginHelper.getUserId();
        if (request.title() == null || request.title().isBlank()
            || request.categoryId() == null
            || request.contactName() == null || request.contactName().isBlank()
            || request.contactPhone() == null || request.contactPhone().isBlank()
            || request.address() == null || request.address().isBlank()) {
            return R.fail("请完整填写标题、服务类型、联系人、电话和详细地址");
        }
        jdbcTemplate.update("""
            insert into user_demand(user_id, title, category_id, maternity_period, contact_name, contact_phone,
                gender, live_in, expected_salary, city, district, address, remark, audit_status, follow_status,
                created_at, updated_at, deleted)
            values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'PENDING', 'TO_FOLLOW', now(), now(), 0)
            """, userId, request.title().trim(), request.categoryId(), request.maternityPeriod(),
            request.contactName().trim(), request.contactPhone().trim(), request.gender(), request.liveInValue(),
            request.expectedSalary(), request.city(), request.district(), request.address().trim(), request.remark());
        Long id = jdbcTemplate.queryForObject("select last_insert_id()", Long.class);
        return R.ok(id);
    }

    @GetMapping
    public TableDataInfo<Map<String, Object>> list(
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "10") int pageSize
    ) {
        Long userId = LoginHelper.getUserId();
        Long total = jdbcTemplate.queryForObject("""
            select count(1) from user_demand
            where user_id = ? and deleted = 0
            """, Long.class, userId);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
            select d.id, d.title, d.category_id categoryId, c.name categoryName,
                   d.city, d.district, d.audit_status auditStatus, d.follow_status followStatus,
                   d.created_at createdAt, d.updated_at updatedAt,
                   (select count(1) from demand_recommendation r where r.demand_id = d.id and r.deleted = 0) recommendationCount
            from user_demand d
            left join service_category c on c.id = d.category_id
            where d.user_id = ? and d.deleted = 0
            order by d.id desc
            limit ? offset ?
            """, userId, Math.max(pageSize, 1), Math.max(pageNum - 1, 0) * Math.max(pageSize, 1));
        return new TableDataInfo<>(rows, total == null ? 0 : total);
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        Long userId = LoginHelper.getUserId();
        Map<String, Object> demand = jdbcTemplate.queryForMap("""
            select d.id, d.title, d.category_id categoryId, c.name categoryName, d.maternity_period maternityPeriod,
                   d.contact_name contactName, d.contact_phone contactPhone, d.gender, d.live_in liveIn,
                   d.expected_salary expectedSalary, d.city, d.district, d.address, d.remark,
                   d.audit_status auditStatus, d.follow_status followStatus, d.created_at createdAt, d.updated_at updatedAt
            from user_demand d
            left join service_category c on c.id = d.category_id
            where d.id = ? and d.user_id = ? and d.deleted = 0
            """, id, userId);
        demand.put("recommendations", recommendations(id));
        return R.ok(demand);
    }

    @PostMapping("/{id}/cancel")
    public R<Void> cancel(@PathVariable Long id) {
        Long userId = LoginHelper.getUserId();
        int updated = jdbcTemplate.update("""
            update user_demand
            set audit_status = 'CANCELED', follow_status = 'CLOSED', updated_at = now()
            where id = ? and user_id = ? and deleted = 0 and audit_status = 'PENDING'
            """, id, userId);
        return updated > 0 ? R.ok() : R.fail("只有审核中的需求可以取消");
    }

    @GetMapping("/{id}/recommendations")
    public R<List<Map<String, Object>>> recommendationList(@PathVariable Long id) {
        Long userId = LoginHelper.getUserId();
        Long count = jdbcTemplate.queryForObject(
            "select count(1) from user_demand where id = ? and user_id = ? and deleted = 0",
            Long.class,
            id,
            userId
        );
        if (count == null || count == 0) {
            return R.fail("需求不存在");
        }
        jdbcTemplate.update("""
            update demand_recommendation
            set status = 'VIEWED', updated_at = now()
            where demand_id = ? and status = 'RECOMMENDED' and deleted = 0
            """, id);
        return R.ok(recommendations(id));
    }

    private List<Map<String, Object>> recommendations(Long demandId) {
        return jdbcTemplate.queryForList("""
            select r.id, r.demand_id demandId, r.staff_id staffId, r.reason, r.status, r.sort_no sortNo,
                   s.name staffName, s.avatar_url avatarUrl, s.city, s.district, s.age, s.education,
                   s.experience_years experienceYears, s.salary_min salaryMin, s.salary_max salaryMax,
                   s.salary_unit salaryUnit, c.name categoryName
            from demand_recommendation r
            inner join service_staff s on s.id = r.staff_id
            left join service_category c on c.id = s.category_id
            where r.demand_id = ? and r.deleted = 0 and s.deleted = 0 and s.status = 'ONLINE'
            order by r.sort_no asc, r.id desc
            """, demandId);
    }

    public record DemandRequest(
        String title,
        Long categoryId,
        String maternityPeriod,
        String contactName,
        String contactPhone,
        String gender,
        Boolean liveIn,
        String expectedSalary,
        String city,
        String district,
        String address,
        String remark
    ) {
        int liveInValue() {
            return liveIn != null && liveIn ? 1 : 0;
        }
    }
}
