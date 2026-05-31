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
@RequestMapping("/api/app/contracts")
public class AppContractController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public TableDataInfo<Map<String, Object>> list(
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "10") int pageSize
    ) {
        Long userId = LoginHelper.getUserId();
        String where = "c.user_id = ? and c.deleted = 0";
        Object[] countArgs;
        Object[] listArgs;
        if (status != null && !status.isBlank()) {
            where += " and c.status = ?";
            countArgs = new Object[] { userId, status.trim() };
            listArgs = new Object[] { userId, status.trim(), Math.max(pageSize, 1), Math.max(pageNum - 1, 0) * Math.max(pageSize, 1) };
        } else {
            countArgs = new Object[] { userId };
            listArgs = new Object[] { userId, Math.max(pageSize, 1), Math.max(pageNum - 1, 0) * Math.max(pageSize, 1) };
        }
        Long total = jdbcTemplate.queryForObject("select count(1) from contract c where " + where, Long.class, countArgs);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
            select c.id, c.contract_no contractNo, c.title, c.file_url fileUrl, c.status,
                   c.signed_at signedAt, c.terminated_at terminatedAt, c.created_at createdAt,
                   s.name staffName, sc.name categoryName
            from contract c
            left join service_staff s on s.id = c.staff_id
            left join service_category sc on sc.id = s.category_id
            where
            """ + where + "\n" + """
            order by c.id desc
            limit ? offset ?
            """, listArgs);
        return new TableDataInfo<>(rows, total == null ? 0 : total);
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        Long userId = LoginHelper.getUserId();
        Map<String, Object> contract = jdbcTemplate.queryForMap("""
            select c.id, c.contract_no contractNo, c.title, c.file_url fileUrl, c.status,
                   c.signed_at signedAt, c.terminated_at terminatedAt, c.created_at createdAt,
                   s.name staffName, sc.name categoryName, d.title demandTitle, o.order_no orderNo
            from contract c
            left join service_staff s on s.id = c.staff_id
            left join service_category sc on sc.id = s.category_id
            left join user_demand d on d.id = c.demand_id
            left join service_order o on o.id = c.service_order_id
            where c.id = ? and c.user_id = ? and c.deleted = 0
            """, id, userId);
        return R.ok(contract);
    }
}
