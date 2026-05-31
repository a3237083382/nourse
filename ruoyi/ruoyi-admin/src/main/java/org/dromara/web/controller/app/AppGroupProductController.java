package org.dromara.web.controller.app;

import cn.dev33.satoken.annotation.SaIgnore;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@SaIgnore
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/app/group-products")
public class AppGroupProductController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public TableDataInfo<Map<String, Object>> list(
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "10") int pageSize
    ) {
        Long total = jdbcTemplate.queryForObject("""
            select count(1) from group_product where deleted = 0 and status = 'ONLINE'
            """, Long.class);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
            select id, title, cover_url coverUrl, original_price originalPrice,
                   single_price singlePrice, group_price groupPrice, group_size groupSize,
                   valid_days validDays, sold_count soldCount, notice, guarantee, description
            from group_product
            where deleted = 0 and status = 'ONLINE'
            order by id desc
            limit ? offset ?
            """, Math.max(pageSize, 1), Math.max(pageNum - 1, 0) * Math.max(pageSize, 1));
        return new TableDataInfo<>(rows, total == null ? 0 : total);
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        Map<String, Object> product = jdbcTemplate.queryForMap("""
            select id, title, cover_url coverUrl, original_price originalPrice,
                   single_price singlePrice, group_price groupPrice, group_size groupSize,
                   valid_days validDays, sold_count soldCount, notice, guarantee, description
            from group_product
            where id = ? and deleted = 0 and status = 'ONLINE'
            """, id);
        product.put("activeTeams", activeTeams(id));
        return R.ok(product);
    }

    @GetMapping("/{id}/active-teams")
    public R<List<Map<String, Object>>> activeTeamList(@PathVariable Long id) {
        return R.ok(activeTeams(id));
    }

    private List<Map<String, Object>> activeTeams(Long productId) {
        return jdbcTemplate.queryForList("""
            select t.id, t.product_id productId, t.group_size groupSize, t.joined_count joinedCount,
                   t.expire_at expireAt, u.nickname leaderNickname,
                   greatest(t.group_size - t.joined_count, 0) remainingCount
            from group_team t
            left join app_user u on u.id = t.leader_user_id
            where t.product_id = ? and t.deleted = 0 and t.status = 'GROUPING' and t.expire_at > now()
            order by t.id desc
            limit 5
            """, productId);
    }
}
