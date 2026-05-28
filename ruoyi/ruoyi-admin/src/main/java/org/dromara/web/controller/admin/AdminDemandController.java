package org.dromara.web.controller.admin;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/demands")
public class AdminDemandController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public TableDataInfo<Map<String, Object>> list(
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) String auditStatus,
        @RequestParam(required = false) String followStatus,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "10") int pageSize
    ) {
        List<Object> args = new ArrayList<>();
        String where = buildWhere(categoryId, auditStatus, followStatus, keyword, args);
        Long total = jdbcTemplate.queryForObject("select count(1) from user_demand d where " + where, Long.class, args.toArray());
        args.add(Math.max(pageSize, 1));
        args.add(Math.max(pageNum - 1, 0) * Math.max(pageSize, 1));
        String sql = """
            select d.id, d.user_id userId, u.nickname userNickname, c.name categoryName,
                   d.title, d.category_id categoryId, d.contact_name contactName,
                   d.contact_phone contactPhone, d.city, d.district, d.address,
                   d.audit_status auditStatus, d.follow_status followStatus,
                   d.created_at createdAt, d.updated_at updatedAt
            from user_demand d
            left join app_user u on u.id = d.user_id
            left join service_category c on c.id = d.category_id
            """ + " where " + where + " order by d.id desc limit ? offset ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, args.toArray());
        return new TableDataInfo<>(rows, total == null ? 0 : total);
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        Map<String, Object> demand = jdbcTemplate.queryForMap("""
            select d.*, u.nickname userNickname, c.name categoryName
            from user_demand d
            left join app_user u on u.id = d.user_id
            left join service_category c on c.id = d.category_id
            where d.id = ? and d.deleted = 0
            """, id);
        demand.put("recommendations", recommendations(id));
        return R.ok(demand);
    }

    @PostMapping("/{id}/approve")
    public R<Void> approve(@PathVariable Long id) {
        jdbcTemplate.update("""
            update user_demand
            set audit_status = 'APPROVED', follow_status = 'TO_FOLLOW', updated_at = now()
            where id = ? and deleted = 0 and audit_status = 'PENDING'
            """, id);
        createMessage(id, "需求审核通过", "你的需求已审核通过，平台将为你推荐合适的服务人员。", "DEMAND_APPROVED");
        return R.ok();
    }

    @PostMapping("/{id}/reject")
    public R<Void> reject(@PathVariable Long id, @RequestBody(required = false) RejectRequest request) {
        String reason = request == null || request.reason() == null || request.reason().isBlank()
            ? "你的需求暂未通过审核，请调整后重新提交。"
            : request.reason().trim();
        jdbcTemplate.update("""
            update user_demand
            set audit_status = 'REJECTED', follow_status = 'CLOSED', updated_at = now()
            where id = ? and deleted = 0 and audit_status = 'PENDING'
            """, id);
        createMessage(id, "需求审核未通过", reason, "DEMAND_REJECTED");
        return R.ok();
    }

    @PutMapping("/{id}/follow-status")
    public R<Void> updateFollowStatus(@PathVariable Long id, @RequestBody FollowStatusRequest request) {
        String followStatus = request == null || request.followStatus() == null || request.followStatus().isBlank()
            ? "TO_FOLLOW"
            : request.followStatus().trim();
        jdbcTemplate.update("""
            update user_demand
            set follow_status = ?, updated_at = now()
            where id = ? and deleted = 0
            """, followStatus, id);
        return R.ok();
    }

    @GetMapping("/{id}/recommendations")
    public R<List<Map<String, Object>>> recommendationList(@PathVariable Long id) {
        return R.ok(recommendations(id));
    }

    @PostMapping("/{id}/recommendations")
    public R<Long> addRecommendation(@PathVariable Long id, @RequestBody RecommendationRequest request) {
        if (request == null || request.staffId() == null) {
            return R.fail("请选择要推荐的服务人员");
        }
        String auditStatus = jdbcTemplate.queryForObject(
            "select audit_status from user_demand where id = ? and deleted = 0",
            String.class,
            id
        );
        if (!"APPROVED".equals(auditStatus)) {
            return R.fail("需求审核通过后才能推荐阿姨");
        }

        Long existingId = jdbcTemplate.query("""
            select id
            from demand_recommendation
            where demand_id = ? and staff_id = ?
            order by id desc
            limit 1
            """, rs -> rs.next() ? rs.getLong("id") : null, id, request.staffId());
        if (existingId == null) {
            jdbcTemplate.update("""
                insert into demand_recommendation(demand_id, staff_id, reason, status, sort_no, created_at, updated_at, deleted)
                values(?, ?, ?, 'RECOMMENDED', ?, now(), now(), 0)
                """, id, request.staffId(), request.reason(), request.sortNoValue());
            existingId = jdbcTemplate.queryForObject("select last_insert_id()", Long.class);
        } else {
            jdbcTemplate.update("""
                update demand_recommendation
                set reason = ?, status = 'RECOMMENDED', sort_no = ?, deleted = 0, updated_at = now()
                where id = ?
                """, request.reason(), request.sortNoValue(), existingId);
        }

        jdbcTemplate.update("update user_demand set follow_status = 'MATCHED', updated_at = now() where id = ?", id);
        createMessage(id, "平台已推荐阿姨", "平台已为你的需求推荐合适的服务人员，请进入我的需求查看。", "DEMAND_RECOMMENDATION");
        return R.ok(existingId);
    }

    @DeleteMapping("/recommendations/{recommendationId}")
    public R<Void> deleteRecommendation(@PathVariable Long recommendationId) {
        jdbcTemplate.update("""
            update demand_recommendation
            set deleted = 1, updated_at = now()
            where id = ?
            """, recommendationId);
        return R.ok();
    }

    private List<Map<String, Object>> recommendations(Long demandId) {
        return jdbcTemplate.queryForList("""
            select r.id, r.demand_id demandId, r.staff_id staffId, r.reason, r.status, r.sort_no sortNo,
                   s.name staffName, s.avatar_url avatarUrl, s.city, s.district, s.age,
                   s.education, s.experience_years experienceYears, s.salary_min salaryMin,
                   s.salary_max salaryMax, s.salary_unit salaryUnit, c.name categoryName
            from demand_recommendation r
            inner join service_staff s on s.id = r.staff_id
            left join service_category c on c.id = s.category_id
            where r.demand_id = ? and r.deleted = 0 and s.deleted = 0
            order by r.sort_no asc, r.id desc
            """, demandId);
    }

    private void createMessage(Long demandId, String title, String content, String type) {
        Long userId = jdbcTemplate.query(
            "select user_id from user_demand where id = ? and deleted = 0 limit 1",
            rs -> rs.next() ? rs.getLong("user_id") : null,
            demandId
        );
        if (userId == null) {
            return;
        }
        jdbcTemplate.update("""
            insert into system_message(user_id, title, content, message_type, read_flag, created_at, updated_at, deleted)
            values(?, ?, ?, ?, 0, now(), now(), 0)
            """, userId, title, content, type);
    }

    private static String buildWhere(Long categoryId, String auditStatus, String followStatus, String keyword, List<Object> args) {
        StringBuilder where = new StringBuilder("d.deleted = 0");
        if (categoryId != null) {
            where.append(" and d.category_id = ?");
            args.add(categoryId);
        }
        if (auditStatus != null && !auditStatus.isBlank()) {
            where.append(" and d.audit_status = ?");
            args.add(auditStatus.trim());
        }
        if (followStatus != null && !followStatus.isBlank()) {
            where.append(" and d.follow_status = ?");
            args.add(followStatus.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword.trim() + "%";
            where.append(" and (d.title like ? or d.contact_name like ? or d.contact_phone like ?)");
            args.add(like);
            args.add(like);
            args.add(like);
        }
        return where.toString();
    }

    public record RejectRequest(String reason) {
    }

    public record FollowStatusRequest(String followStatus) {
    }

    public record RecommendationRequest(Long staffId, String reason, Integer sortNo) {
        int sortNoValue() {
            return sortNo == null ? 0 : sortNo;
        }
    }
}
