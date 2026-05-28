package org.dromara.web.controller.app;

import cn.dev33.satoken.annotation.SaIgnore;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 小程序服务分类接口。
 */
@SaIgnore
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/app/categories")
public class AppCategoryController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public R<List<Map<String, Object>>> list() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
            select id, name, icon_url iconUrl, sort_no sortNo
            from service_category
            where deleted = 0 and enabled = 1
            order by sort_no asc, id asc
            """);
        return R.ok(rows);
    }
}
