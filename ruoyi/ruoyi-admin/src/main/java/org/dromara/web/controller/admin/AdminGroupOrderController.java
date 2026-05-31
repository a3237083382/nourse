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

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/group-orders")
public class AdminGroupOrderController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public TableDataInfo<Map<String, Object>> list(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "10") int pageSize
    ) {
        List<Object> args = new ArrayList<>();
        String where = "o.deleted = 0";
        if (status != null && !status.isBlank()) {
            where += " and o.status = ?";
            args.add(status.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            where += " and (o.order_no like ? or p.title like ? or u.nickname like ?)";
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
            args.add(like);
        }
        Long total = jdbcTemplate.queryForObject("select count(1) from group_order o left join group_product p on p.id = o.product_id left join app_user u on u.id = o.user_id where " + where, Long.class, args.toArray());
        args.add(Math.max(pageSize, 1));
        args.add(Math.max(pageNum - 1, 0) * Math.max(pageSize, 1));
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
            select o.id, o.order_no orderNo, o.buy_type buyType, o.quantity, o.amount, o.status,
                   o.group_team_id groupTeamId, o.valid_until validUntil, o.created_at createdAt,
                   p.title productTitle, u.nickname userNickname, t.status teamStatus
            from group_order o
            left join group_product p on p.id = o.product_id
            left join app_user u on u.id = o.user_id
            left join group_team t on t.id = o.group_team_id
            where
            """ + where + "\n" + """
            order by o.id desc
            limit ? offset ?
            """, args.toArray());
        return new TableDataInfo<>(rows, total == null ? 0 : total);
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        return R.ok(jdbcTemplate.queryForMap("""
            select o.id, o.order_no orderNo, o.buy_type buyType, o.quantity, o.amount, o.status,
                   o.group_team_id groupTeamId, o.paid_at paidAt, o.valid_until validUntil, o.created_at createdAt,
                   p.title productTitle, u.nickname userNickname, t.status teamStatus,
                   t.group_size groupSize, t.joined_count joinedCount
            from group_order o
            left join group_product p on p.id = o.product_id
            left join app_user u on u.id = o.user_id
            left join group_team t on t.id = o.group_team_id
            where o.id = ? and o.deleted = 0
            """, id));
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        if (request == null || request.status() == null || request.status().isBlank()) {
            return R.fail("请选择订单状态");
        }
        jdbcTemplate.update("update group_order set status = ?, updated_at = now() where id = ? and deleted = 0", request.status().trim(), id);
        return R.ok();
    }

    public record StatusRequest(String status) {
    }
}
