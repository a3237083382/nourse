package org.dromara.web.controller.app;

import cn.dev33.satoken.annotation.SaIgnore;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.web.service.ContentConfigService;
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
    private final ContentConfigService contentConfigService;

    @GetMapping
    public R<Map<String, Object>> home() {
        List<Map<String, Object>> categories = jdbcTemplate.queryForList("""
            select id, name, icon_url iconUrl, sort_no sortNo
            from service_category
            where enabled = 1 and deleted = 0
            order by sort_no asc, id asc
            """);
        List<Map<String, Object>> staffCategories = jdbcTemplate.queryForList("""
            select id, name, icon_url iconUrl, sort_no sortNo
            from service_category
            where enabled = 1 and deleted = 0
              and name in ('月嫂', '保姆', '育婴师')
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
        List<Map<String, Object>> groupProducts = jdbcTemplate.queryForList("""
            select id, title, cover_url coverUrl, original_price originalPrice,
                   single_price price, single_price singlePrice, group_price groupPrice,
                   group_size groupSize, sold_count soldCount,
                   (
                       select min(t.expire_at)
                       from group_team t
                       where t.product_id = group_product.id
                         and t.deleted = 0
                         and t.status = 'GROUPING'
                         and t.expire_at > now()
                   ) activeTeamExpireAt,
                   (
                       select count(1)
                       from group_team t
                       where t.product_id = group_product.id
                         and t.deleted = 0
                         and t.status = 'GROUPING'
                         and t.expire_at > now()
                   ) activeTeamCount
            from group_product
            where deleted = 0 and status = 'ONLINE'
            order by id desc
            limit 4
            """);
        List<Map<String, Object>> banners = contentConfigService.listEnabled("BANNER");
        List<String> signTips = contentConfigService.listEnabled("SIGN_TIP").stream()
            .map(item -> {
                Object content = item.get("content");
                if (content != null && !content.toString().isBlank()) {
                    return content.toString();
                }
                Object title = item.get("title");
                return title == null ? "" : title.toString();
            })
            .filter(text -> !text.isBlank())
            .toList();
        if (signTips.isEmpty()) {
            signTips = List.of(
                "恭喜李女士签约月嫂服务",
                "恭喜王先生预约保洁服务",
                "恭喜陈女士完成育婴师面试"
            );
        }
        return R.ok(Map.of(
            "banners", banners,
            "categories", categories,
            "staffCategories", staffCategories,
            "signSuccessTips", signTips,
            "groupProducts", groupProducts,
            "recommendedStaff", staff
        ));
    }
}
