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
 * 小程序首页聚合接口。
 */
@SaIgnore
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/app/home")
public class AppHomeController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public R<Map<String, Object>> home() {
        List<Map<String, Object>> categories = jdbcTemplate.queryForList("""
            select id, name, icon_url iconUrl, sort_no sortNo
            from service_category
            where enabled = 1 and deleted = 0
            order by sort_no asc, id asc
            """);
        List<Map<String, Object>> staff = jdbcTemplate.queryForList("""
            select id, name, avatar_url avatarUrl, city, district, salary_min salaryMin, salary_max salaryMax,
                   salary_unit salaryUnit, service_desc serviceDesc
            from service_staff
            where deleted = 0 and status = 'ONLINE'
            order by recommended desc, sort_no asc, id desc
            limit 4
            """);
        return R.ok(Map.of(
            "categories", categories,
            "signSuccessTips", List.of(
                "恭喜李女士签约月嫂服务",
                "恭喜王先生预约保洁服务",
                "恭喜陈女士完成育婴师面试"
            ),
            "groupProducts", List.of(
                Map.of("id", 1, "title", "深度保洁体验", "price", 429, "groupPrice", 299),
                Map.of("id", 2, "title", "老人陪护体验", "price", 368, "groupPrice", 258)
            ),
            "recommendedStaff", staff
        ));
    }
}
