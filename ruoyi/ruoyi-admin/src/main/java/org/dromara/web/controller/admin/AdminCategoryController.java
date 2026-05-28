package org.dromara.web.controller.admin;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 服务分类后台管理接口。
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public TableDataInfo<Map<String, Object>> list() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
            select id, name, icon_url iconUrl, sort_no sortNo, enabled, created_at createdAt, updated_at updatedAt
            from service_category
            where deleted = 0
            order by sort_no asc, id asc
            """);
        return TableDataInfo.build(rows);
    }

    @PostMapping
    public R<Long> create(@RequestBody CategoryRequest request) {
        jdbcTemplate.update("""
            insert into service_category(name, icon_url, sort_no, enabled, created_at, updated_at, deleted)
            values(?, ?, ?, ?, now(), now(), 0)
            """, request.name(), request.iconUrl(), request.sortNoValue(), request.enabledValue());
        Long id = jdbcTemplate.queryForObject("select last_insert_id()", Long.class);
        return R.ok(id);
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody CategoryRequest request) {
        jdbcTemplate.update("""
            update service_category
            set name = ?, icon_url = ?, sort_no = ?, enabled = ?, updated_at = now()
            where id = ? and deleted = 0
            """, request.name(), request.iconUrl(), request.sortNoValue(), request.enabledValue(), id);
        return R.ok();
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        int enabled = boolValue(body.get("enabled")) ? 1 : 0;
        jdbcTemplate.update("update service_category set enabled = ?, updated_at = now() where id = ? and deleted = 0", enabled, id);
        return R.ok();
    }

    private static boolean boolValue(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        return value != null && Boolean.parseBoolean(value.toString());
    }

    public record CategoryRequest(String name, String iconUrl, Integer sortNo, Boolean enabled) {
        int sortNoValue() {
            return sortNo == null ? 0 : sortNo;
        }

        int enabledValue() {
            return enabled == null || enabled ? 1 : 0;
        }
    }
}
