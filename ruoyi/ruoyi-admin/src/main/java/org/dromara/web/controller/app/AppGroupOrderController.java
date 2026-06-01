package org.dromara.web.controller.app;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.web.service.SystemMessageService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/app/group-orders")
public class AppGroupOrderController {

    private final JdbcTemplate jdbcTemplate;
    private final SystemMessageService systemMessageService;

    @PostMapping("/single")
    @Transactional(rollbackFor = Exception.class)
    public R<Long> single(@RequestBody ProductOrderRequest request) {
        if (request == null || request.productId() == null) {
            return R.fail("请选择团购商品");
        }
        Long userId = LoginHelper.getUserId();
        Product product = product(request.productId());
        int quantity = quantity(request.quantity());
        String orderNo = orderNo("GO-S");
        jdbcTemplate.update("""
            insert into group_order(order_no, user_id, product_id, group_team_id, buy_type, quantity,
                amount, status, paid_at, valid_until, created_at, updated_at, deleted)
            values(?, ?, ?, null, 'SINGLE', ?, ?, 'WAIT_USE', now(), date_add(now(), interval ? day), now(), now(), 0)
            """, orderNo, userId, product.id(), quantity, product.singlePrice().multiply(BigDecimal.valueOf(quantity)), product.validDays());
        jdbcTemplate.update("update group_product set sold_count = sold_count + ?, updated_at = now() where id = ?", quantity, product.id());
        return R.ok(lastId());
    }

    @PostMapping("/group/start")
    @Transactional(rollbackFor = Exception.class)
    public R<Long> startGroup(@RequestBody ProductOrderRequest request) {
        if (request == null || request.productId() == null) {
            return R.fail("请选择团购商品");
        }
        Long userId = LoginHelper.getUserId();
        Product product = product(request.productId());
        int quantity = quantity(request.quantity());
        jdbcTemplate.update("""
            insert into group_team(product_id, leader_user_id, group_size, joined_count, expire_at, status, created_at, updated_at, deleted)
            values(?, ?, ?, 1, date_add(now(), interval 1 day), 'GROUPING', now(), now(), 0)
            """, product.id(), userId, product.groupSize());
        Long teamId = lastId();
        jdbcTemplate.update("""
            insert into group_order(order_no, user_id, product_id, group_team_id, buy_type, quantity,
                amount, status, paid_at, valid_until, created_at, updated_at, deleted)
            values(?, ?, ?, ?, 'GROUP', ?, ?, 'WAIT_SHARE', now(), date_add(now(), interval ? day), now(), now(), 0)
            """, orderNo("GO-G"), userId, product.id(), teamId, quantity, product.groupPrice().multiply(BigDecimal.valueOf(quantity)), product.validDays());
        jdbcTemplate.update("update group_product set sold_count = sold_count + ?, updated_at = now() where id = ?", quantity, product.id());
        return R.ok(lastId());
    }

    @PostMapping("/group/join")
    @Transactional(rollbackFor = Exception.class)
    public R<Long> joinGroup(@RequestBody JoinOrderRequest request) {
        if (request == null || request.teamId() == null) {
            return R.fail("请选择要加入的拼团");
        }
        Long userId = LoginHelper.getUserId();
        Map<String, Object> team = jdbcTemplate.queryForMap("""
            select t.id, t.product_id productId, t.group_size groupSize, t.joined_count joinedCount,
                   t.status, p.title productTitle, p.group_price groupPrice, p.valid_days validDays
            from group_team t
            inner join group_product p on p.id = t.product_id
            where t.id = ? and t.deleted = 0 and p.deleted = 0 and p.status = 'ONLINE'
            for update
            """, request.teamId());
        if (!"GROUPING".equals(team.get("status")) || ((Number) team.get("joinedCount")).intValue() >= ((Number) team.get("groupSize")).intValue()) {
            return R.fail("该拼团已满或不可加入");
        }
        Long productId = ((Number) team.get("productId")).longValue();
        int quantity = quantity(request.quantity());
        BigDecimal amount = ((BigDecimal) team.get("groupPrice")).multiply(BigDecimal.valueOf(quantity));
        jdbcTemplate.update("""
            insert into group_order(order_no, user_id, product_id, group_team_id, buy_type, quantity,
                amount, status, paid_at, valid_until, created_at, updated_at, deleted)
            values(?, ?, ?, ?, 'GROUP', ?, ?, 'WAIT_SHARE', now(), date_add(now(), interval ? day), now(), now(), 0)
            """, orderNo("GO-J"), userId, productId, request.teamId(), quantity, amount, ((Number) team.get("validDays")).intValue());
        Long orderId = lastId();
        int joinedCount = ((Number) team.get("joinedCount")).intValue() + 1;
        String status = joinedCount >= ((Number) team.get("groupSize")).intValue() ? "SUCCESS" : "GROUPING";
        jdbcTemplate.update("""
            update group_team set joined_count = ?, status = ?, updated_at = now()
            where id = ? and deleted = 0
            """, joinedCount, status, request.teamId());
        if ("SUCCESS".equals(status)) {
            jdbcTemplate.update("""
                update group_order set status = 'WAIT_USE', updated_at = now()
                where group_team_id = ? and deleted = 0
                """, request.teamId());
            systemMessageService.createGroupSuccess(request.teamId(), String.valueOf(team.get("productTitle")));
        }
        jdbcTemplate.update("update group_product set sold_count = sold_count + ?, updated_at = now() where id = ?", quantity, productId);
        return R.ok(orderId);
    }

