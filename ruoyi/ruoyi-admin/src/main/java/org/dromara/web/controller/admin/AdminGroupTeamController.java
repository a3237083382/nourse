package org.dromara.web.controller.admin;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.web.service.SystemMessageService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/group-teams")
public class AdminGroupTeamController {

    private final JdbcTemplate jdbcTemplate;
    private final SystemMessageService systemMessageService;

    @GetMapping
    public TableDataInfo<Map<String, Object>> list(
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "10") int pageSize
    ) {
        Long total = jdbcTemplate.queryForObject("select count(1) from group_team where deleted = 0", Long.class);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
            select t.id, t.product_id productId, p.title productTitle, t.leader_user_id leaderUserId,
                   t.group_size groupSize, t.joined_count joinedCount, t.expire_at expireAt,
                   t.status, t.created_at createdAt, t.updated_at updatedAt
            from group_team t
            left join group_product p on p.id = t.product_id
            where t.deleted = 0
            order by t.id desc
            limit ? offset ?
            """, Math.max(pageSize, 1), Math.max(pageNum - 1, 0) * Math.max(pageSize, 1));
        return new TableDataInfo<>(rows, total == null ? 0 : total);
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        if (request == null || request.status() == null || request.status().isBlank()) {
            return R.fail("请选择拼团状态");
        }
        String status = request.status().trim();
        jdbcTemplate.update("""
            update group_team
            set status = ?, updated_at = now()
            where id = ? and deleted = 0
            """, status, id);
        if ("SUCCESS".equals(status)) {
            jdbcTemplate.update("""
                update group_order
                set status = 'WAIT_USE', updated_at = now()
                where group_team_id = ? and deleted = 0
                """, id);
            String productTitle = jdbcTemplate.query("""
                select p.title
                from group_team t
                inner join group_product p on p.id = t.product_id
                where t.id = ? and t.deleted = 0
                limit 1
                """, rs -> rs.next() ? rs.getString("title") : "团购服务", id);
            systemMessageService.createGroupSuccess(id, productTitle);
        }
        return R.ok();
    }

    public record StatusRequest(String status) {
    }
}
