package org.dromara.web.controller.app;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 小程序用户收藏接口。
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/app")
public class AppFavoriteController {

    private final JdbcTemplate jdbcTemplate;

    @PostMapping("/staff/{id}/favorite")
    public R<Void> favoriteStaff(@PathVariable Long id) {
        Long userId = LoginHelper.getUserId();
        try {
            jdbcTemplate.update("""
                insert into user_staff_favorite(user_id, staff_id, created_at, updated_at, deleted)
                values(?, ?, now(), now(), 0)
                """, userId, id);
        } catch (DuplicateKeyException ignored) {
            jdbcTemplate.update("""
                update user_staff_favorite set deleted = 0, updated_at = now()
                where user_id = ? and staff_id = ?
                """, userId, id);
        }
        return R.ok();
    }

    @DeleteMapping("/staff/{id}/favorite")
    public R<Void> cancelFavoriteStaff(@PathVariable Long id) {
        Long userId = LoginHelper.getUserId();
        jdbcTemplate.update("""
            update user_staff_favorite set deleted = 1, updated_at = now()
            where user_id = ? and staff_id = ?
            """, userId, id);
        return R.ok();
    }

    @GetMapping("/favorites/staff")
    public TableDataInfo<Map<String, Object>> favoriteStaffList(
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "10") int pageSize
    ) {
        Long userId = LoginHelper.getUserId();
        Long total = jdbcTemplate.queryForObject("""
            select count(1)
            from user_staff_favorite f
            inner join service_staff s on s.id = f.staff_id
            where f.user_id = ? and f.deleted = 0 and s.deleted = 0 and s.status = 'ONLINE'
            """, Long.class, userId);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
            select s.id, s.category_id categoryId, c.name categoryName, s.name, s.avatar_url avatarUrl,
                   s.age, s.city, s.district, s.education, s.experience_years experienceYears,
                   s.salary_min salaryMin, s.salary_max salaryMax, s.salary_unit salaryUnit,
                   s.service_desc serviceDesc, s.recommended, true favorited
            from user_staff_favorite f
            inner join service_staff s on s.id = f.staff_id
            left join service_category c on c.id = s.category_id
            where f.user_id = ? and f.deleted = 0 and s.deleted = 0 and s.status = 'ONLINE'
            order by f.updated_at desc, f.id desc
            limit ? offset ?
            """, userId, Math.max(pageSize, 1), Math.max(pageNum - 1, 0) * Math.max(pageSize, 1));
        return new TableDataInfo<>(rows, total == null ? 0 : total);
    }
}
