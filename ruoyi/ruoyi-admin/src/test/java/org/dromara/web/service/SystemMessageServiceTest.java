package org.dromara.web.service;

import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("dev")
class SystemMessageServiceTest {

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final SystemMessageService service = new SystemMessageService(jdbcTemplate);

    @Test
    void createsDemandMessageForDemandOwner() {
        when(jdbcTemplate.queryForList(contains("from user_demand"), eq(Long.class), eq(12L)))
            .thenReturn(List.of(7L));

        service.createForDemand(12L, "需求审核通过", "你的需求已审核通过。", "DEMAND_APPROVED");

        verify(jdbcTemplate).update(
            contains("insert into system_message"),
            eq(7L),
            eq("需求审核通过"),
            eq("你的需求已审核通过。"),
            eq("DEMAND_APPROVED")
        );
    }

    @Test
    void skipsDemandMessageWhenDemandIsMissing() {
        when(jdbcTemplate.queryForList(contains("from user_demand"), eq(Long.class), eq(12L)))
            .thenReturn(List.of());

        service.createForDemand(12L, "需求审核通过", "你的需求已审核通过。", "DEMAND_APPROVED");

        verify(jdbcTemplate, never()).update(contains("insert into system_message"), eq(7L));
    }

    @Test
    void createsContractCreatedMessageForUser() {
        service.createContractCreated(7L, "月嫂服务合同");

        verify(jdbcTemplate).update(
            contains("insert into system_message"),
            eq(7L),
            eq("合同已创建"),
            eq("你的合同「月嫂服务合同」已创建，可在合同页查看。"),
            eq("CONTRACT_CREATED")
        );
    }

    @Test
    void createsGroupSuccessMessagesForTeamOrderUsers() {
        when(jdbcTemplate.queryForList(contains("from group_order"), eq(Long.class), eq(21L)))
            .thenReturn(List.of(7L, 8L));

        service.createGroupSuccess(21L, "深度保洁体验");

        verify(jdbcTemplate).update(
            contains("insert into system_message"),
            eq(7L),
            eq("拼团成功"),
            eq("你参与的「深度保洁体验」拼团已成功，可在团购订单中查看。"),
            eq("GROUP_SUCCESS")
        );
        verify(jdbcTemplate).update(
            contains("insert into system_message"),
            eq(8L),
            eq("拼团成功"),
            eq("你参与的「深度保洁体验」拼团已成功，可在团购订单中查看。"),
            eq("GROUP_SUCCESS")
        );
    }

    @Test
    void listsMessagesForCurrentUser() {
        List<Map<String, Object>> rows = List.of(Map.of("id", 3L, "title", "平台已推荐阿姨"));
        when(jdbcTemplate.queryForObject(contains("count(1)"), eq(Long.class), eq(7L))).thenReturn(1L);
        when(jdbcTemplate.queryForList(contains("from system_message"), eq(7L), eq(20), eq(0))).thenReturn(rows);

        TableDataInfo<Map<String, Object>> result = service.listForUser(7L, 1, 20);

        assertThat(result.getRows()).isEqualTo(rows);
        assertThat(result.getTotal()).isEqualTo(1L);
    }

    @Test
    void marksOnlyCurrentUsersMessageAsRead() {
        when(jdbcTemplate.update(contains("set read_flag = 1"), eq(99L), eq(7L))).thenReturn(1);

        boolean updated = service.markRead(7L, 99L);

        assertThat(updated).isTrue();
        verify(jdbcTemplate).update(contains("where id = ? and user_id = ?"), eq(99L), eq(7L));
    }
}