    @PostMapping("/{id}/mock-pay")
    public R<Void> mockPay(@PathVariable Long id) {
        Long userId = LoginHelper.getUserId();
        int updated = jdbcTemplate.update("""
            update group_order set paid_at = now(), updated_at = now()
            where id = ? and user_id = ? and deleted = 0
            """, id, userId);
        return updated > 0 ? R.ok() : R.fail("订单不存在");
    }

    @GetMapping
    public TableDataInfo<Map<String, Object>> list(
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "10") int pageSize
    ) {
        Long userId = LoginHelper.getUserId();
        String where = "o.user_id = ? and o.deleted = 0";
        Object[] countArgs;
        Object[] listArgs;
        if (status != null && !status.isBlank()) {
            where += " and o.status = ?";
            countArgs = new Object[] { userId, status.trim() };
            listArgs = new Object[] { userId, status.trim(), Math.max(pageSize, 1), Math.max(pageNum - 1, 0) * Math.max(pageSize, 1) };
        } else {
            countArgs = new Object[] { userId };
            listArgs = new Object[] { userId, Math.max(pageSize, 1), Math.max(pageNum - 1, 0) * Math.max(pageSize, 1) };
        }
        Long total = jdbcTemplate.queryForObject("select count(1) from group_order o where " + where, Long.class, countArgs);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
            select o.id, o.order_no orderNo, o.buy_type buyType, o.quantity, o.amount, o.status,
                   o.group_team_id groupTeamId, o.valid_until validUntil, o.created_at createdAt,
                   p.title productTitle, p.cover_url coverUrl, t.status teamStatus,
                   t.group_size groupSize, t.joined_count joinedCount,
                   r.id reviewId, r.rating reviewRating, r.content reviewContent
            from group_order o
            left join group_product p on p.id = o.product_id
            left join group_team t on t.id = o.group_team_id
            left join group_order_review r on r.group_order_id = o.id and r.deleted = 0
            where
            """ + where + "\n" + """
            order by o.id desc
            limit ? offset ?
            """, listArgs);
        return new TableDataInfo<>(rows, total == null ? 0 : total);
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        Long userId = LoginHelper.getUserId();
        Map<String, Object> order = jdbcTemplate.queryForMap("""
            select o.id, o.order_no orderNo, o.buy_type buyType, o.quantity, o.amount, o.status,
                   o.group_team_id groupTeamId, o.paid_at paidAt, o.valid_until validUntil, o.created_at createdAt,
                   p.title productTitle, p.cover_url coverUrl, p.notice, p.guarantee,
                   t.status teamStatus, t.group_size groupSize, t.joined_count joinedCount, t.expire_at expireAt,
                   r.id reviewId, r.rating reviewRating, r.content reviewContent
            from group_order o
            left join group_product p on p.id = o.product_id
            left join group_team t on t.id = o.group_team_id
            left join group_order_review r on r.group_order_id = o.id and r.deleted = 0
            where o.id = ? and o.user_id = ? and o.deleted = 0
            """, id, userId);
        return R.ok(order);
    }

    @PostMapping("/{id}/review")
    @Transactional(rollbackFor = Exception.class)
    public R<Void> review(@PathVariable Long id, @RequestBody ReviewRequest request) {
        if (request == null || request.rating() == null || request.rating() < 1 || request.rating() > 5) {
            return R.fail("请选择 1-5 分评价");
        }
        Long userId = LoginHelper.getUserId();
        Map<String, Object> order = jdbcTemplate.queryForMap("""
            select id, product_id productId, status
            from group_order
            where id = ? and user_id = ? and deleted = 0
            """, id, userId);
        if (!"USED".equals(order.get("status"))) {
            return R.fail("团购服务使用完成后才可以评价");
        }
        Long exists = jdbcTemplate.queryForObject("""
            select count(1) from group_order_review
            where group_order_id = ? and deleted = 0
            """, Long.class, id);
        if (exists != null && exists > 0) {
            return R.fail("该团购订单已评价");
        }
        jdbcTemplate.update("""
            insert into group_order_review(group_order_id, user_id, product_id, rating, content, created_at, updated_at, deleted)
            values(?, ?, ?, ?, ?, now(), now(), 0)
            """, id, userId, ((Number) order.get("productId")).longValue(), request.rating(), cleanContent(request.content()));
        return R.ok();
    }

    private Product product(Long productId) {
        Map<String, Object> row = jdbcTemplate.queryForMap("""
            select id, single_price singlePrice, group_price groupPrice, group_size groupSize, valid_days validDays
            from group_product
            where id = ? and deleted = 0 and status = 'ONLINE'
            """, productId);
        return new Product(
            ((Number) row.get("id")).longValue(),
            (BigDecimal) row.get("singlePrice"),
            (BigDecimal) row.get("groupPrice"),
            ((Number) row.get("groupSize")).intValue(),
            ((Number) row.get("validDays")).intValue()
        );
    }

    private Long lastId() {
        return jdbcTemplate.queryForObject("select last_insert_id()", Long.class);
    }

    private static int quantity(Integer quantity) {
        return quantity == null || quantity < 1 ? 1 : quantity;
    }

    private static String orderNo(String prefix) {
        return prefix + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

    private static String cleanContent(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        String text = content.trim();
        return text.length() > 512 ? text.substring(0, 512) : text;
    }

    private record Product(Long id, BigDecimal singlePrice, BigDecimal groupPrice, int groupSize, int validDays) {
    }

    public record ProductOrderRequest(Long productId, Integer quantity) {
    }

    public record JoinOrderRequest(Long teamId, Integer quantity) {
    }

    public record ReviewRequest(Integer rating, String content) {
    }
}
