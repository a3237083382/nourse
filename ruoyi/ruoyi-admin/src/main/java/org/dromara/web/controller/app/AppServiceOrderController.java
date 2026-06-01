package org.dromara.web.controller.app;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
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
@RequestMapping("/api/app/service-orders")
public class AppServiceOrderController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public TableDataInfo<Map<String, Object>> list(
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "10") int pageSize
    ) {
        Long userId = LoginHelper.getUserId();
        String where = "o.user_id = ? and o.deleted = 0";
        Object[] countArgs;
        Object[] listArgs;
        if (status != null && !status.isBlank()) {
            where += " and o.status = ?";
            countArgs = new Object[] { userId, status.trim() };
            listArgs = new Object[] { userId, status.trim(), Math.max(pageSize, 1), Math.max(pageNum - 1, 0) * Math.max(pageSize, 1) };
        } else {
            countArgs = new Object[] { userId };
            listArgs = new Object[] { userId, Math.max(pageSize, 1), Math.max(pageNum - 1, 0) * Math.max(pageSize, 1) };
        }
        Long total = jdbcTemplate.queryForObject("select count(1) from service_order o where " + where, Long.class, countArgs);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
            select o.id, o.order_no orderNo, o.amount, o.start_date startDate, o.end_date endDate,
                   o.status, o.created_at createdAt, s.name staffName, c.name categoryName,
                   r.id reviewId, r.rating reviewRating, r.content reviewContent
            from service_order o
            left join service_staff s on s.id = o.staff_id
            left join service_category c on c.id = o.category_id
            left join service_order_review r on r.service_order_id = o.id and r.deleted = 0
            where
            """ + where + "\n" + """
            order by o.id desc
            limit ? offset ?
            """, listArgs);
        return new TableDataInfo<>(rows, total == null ? 0 : total);
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        Long userId = LoginHelper.getUserId();
        Map<String, Object> order = jdbcTemplate.queryForMap("""
            select o.id, o.order_no orderNo, o.amount, o.start_date startDate, o.end_date endDate,
                   o.status, o.admin_note adminNote, o.created_at createdAt,
                   s.name staffName, c.name categoryName, d.title demandTitle,
                   r.id reviewId, r.rating reviewRating, r.content reviewContent
            from service_order o
            left join service_staff s on s.id = o.staff_id
            left join service_category c on c.id = o.category_id
            left join user_demand d on d.id = o.demand_id
            left join service_order_review r on r.service_order_id = o.id and r.deleted = 0
            where o.id = ? and o.user_id = ? and o.deleted = 0
            """, id, userId);
        return R.ok(order);
    }

    @PostMapping("/{id}/review")
    @Transactional(rollbackFor = Exception.class)
    public R<Void> review(@PathVariable Long id, @RequestBody ReviewRequest request) {
        if (request == null || request.rating() == null || request.rating() < 1 || request.rating() > 5) {
            return R.fail("请选择 1-5 分评价");
        }
        Long userId = LoginHelper.getUserId();
        Map<String, Object> order = jdbcTemplate.queryForMap("""
            select id, user_id userId, staff_id staffId, status
            from service_order
            where id = ? and user_id = ? and deleted = 0
            """, id, userId);
        if (!"COMPLETED".equals(order.get("status"))) {
            return R.fail("服务完成后才可以评价");
        }
        Long exists = jdbcTemplate.queryForObject("""
            select count(1) from service_order_review
            where service_order_id = ? and deleted = 0
            """, Long.class, id);
        if (exists != null && exists > 0) {
            return R.fail("该服务订单已评价");
        }
        jdbcTemplate.update("""
            insert into service_order_review(service_order_id, user_id, staff_id, rating, content, created_at, updated_at, deleted)
            values(?, ?, ?, ?, ?, now(), now(), 0)
            """, id, userId, ((Number) order.get("staffId")).longValue(), request.rating(), cleanContent(request.content()));
        return R.ok();
    }

    private static String cleanContent(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        String text = content.trim();
        return text.length() > 512 ? text.substring(0, 512) : text;
    }

    public record ReviewRequest(Integer rating, String content) {
    }
}
