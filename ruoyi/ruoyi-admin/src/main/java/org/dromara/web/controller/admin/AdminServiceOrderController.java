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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/service-orders")
public class AdminServiceOrderController {

    private static final Set<String> STATUSES = Set.of("WAIT_START", "SERVING", "COMPLETED", "CANCELED");

    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public TableDataInfo<Map<String, Object>> list(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "10") int pageSize
    ) {
        List<Object> args = new ArrayList<>();
        String where = "o.deleted = 0";
        if (status != null && !status.isBlank()) {
            where += " and o.status = ?";
            args.add(status.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            where += " and (o.order_no like ? or u.nickname like ? or s.name like ?)";
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
            args.add(like);
        }
        Long total = jdbcTemplate.queryForObject("select count(1) from service_order o left join app_user u on u.id = o.user_id left join service_staff s on s.id = o.staff_id where " + where, Long.class, args.toArray());
        args.add(Math.max(pageSize, 1));
        args.add(Math.max(pageNum - 1, 0) * Math.max(pageSize, 1));
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
            select o.id, o.order_no orderNo, o.amount, o.start_date startDate, o.end_date endDate,
                   o.status, o.created_at createdAt, u.nickname userNickname, u.phone userPhone,
                   s.name staffName, c.name categoryName
            from service_order o
            left join app_user u on u.id = o.user_id
            left join service_staff s on s.id = o.staff_id
            left join service_category c on c.id = o.category_id
            where
            """ + where + "\n" + """
            order by o.id desc
            limit ? offset ?
            """, args.toArray());
        return new TableDataInfo<>(rows, total == null ? 0 : total);
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        return R.ok(jdbcTemplate.queryForMap("""
            select o.id, o.order_no orderNo, o.user_id userId, o.staff_id staffId,
                   o.demand_id demandId, o.category_id categoryId, o.amount, o.start_date startDate,
                   o.end_date endDate, o.status, o.admin_note adminNote, o.created_at createdAt,
                   u.nickname userNickname, s.name staffName, c.name categoryName
            from service_order o
            left join app_user u on u.id = o.user_id
            left join service_staff s on s.id = o.staff_id
            left join service_category c on c.id = o.category_id
            where o.id = ? and o.deleted = 0
            """, id));
    }

    @PostMapping
    public R<Long> create(@RequestBody ServiceOrderRequest request) {
        if (request == null || request.userId() == null || request.staffId() == null || request.categoryId() == null) {
            return R.fail("请完整填写用户、服务人员和服务类型");
        }
        String status = statusValue(request.status());
        if (status == null) {
            return R.fail("订单状态不正确");
        }
        jdbcTemplate.update("""
            insert into service_order(order_no, user_id, staff_id, demand_id, category_id, amount, start_date,
                end_date, status, admin_note, created_at, updated_at, deleted)
            values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), now(), 0)
            """, isBlank(request.orderNo()) ? orderNo() : request.orderNo().trim(), request.userId(), request.staffId(),
            request.demandId(), request.categoryId(), amount(request.amount()), request.startDate(), request.endDate(),
            status, request.adminNote());
        return R.ok(jdbcTemplate.queryForObject("select last_insert_id()", Long.class));
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody ServiceOrderRequest request) {
        if (request == null || request.userId() == null || request.staffId() == null || request.categoryId() == null) {
            return R.fail("请完整填写用户、服务人员和服务类型");
        }
        String status = statusValue(request.status());
        if (status == null) {
            return R.fail("订单状态不正确");
        }
        jdbcTemplate.update("""
            update service_order
            set order_no = ?, user_id = ?, staff_id = ?, demand_id = ?, category_id = ?, amount = ?,
                start_date = ?, end_date = ?, status = ?, admin_note = ?, updated_at = now()
            where id = ? and deleted = 0
            """, isBlank(request.orderNo()) ? orderNo() : request.orderNo().trim(), request.userId(), request.staffId(),
            request.demandId(), request.categoryId(), amount(request.amount()), request.startDate(), request.endDate(),
            status, request.adminNote(), id);
        return R.ok();
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        String status = request == null ? null : statusValue(request.status());
        if (status == null) {
            return R.fail("订单状态不正确");
        }
        jdbcTemplate.update("update service_order set status = ?, updated_at = now() where id = ? and deleted = 0", status, id);
        return R.ok();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static BigDecimal amount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private static String statusValue(String status) {
        String value = isBlank(status) ? "WAIT_START" : status.trim();
        return STATUSES.contains(value) ? value : null;
    }

    private static String orderNo() {
        return "SO-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

    public record ServiceOrderRequest(
        String orderNo,
        Long userId,
        Long staffId,
        Long demandId,
        Long categoryId,
        BigDecimal amount,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        String adminNote
    ) {
    }

    public record StatusRequest(String status) {
    }
}
