package org.dromara.web.service;

import lombok.RequiredArgsConstructor;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class SystemMessageService {

    private final JdbcTemplate jdbcTemplate;

    public void createForDemand(Long demandId, String title, String content, String messageType) {
        List<Long> userIds = jdbcTemplate.queryForList("""
            select user_id
            from user_demand
            where id = ? and deleted = 0
            limit 1
            """, Long.class, demandId);
        if (userIds.isEmpty()) {
            return;
        }
        createForUser(userIds.get(0), title, content, messageType);
    }

    public void createContractCreated(Long userId, String contractTitle) {
        createForUser(
            userId,
            "合同已创建",
            "你的合同「" + contractTitle + "」已创建，可在合同页查看。",
            "CONTRACT_CREATED"
        );
    }

    public void createGroupSuccess(Long groupTeamId, String productTitle) {
        List<Long> userIds = jdbcTemplate.queryForList("""
            select distinct user_id
            from group_order
            where group_team_id = ? and deleted = 0
            """, Long.class, groupTeamId);
        for (Long userId : userIds) {
            createForUser(
                userId,
                "拼团成功",
                "你参与的「" + productTitle + "」拼团已成功，可在团购订单中查看。",
                "GROUP_SUCCESS"
            );
        }
    }

    public TableDataInfo<Map<String, Object>> listForUser(Long userId, int pageNum, int pageSize) {
        int safePageSize = Math.max(pageSize, 1);
        int offset = Math.max(pageNum - 1, 0) * safePageSize;
        Long total = jdbcTemplate.queryForObject("""
            select count(1)
            from system_message
            where user_id = ? and deleted = 0
            """, Long.class, userId);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
            select id, title, content, message_type messageType, read_flag readFlag,
                   created_at createdAt, updated_at updatedAt
            from system_message
            where user_id = ? and deleted = 0
            order by read_flag asc, id desc
            limit ? offset ?
            """, userId, safePageSize, offset);
        return new TableDataInfo<>(rows, total == null ? 0 : total);
    }

    public boolean markRead(Long userId, Long messageId) {
        int updated = jdbcTemplate.update("""
            update system_message
            set read_flag = 1, updated_at = now()
            where id = ? and user_id = ? and deleted = 0
            """, messageId, userId);
        return updated > 0;
    }

    private void createForUser(Long userId, String title, String content, String messageType) {
        jdbcTemplate.update("""
            insert into system_message(user_id, title, content, message_type, read_flag, created_at, updated_at, deleted)
            values(?, ?, ?, ?, 0, now(), now(), 0)
            """, userId, title, content, messageType);
    }
}
