package org.dromara.web.controller.admin;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/group-products")
public class AdminGroupProductController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public TableDataInfo<Map<String, Object>> list(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "10") int pageSize
    ) {
        List<Object> args = new ArrayList<>();
        String where = "deleted = 0";
        if (status != null && !status.isBlank()) {
            where += " and status = ?";
            args.add(status.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            where += " and title like ?";
            args.add("%" + keyword.trim() + "%");
        }
        Long total = jdbcTemplate.queryForObject("select count(1) from group_product where " + where, Long.class, args.toArray());
        args.add(Math.max(pageSize, 1));
        args.add(Math.max(pageNum - 1, 0) * Math.max(pageSize, 1));
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
            select id, title, cover_url coverUrl, original_price originalPrice, single_price singlePrice,
                   group_price groupPrice, group_size groupSize, valid_days validDays, sold_count soldCount,
                   status, created_at createdAt
            from group_product
            where
            """ + where + "\n" + """
            order by id desc
            limit ? offset ?
            """, args.toArray());
        return new TableDataInfo<>(rows, total == null ? 0 : total);
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        return R.ok(jdbcTemplate.queryForMap("""
            select id, title, cover_url coverUrl, original_price originalPrice, single_price singlePrice,
                   group_price groupPrice, group_size groupSize, valid_days validDays, sold_count soldCount,
                   notice, guarantee, description, status
            from group_product
            where id = ? and deleted = 0
            """, id));
    }

    @PostMapping
    public R<Long> create(@RequestBody GroupProductRequest request) {
        if (request == null || isBlank(request.title())) {
            return R.fail("请填写商品名称");
        }
        jdbcTemplate.update("""
            insert into group_product(title, cover_url, original_price, single_price, group_price, group_size,
                valid_days, sold_count, notice, guarantee, description, status, created_at, updated_at, deleted)
            values(?, ?, ?, ?, ?, ?, ?, 0, ?, ?, ?, ?, now(), now(), 0)
            """, request.title().trim(), request.coverUrl(), money(request.originalPrice()), money(request.singlePrice()),
            money(request.groupPrice()), request.groupSize() == null ? 2 : request.groupSize(),
            request.validDays() == null ? 30 : request.validDays(), request.notice(), request.guarantee(),
            request.description(), isBlank(request.status()) ? "ONLINE" : request.status().trim());
        return R.ok(jdbcTemplate.queryForObject("select last_insert_id()", Long.class));
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody GroupProductRequest request) {
        if (request == null || isBlank(request.title())) {
            return R.fail("请填写商品名称");
        }
        jdbcTemplate.update("""
            update group_product
            set title = ?, cover_url = ?, original_price = ?, single_price = ?, group_price = ?,
                group_size = ?, valid_days = ?, notice = ?, guarantee = ?, description = ?,
                status = ?, updated_at = now()
            where id = ? and deleted = 0
            """, request.title().trim(), request.coverUrl(), money(request.originalPrice()), money(request.singlePrice()),
            money(request.groupPrice()), request.groupSize() == null ? 2 : request.groupSize(),
            request.validDays() == null ? 30 : request.validDays(), request.notice(), request.guarantee(),
            request.description(), isBlank(request.status()) ? "ONLINE" : request.status().trim(), id);
        return R.ok();
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        if (request == null || isBlank(request.status())) {
            return R.fail("请选择商品状态");
        }
        jdbcTemplate.update("update group_product set status = ?, updated_at = now() where id = ? and deleted = 0", request.status().trim(), id);
        return R.ok();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public record GroupProductRequest(
        String title,
        String coverUrl,
        BigDecimal originalPrice,
        BigDecimal singlePrice,
        BigDecimal groupPrice,
        Integer groupSize,
        Integer validDays,
        String notice,
        String guarantee,
        String description,
        String status
    ) {
    }

    public record StatusRequest(String status) {
    }
}
