package org.dromara.web.controller.admin;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 服务人员后台管理接口。
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/staff")
public class AdminStaffController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public TableDataInfo<Map<String, Object>> list(
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "10") int pageSize
    ) {
        List<Object> args = new ArrayList<>();
        String where = buildWhere(categoryId, status, keyword, args);
        Long total = jdbcTemplate.queryForObject("select count(1) from service_staff s where " + where, Long.class, args.toArray());
        args.add(Math.max(pageSize, 1));
        args.add(Math.max(pageNum - 1, 0) * Math.max(pageSize, 1));
        String sql = """
            select s.id, s.category_id categoryId, c.name categoryName, s.name, s.avatar_url avatarUrl,
                   s.gender, s.age, s.city, s.district, s.education, s.experience_years experienceYears,
                   s.salary_min salaryMin, s.salary_max salaryMax, s.salary_unit salaryUnit,
                   s.native_place nativePlace, s.height_cm heightCm, s.weight_kg weightKg,
                   s.birth_date birthDate, s.marital_status maritalStatus,
                   s.status, s.recommended, s.sort_no sortNo, s.created_at createdAt, s.updated_at updatedAt
            from service_staff s
            left join service_category c on c.id = s.category_id
            where
            """ + where + "\n" + """
            order by s.sort_no asc, s.id desc
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
                   s.self_intro selfIntro, s.skills, s.verification_note verificationNote,
                   s.status, s.recommended, s.sort_no sortNo, s.created_at createdAt, s.updated_at updatedAt
            from service_staff s
            left join service_category c on c.id = s.category_id
            where s.id = ? and s.deleted = 0
            """, id);
        staff.put("tags", childRows("select id, tag_name tagName from staff_tag where staff_id = ? and deleted = 0 order by id asc", id));
        staff.put("certificates", childRows("select id, certificate_name certificateName, file_url fileUrl, sort_no sortNo from staff_certificate where staff_id = ? and deleted = 0 order by sort_no asc, id asc", id));
        staff.put("photos", childRows("select id, photo_url photoUrl, sort_no sortNo from staff_photo where staff_id = ? and deleted = 0 order by sort_no asc, id asc", id));
        staff.put("experiences", childRows("select id, start_date startDate, end_date endDate, description from staff_work_experience where staff_id = ? and deleted = 0 order by start_date desc, id desc", id));
        return R.ok(staff);
    }

    @PostMapping
    public R<Long> create(@RequestBody StaffRequest request) {
        jdbcTemplate.update("""
            insert into service_staff(category_id, name, avatar_url, gender, age, city, district, education,
                experience_years, salary_min, salary_max, salary_unit, service_desc, native_place, height_cm,
                weight_kg, birth_date, marital_status, self_intro, skills, verification_note,
                status, recommended, sort_no,
                created_at, updated_at, deleted)
            values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), now(), 0)
            """, request.categoryId(), request.name(), request.avatarUrl(), request.gender(), request.age(),
            request.city(), request.district(), request.education(), request.experienceYears(), request.salaryMin(),
            request.salaryMax(), request.salaryUnit(), request.serviceDesc(), request.nativePlace(), request.heightCm(),
            request.weightKg(), request.birthDate(), request.maritalStatus(), request.selfIntro(), request.skills(),
            request.verificationNote(), request.statusValue(), request.recommendedValue(), request.sortNoValue());
        Long id = jdbcTemplate.queryForObject("select last_insert_id()", Long.class);
        replaceTags(id, request.tags());
        replaceCertificates(id, request.certificates());
        replacePhotos(id, request.photos());
        replaceExperiences(id, request.experiences());
        return R.ok(id);
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody StaffRequest request) {
        jdbcTemplate.update("""
            update service_staff
            set category_id = ?, name = ?, avatar_url = ?, gender = ?, age = ?, city = ?, district = ?,
                education = ?, experience_years = ?, salary_min = ?, salary_max = ?, salary_unit = ?,
                service_desc = ?, native_place = ?, height_cm = ?, weight_kg = ?, birth_date = ?,
                marital_status = ?, self_intro = ?, skills = ?, verification_note = ?,
                status = ?, recommended = ?, sort_no = ?, updated_at = now()
            where id = ? and deleted = 0
            """, request.categoryId(), request.name(), request.avatarUrl(), request.gender(), request.age(),
            request.city(), request.district(), request.education(), request.experienceYears(), request.salaryMin(),
            request.salaryMax(), request.salaryUnit(), request.serviceDesc(), request.nativePlace(), request.heightCm(),
            request.weightKg(), request.birthDate(), request.maritalStatus(), request.selfIntro(), request.skills(),
            request.verificationNote(), request.statusValue(), request.recommendedValue(), request.sortNoValue(), id);
        replaceTags(id, request.tags());
        replaceCertificates(id, request.certificates());
        replacePhotos(id, request.photos());
        replaceExperiences(id, request.experiences());
        return R.ok();
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String status = stringValue(body.get("status"), "DRAFT");
        Integer recommended = body.containsKey("recommended") ? boolValue(body.get("recommended")) : null;
        if (recommended == null) {
            jdbcTemplate.update("update service_staff set status = ?, updated_at = now() where id = ? and deleted = 0", status, id);
        } else {
            jdbcTemplate.update("update service_staff set status = ?, recommended = ?, updated_at = now() where id = ? and deleted = 0", status, recommended, id);
        }
        return R.ok();
    }

    @PostMapping("/{id}/certificates")
    public R<Long> addCertificate(@PathVariable Long id, @RequestBody CertificateRequest request) {
        jdbcTemplate.update("""
            insert into staff_certificate(staff_id, certificate_name, file_url, sort_no, created_at, updated_at, deleted)
            values(?, ?, ?, ?, now(), now(), 0)
            """, id, request.certificateName(), request.fileUrl(), request.sortNoValue());
        return R.ok(jdbcTemplate.queryForObject("select last_insert_id()", Long.class));
    }

    @PostMapping("/{id}/photos")
    public R<Long> addPhoto(@PathVariable Long id, @RequestBody PhotoRequest request) {
        jdbcTemplate.update("""
            insert into staff_photo(staff_id, photo_url, sort_no, created_at, updated_at, deleted)
            values(?, ?, ?, now(), now(), 0)
            """, id, request.photoUrl(), request.sortNoValue());
        return R.ok(jdbcTemplate.queryForObject("select last_insert_id()", Long.class));
    }

    @PostMapping("/{id}/experiences")
    public R<Long> addExperience(@PathVariable Long id, @RequestBody ExperienceRequest request) {
        jdbcTemplate.update("""
            insert into staff_work_experience(staff_id, start_date, end_date, description, created_at, updated_at, deleted)
            values(?, ?, ?, ?, now(), now(), 0)
            """, id, request.startDate(), request.endDate(), request.description());
        return R.ok(jdbcTemplate.queryForObject("select last_insert_id()", Long.class));
    }

    private static String buildWhere(Long categoryId, String status, String keyword, List<Object> args) {
        StringBuilder where = new StringBuilder("s.deleted = 0");
        if (categoryId != null) {
            where.append(" and s.category_id = ?");
            args.add(categoryId);
        }
        if (status != null && !status.isBlank()) {
            where.append(" and s.status = ?");
            args.add(status.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            where.append(" and (s.name like ? or s.city like ? or s.district like ?)");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
            args.add(like);
        }
        return where.toString();
    }

    private List<Map<String, Object>> childRows(String sql, Long id) {
        return jdbcTemplate.queryForList(sql, id);
    }

    private void replaceTags(Long staffId, List<String> tags) {
        jdbcTemplate.update("update staff_tag set deleted = 1, updated_at = now() where staff_id = ?", staffId);
        if (tags == null) {
            return;
        }
        for (String tag : tags) {
            if (tag != null && !tag.isBlank()) {
                jdbcTemplate.update("insert into staff_tag(staff_id, tag_name, created_at, updated_at, deleted) values(?, ?, now(), now(), 0)", staffId, tag.trim());
            }
        }
    }

    private void replaceCertificates(Long staffId, List<CertificateRequest> certificates) {
        if (certificates == null) {
            return;
        }
        jdbcTemplate.update("update staff_certificate set deleted = 1, updated_at = now() where staff_id = ?", staffId);
        for (CertificateRequest certificate : certificates) {
            if (certificate.certificateName() != null && !certificate.certificateName().isBlank()) {
                jdbcTemplate.update("""
                    insert into staff_certificate(staff_id, certificate_name, file_url, sort_no, created_at, updated_at, deleted)
                    values(?, ?, ?, ?, now(), now(), 0)
                    """, staffId, certificate.certificateName().trim(), certificate.fileUrl(), certificate.sortNoValue());
            }
        }
    }

    private void replacePhotos(Long staffId, List<PhotoRequest> photos) {
        if (photos == null) {
            return;
        }
        jdbcTemplate.update("update staff_photo set deleted = 1, updated_at = now() where staff_id = ?", staffId);
        for (PhotoRequest photo : photos) {
            if (photo.photoUrl() != null && !photo.photoUrl().isBlank()) {
                jdbcTemplate.update("""
                    insert into staff_photo(staff_id, photo_url, sort_no, created_at, updated_at, deleted)
                    values(?, ?, ?, now(), now(), 0)
                    """, staffId, photo.photoUrl().trim(), photo.sortNoValue());
            }
        }
    }

    private void replaceExperiences(Long staffId, List<ExperienceRequest> experiences) {
        if (experiences == null) {
            return;
        }
        jdbcTemplate.update("update staff_work_experience set deleted = 1, updated_at = now() where staff_id = ?", staffId);
        for (ExperienceRequest experience : experiences) {
            if (experience.description() != null && !experience.description().isBlank()) {
                jdbcTemplate.update("""
                    insert into staff_work_experience(staff_id, start_date, end_date, description, created_at, updated_at, deleted)
                    values(?, ?, ?, ?, now(), now(), 0)
                    """, staffId, experience.startDate(), experience.endDate(), experience.description().trim());
            }
        }
    }

    private static String stringValue(Object value, String defaultValue) {
        return value == null || value.toString().isBlank() ? defaultValue : value.toString().trim();
    }

    private static int boolValue(Object value) {
        if (value instanceof Boolean bool) {
            return bool ? 1 : 0;
        }
        if (value instanceof Number number) {
            return number.intValue() == 0 ? 0 : 1;
        }
        return Boolean.parseBoolean(String.valueOf(value)) ? 1 : 0;
    }

    public record StaffRequest(
        Long categoryId,
        String name,
        String avatarUrl,
        String gender,
        Integer age,
        String city,
        String district,
        String education,
        Integer experienceYears,
        BigDecimal salaryMin,
        BigDecimal salaryMax,
        String salaryUnit,
        String serviceDesc,
        String nativePlace,
        Integer heightCm,
        Integer weightKg,
        String birthDate,
        String maritalStatus,
        String selfIntro,
        String skills,
        String verificationNote,
        String status,
        Boolean recommended,
        Integer sortNo,
        List<String> tags,
        List<CertificateRequest> certificates,
        List<PhotoRequest> photos,
        List<ExperienceRequest> experiences
    ) {
        String statusValue() {
            return status == null || status.isBlank() ? "DRAFT" : status.trim();
        }

        int recommendedValue() {
            return recommended != null && recommended ? 1 : 0;
        }

        int sortNoValue() {
            return sortNo == null ? 0 : sortNo;
        }
    }

    public record CertificateRequest(String certificateName, String fileUrl, Integer sortNo) {
        int sortNoValue() {
            return sortNo == null ? 0 : sortNo;
        }
    }

    public record PhotoRequest(String photoUrl, Integer sortNo) {
        int sortNoValue() {
            return sortNo == null ? 0 : sortNo;
        }
    }

    public record ExperienceRequest(String startDate, String endDate, String description) {
    }
}
