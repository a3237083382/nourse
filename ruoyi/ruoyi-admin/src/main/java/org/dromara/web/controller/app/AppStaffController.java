package org.dromara.web.controller.app;

import cn.dev33.satoken.annotation.SaIgnore;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 小程序服务人员公开接口。
 */
@SaIgnore
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/app/staff")
public class AppStaffController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public TableDataInfo<Map<String, Object>> list(
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String city,
        @RequestParam(required = false) String district,
        @RequestParam(required = false) String education,
        @RequestParam(required = false) Integer ageMin,
        @RequestParam(required = false) Integer ageMax,
        @RequestParam(required = false) Integer salaryMin,
        @RequestParam(required = false) Integer salaryMax,
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "10") int pageSize
    ) {
        List<Object> args = new ArrayList<>();
        String where = buildWhere(categoryId, keyword, city, district, education, ageMin, ageMax, salaryMin, salaryMax, args);
        Long total = jdbcTemplate.queryForObject("select count(1) from service_staff s where " + where, Long.class, args.toArray());
        args.add(Math.max(pageSize, 1));
        args.add(Math.max(pageNum - 1, 0) * Math.max(pageSize, 1));
        String sql = """
            select s.id, s.category_id categoryId, c.name categoryName, s.name, s.avatar_url avatarUrl,
                   s.age, s.city, s.district, s.education, s.experience_years experienceYears,
                   s.salary_min salaryMin, s.salary_max salaryMax, s.salary_unit salaryUnit,
                   s.service_desc serviceDesc, s.native_place nativePlace, s.height_cm heightCm,
                   s.weight_kg weightKg, s.birth_date birthDate, s.marital_status maritalStatus,
                   s.self_intro selfIntro, s.skills, s.verification_note verificationNote, s.recommended
            from service_staff s
            left join service_category c on c.id = s.category_id
            where
            """ + where + "\n" + """
            order by s.recommended desc, s.sort_no asc, s.id desc
            limit ? offset ?
            """;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, args.toArray());
        return new TableDataInfo<>(rows, total == null ? 0 : total);
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        Map<String, Object> staff = jdbcTemplate.queryForMap("""
            select s.id, s.category_id categoryId, c.name categoryName, s.name, s.avatar_url avatarUrl,
                   s.gender, s.age, s.city, s.district, s.education, s.experience_years experienceYears,
                   s.salary_min salaryMin, s.salary_max salaryMax, s.salary_unit salaryUnit,
                   s.service_desc serviceDesc, s.native_place nativePlace, s.height_cm heightCm,
                   s.weight_kg weightKg, s.birth_date birthDate, s.marital_status maritalStatus,
                   s.self_intro selfIntro, s.skills, s.verification_note verificationNote, s.recommended
            from service_staff s
            left join service_category c on c.id = s.category_id
            where s.id = ? and s.deleted = 0 and s.status = 'ONLINE'
            """, id);
        staff.put("tags", childRows("select id, tag_name tagName from staff_tag where staff_id = ? and deleted = 0 order by id asc", id));
        staff.put("certificates", childRows("select id, certificate_name certificateName, file_url fileUrl, sort_no sortNo from staff_certificate where staff_id = ? and deleted = 0 order by sort_no asc, id asc", id));
        staff.put("photos", childRows("select id, photo_url photoUrl, sort_no sortNo from staff_photo where staff_id = ? and deleted = 0 order by sort_no asc, id asc", id));
        staff.put("experiences", childRows("select id, start_date startDate, end_date endDate, description from staff_work_experience where staff_id = ? and deleted = 0 order by start_date desc, id desc", id));
        staff.put("reviews", childRows("""
            select r.id, r.rating, r.content, r.created_at createdAt, u.nickname userName
            from service_order_review r
            left join app_user u on u.id = r.user_id
            where r.staff_id = ? and r.deleted = 0
            order by r.id desc
            limit 10
            """, id));
        return R.ok(staff);
    }

    private static String buildWhere(Long categoryId, String keyword, String city, String district, String education,
                                     Integer ageMin, Integer ageMax,
                                     Integer salaryMin, Integer salaryMax, List<Object> args) {
        StringBuilder where = new StringBuilder("s.deleted = 0 and s.status = 'ONLINE'");
        if (categoryId != null) {
            where.append(" and s.category_id = ?");
            args.add(categoryId);
        }
        if (keyword != null && !keyword.isBlank()) {
            where.append(" and (s.name like ? or s.service_desc like ?)");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
        }
        if (city != null && !city.isBlank()) {
            where.append(" and s.city = ?");
            args.add(city.trim());
        }
        if (district != null && !district.isBlank()) {
            where.append(" and s.district = ?");
            args.add(district.trim());
        }
        if (education != null && !education.isBlank()) {
            where.append(" and s.education = ?");
            args.add(education.trim());
        }
        if (ageMin != null) {
            where.append(" and s.age >= ?");
            args.add(ageMin);
        }
        if (ageMax != null) {
            where.append(" and s.age <= ?");
            args.add(ageMax);
        }
        if (salaryMin != null) {
            where.append(" and (s.salary_max is null or s.salary_max >= ?)");
            args.add(salaryMin);
        }
        if (salaryMax != null) {
            where.append(" and (s.salary_min is null or s.salary_min <= ?)");
            args.add(salaryMax);
        }
        return where.toString();
    }

    private List<Map<String, Object>> childRows(String sql, Long id) {
        return jdbcTemplate.queryForList(sql, id);
    }
}
