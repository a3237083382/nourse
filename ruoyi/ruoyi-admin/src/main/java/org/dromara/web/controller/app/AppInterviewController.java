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
@RequestMapping("/api/app/interviews")
public class AppInterviewController {

    private final JdbcTemplate jdbcTemplate;

    @PostMapping
    public R<Long> create(@RequestBody InterviewRequest request) {
        Long userId = LoginHelper.getUserId();
        if (request == null || request.staffId() == null
            || request.contactName() == null || request.contactName().isBlank()
            || request.contactPhone() == null || request.contactPhone().isBlank()) {
            return R.fail("请完整填写服务人员、联系人和联系电话");
        }
        Long staffCount = jdbcTemplate.queryForObject(
            "select count(1) from service_staff where id = ? and deleted = 0 and status = 'ONLINE'",
            Long.class,
            request.staffId()
        );
        if (staffCount == null || staffCount == 0) {
            return R.fail("服务人员不存在或未上架");
        }
        if (request.demandId() != null) {
            Long demandCount = jdbcTemplate.queryForObject(
                "select count(1) from user_demand where id = ? and user_id = ? and deleted = 0",
                Long.class,
                request.demandId(),
                userId
            );
            if (demandCount == null || demandCount == 0) {
                return R.fail("关联需求不存在");
            }
        }
        jdbcTemplate.update("""
            insert into interview_appointment(user_id, staff_id, demand_id, contact_name, contact_phone,
                status, created_at, updated_at, deleted)
            values(?, ?, ?, ?, ?, 'PENDING', now(), now(), 0)
            """, userId, request.staffId(), request.demandId(), request.contactName().trim(),
            request.contactPhone().trim());
        Long id = jdbcTemplate.queryForObject("select last_insert_id()", Long.class);
        if (request.demandId() != null) {
            jdbcTemplate.update("""
                update demand_recommendation
                set status = 'INTERVIEWED', updated_at = now()
                where demand_id = ? and staff_id = ? and deleted = 0
                """, request.demandId(), request.staffId());
        }
        return R.ok(id);
    }

    @GetMapping
    public TableDataInfo<Map<String, Object>> list(
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "10") int pageSize
    ) {
        Long userId = LoginHelper.getUserId();
        Long total = jdbcTemplate.queryForObject("""
            select count(1)
            from interview_appointment
            where user_id = ? and deleted = 0
            """, Long.class, userId);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
            select i.id, i.staff_id staffId, s.name staffName, s.avatar_url avatarUrl,
                   c.name categoryName, i.demand_id demandId, d.title demandTitle,
                   i.contact_name contactName, i.contact_phone contactPhone,
                   i.status, i.created_at createdAt, i.updated_at updatedAt
            from interview_appointment i
            inner join service_staff s on s.id = i.staff_id
            left join service_category c on c.id = s.category_id
            left join user_demand d on d.id = i.demand_id
            where i.user_id = ? and i.deleted = 0
            order by i.id desc
            limit ? offset ?
            """, userId, Math.max(pageSize, 1), Math.max(pageNum - 1, 0) * Math.max(pageSize, 1));
        return new TableDataInfo<>(rows, total == null ? 0 : total);
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        Long userId = LoginHelper.getUserId();
        Map<String, Object> interview = jdbcTemplate.queryForMap("""
            select i.id, i.staff_id staffId, s.name staffName, s.avatar_url avatarUrl,
                   c.name categoryName, i.demand_id demandId, d.title demandTitle,
                   i.contact_name contactName, i.contact_phone contactPhone,
                   i.status, i.admin_note adminNote, i.created_at createdAt, i.updated_at updatedAt
            from interview_appointment i
            inner join service_staff s on s.id = i.staff_id
            left join service_category c on c.id = s.category_id
            left join user_demand d on d.id = i.demand_id
            where i.id = ? and i.user_id = ? and i.deleted = 0
            """, id, userId);
        return R.ok(interview);
    }

    public record InterviewRequest(
        Long staffId,
        Long demandId,
        String contactName,
        String contactPhone
    ) {
    }
}
