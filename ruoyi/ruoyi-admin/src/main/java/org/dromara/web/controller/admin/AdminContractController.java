package org.dromara.web.controller.admin;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.web.service.SystemMessageService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/contracts")
public class AdminContractController {

    private final JdbcTemplate jdbcTemplate;
    private final SystemMessageService systemMessageService;

    @GetMapping
    public TableDataInfo<Map<String, Object>> list(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "10") int pageSize
    ) {
        String where = "c.deleted = 0";
        List<Object> args = new java.util.ArrayList<>();
        if (status != null && !status.isBlank()) {
            where += " and c.status = ?";
            args.add(status.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            where += " and (c.contract_no like ? or c.title like ? or u.nickname like ? or s.name like ?)";
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
            args.add(like);
            args.add(like);
        }
        Long total = jdbcTemplate.queryForObject("select count(1) from contract c left join app_user u on u.id = c.user_id left join service_staff s on s.id = c.staff_id where " + where, Long.class, args.toArray());
        args.add(Math.max(pageSize, 1));
        args.add(Math.max(pageNum - 1, 0) * Math.max(pageSize, 1));
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
            select c.id, c.contract_no contractNo, c.title, c.file_url fileUrl, c.status,
                   c.signed_at signedAt, c.terminated_at terminatedAt, c.created_at createdAt,
                   u.nickname userNickname, u.phone userPhone, s.name staffName
            from contract c
            left join app_user u on u.id = c.user_id
            left join service_staff s on s.id = c.staff_id
            where
            """ + where + "\n" + """
            order by c.id desc
            limit ? offset ?
            """, args.toArray());
        return new TableDataInfo<>(rows, total == null ? 0 : total);
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        return R.ok(jdbcTemplate.queryForMap("""
            select c.id, c.user_id userId, c.staff_id staffId, c.demand_id demandId,
                   c.service_order_id serviceOrderId, c.contract_no contractNo, c.title,
                   c.file_url fileUrl, c.status, c.signed_at signedAt, c.terminated_at terminatedAt,
                   c.created_at createdAt, u.nickname userNickname, s.name staffName
            from contract c
            left join app_user u on u.id = c.user_id
            left join service_staff s on s.id = c.staff_id
            where c.id = ? and c.deleted = 0
            """, id));
    }

    @PostMapping
    public R<Long> create(@RequestBody ContractRequest request) {
        if (request == null || request.userId() == null || request.staffId() == null
            || isBlank(request.contractNo()) || isBlank(request.title()) || isBlank(request.fileUrl())) {
            return R.fail("请完整填写用户、服务人员、合同编号、标题和合同文件");
        }
        String status = isBlank(request.status()) ? "SIGNED" : request.status().trim();
        jdbcTemplate.update("""
            insert into contract(user_id, staff_id, demand_id, service_order_id, contract_no, title,
                file_url, status, signed_at, terminated_at, created_at, updated_at, deleted)
            values(?, ?, ?, ?, ?, ?, ?, ?, if(? = 'SIGNED', now(), null), if(? = 'TERMINATED', now(), null), now(), now(), 0)
            """, request.userId(), request.staffId(), request.demandId(), request.serviceOrderId(),
            request.contractNo().trim(), request.title().trim(), request.fileUrl().trim(),
            status, status, status);
        Long id = jdbcTemplate.queryForObject("select last_insert_id()", Long.class);
        systemMessageService.createContractCreated(request.userId(), request.title().trim());
        return R.ok(id);
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody ContractRequest request) {
        if (request == null || request.userId() == null || request.staffId() == null
            || isBlank(request.contractNo()) || isBlank(request.title()) || isBlank(request.fileUrl())) {
            return R.fail("请完整填写用户、服务人员、合同编号、标题和合同文件");
        }
        String status = isBlank(request.status()) ? "SIGNED" : request.status().trim();
        jdbcTemplate.update("""
            update contract
            set user_id = ?, staff_id = ?, demand_id = ?, service_order_id = ?, contract_no = ?,
                title = ?, file_url = ?, status = ?,
                signed_at = if(? = 'SIGNED', coalesce(signed_at, now()), signed_at),
                terminated_at = if(? = 'TERMINATED', coalesce(terminated_at, now()), null),
                updated_at = now()
            where id = ? and deleted = 0
            """, request.userId(), request.staffId(), request.demandId(), request.serviceOrderId(),
            request.contractNo().trim(), request.title().trim(), request.fileUrl().trim(),
            status, status, status, id);
        return R.ok();
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        if (request == null || isBlank(request.status())) {
            return R.fail("请选择合同状态");
        }
        String status = request.status().trim();
        jdbcTemplate.update("""
            update contract
            set status = ?, signed_at = if(? = 'SIGNED', coalesce(signed_at, now()), signed_at),
                terminated_at = if(? = 'TERMINATED', coalesce(terminated_at, now()), null),
                updated_at = now()
            where id = ? and deleted = 0
            """, status, status, status, id);
        return R.ok();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record ContractRequest(
        Long userId,
        Long staffId,
        Long demandId,
        Long serviceOrderId,
        String contractNo,
        String title,
        String fileUrl,
        String status
    ) {
    }

    public record StatusRequest(String status) {
    }
}
