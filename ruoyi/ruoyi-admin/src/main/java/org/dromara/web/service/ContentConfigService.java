package org.dromara.web.service;

import lombok.RequiredArgsConstructor;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class ContentConfigService {

    private final JdbcTemplate jdbcTemplate;

    public TableDataInfo<Map<String, Object>> listAdmin(String contentType, int pageNum, int pageSize) {
        List<Object> args = new ArrayList<>();
        String where = buildWhere(contentType, args);
        Long total = jdbcTemplate.queryForObject("select count(1) from content_config where " + where, Long.class, args.toArray());
        int safePageSize = Math.max(pageSize, 1);
        args.add(safePageSize);
        args.add(Math.max(pageNum - 1, 0) * safePageSize);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
            select id, content_type contentType, title, image_url imageUrl, content,
                   sort_no sortNo, enabled, created_at createdAt, updated_at updatedAt
            from content_config
            """ + " where " + where + " order by content_type asc, sort_no asc, id desc limit ? offset ?", args.toArray());
        return new TableDataInfo<>(rows, total == null ? 0 : total);
    }

    public Long create(ContentRequest request) {
        jdbcTemplate.update("""
            insert into content_config(content_type, title, image_url, content, sort_no, enabled, created_at, updated_at, deleted)
            values(?, ?, ?, ?, ?, ?, now(), now(), 0)
            """, request.contentType(), request.title(), request.imageUrl(), request.content(),
            request.sortNoValue(), request.enabledValue());
        return jdbcTemplate.queryForObject("select last_insert_id()", Long.class);
    }

    public void update(Long id, ContentRequest request) {
        jdbcTemplate.update("""
            update content_config
            set content_type = ?, title = ?, image_url = ?, content = ?, sort_no = ?, enabled = ?, updated_at = now()
            where id = ? and deleted = 0
            """, request.contentType(), request.title(), request.imageUrl(), request.content(),
            request.sortNoValue(), request.enabledValue(), id);
    }

    public void updateStatus(Long id, Boolean enabled) {
        jdbcTemplate.update("""
            update content_config
            set enabled = ?, updated_at = now()
            where id = ? and deleted = 0
            """, enabled != null && enabled ? 1 : 0, id);
    }

    public List<Map<String, Object>> listEnabled(String contentType) {
        return jdbcTemplate.queryForList("""
            select id, content_type contentType, title, image_url imageUrl, content, sort_no sortNo
            from content_config
            where content_type = ? and enabled = 1 and deleted = 0
            order by sort_no asc, id desc
            """, contentType);
    }

    private static String buildWhere(String contentType, List<Object> args) {
        StringBuilder where = new StringBuilder("deleted = 0");
        if (contentType != null && !contentType.isBlank()) {
            where.append(" and content_type = ?");
            args.add(contentType.trim());
        }
        return where.toString();
    }

    public record ContentRequest(
        String contentType,
        String title,
        String imageUrl,
        String content,
        Integer sortNo,
        Boolean enabled
    ) {
        int sortNoValue() {
            return sortNo == null ? 0 : sortNo;
        }

        int enabledValue() {
            return enabled == null || enabled ? 1 : 0;
        }
    }
}
