package org.dromara.web.controller.admin;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/interviews")
public class AdminInterviewController {

    private static final Set<String> STATUSES = Set.of("PENDING", "CONTACTED", "ARRANGED", "COMPLETED", "CANCELED");

    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public TableDataInfo<Map<String, Object>> list(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "10") int pageSize
    ) {
        List<Object> args = new ArrayList<>();
        String where = buildWhere(status, keyword, args);
        Long total = jdbcTemplate.queryForObject(
            "select count(1) from interview_appointment i inner join service_staff s on s.id = i.staff_id left join app_user u on u.id = i.user_id left join user_demand d on d.id = i.demand_id where " + where,
            Long.class,
            args.toArray()
        );
        args.add(Math.max(pageSize, 1));
        args.add(Math.max(pageNum - 1, 0) * Math.max(pageSize, 1));
        String sql = """
            select i.id, i.user_id userId, u.nickname userNickname,
                   i.staff_id staffId, s.name staffName, c.name categoryName,
                   i.demand_id demandId, d.title demandTitle,
                   i.contact_name contactName, i.contact_phone contactPhone,
                   i.status, i.created_at createdAt, i.updated_at updatedAt
            from interview_appointment i
            inner join service_staff s on s.id = i.staff_id
            left join service_category c on c.id = s.category_id
            left join app_user u on u.id = i.user_id
            left join user_demand d on d.id = i.demand_id
            """ + " where " + where + " order by i.id desc limit ? offset ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, args.toArray());
        return new TableDataInfo<>(rows, total == null ? 0 : total);
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        Map<String, Object> interview = jdbcTemplate.queryForMap("""
            select i.id, i.user_id userId, u.nickname userNickname,
                   i.staff_id staffId, s.name staffName, c.name categoryName,
                   i.demand_id demandId, d.title demandTitle,
                   i.contact_name contactName, i.contact_phone contactPhone,
                   i.status, i.admin_note adminNote, i.created_at createdAt, i.updated_at updatedAt
            from interview_appointment i
            inner join service_staff s on s.id = i.staff_id
            left join service_category c on c.id = s.category_id
            left join app_user u on u.id = i.user_id
            left join user_demand d on d.id = i.demand_id
            where i.id = ? and i.deleted = 0
            """, id);
        return R.ok(interview);
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        if (request == null || request.status() == null || !STATUSES.contains(request.status())) {
            return R.fail("预约状态不正确");
        }
        jdbcTemplate.update("""
            update interview_appointment
            set status = ?, updated_at = now()
            where id = ? and deleted = 0
            """, request.status(), id);
        return R.ok();
    }

    @PutMapping("/{id}/note")
    public R<Void> updateNote(@PathVariable Long id, @RequestBody NoteRequest request) {
        jdbcTemplate.update("""
            update interview_appointment
            set admin_note = ?, updated_at = now()
            where id = ? and deleted = 0
            """, request == null ? null : request.adminNote(), id);
        return R.ok();
    }

    private static String buildWhere(String status, String keyword, List<Object> args) {
        StringBuilder where = new StringBuilder("i.deleted = 0");
        if (status != null && !status.isBlank()) {
            where.append(" and i.status = ?");
            args.add(status.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword.trim() + "%";
            where.append(" and (s.name like ? or i.contact_name like ? or i.contact_phone like ? or d.title like ?)");
            args.add(like);
            args.add(like);
            args.add(like);
            args.add(like);
        }
        return where.toString();
    }

    public record StatusRequest(String status) {
    }

    public record NoteRequest(String adminNote) {
    }
}
