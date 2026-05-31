package org.dromara.web.controller.app;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
                   o.status, o.created_at createdAt, s.name staffName, c.name categoryName
            from service_order o
            left join service_staff s on s.id = o.staff_id
            left join service_category c on c.id = o.category_id
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
                   s.name staffName, c.name categoryName, d.title demandTitle
            from service_order o
            left join service_staff s on s.id = o.staff_id
            left join service_category c on c.id = o.category_id
            left join user_demand d on d.id = o.demand_id
            where o.id = ? and o.user_id = ? and o.deleted = 0
            """, id, userId);
        return R.ok(order);
    }
}
