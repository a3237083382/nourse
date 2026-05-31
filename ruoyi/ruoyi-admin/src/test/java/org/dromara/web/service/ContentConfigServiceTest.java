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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("dev")
class ContentConfigServiceTest {

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final ContentConfigService service = new ContentConfigService(jdbcTemplate);

    @Test
    void listsAdminContentByTypeWithPagination() {
        List<Map<String, Object>> rows = List.of(Map.of("id", 1L, "contentType", "FAQ"));
        when(jdbcTemplate.queryForObject(contains("count(1)"), eq(Long.class), eq("FAQ"))).thenReturn(1L);
        when(jdbcTemplate.queryForList(contains("from content_config"), eq("FAQ"), eq(10), eq(0))).thenReturn(rows);

        TableDataInfo<Map<String, Object>> result = service.listAdmin("FAQ", 1, 10);

        assertThat(result.getRows()).isEqualTo(rows);
        assertThat(result.getTotal()).isEqualTo(1L);
    }

    @Test
    void createsContentConfig() {
        ContentConfigService.ContentRequest request = new ContentConfigService.ContentRequest(
            "FAQ", "如何预约", null, "选择阿姨后提交预约。", 3, true
        );
        when(jdbcTemplate.queryForObject("select last_insert_id()", Long.class)).thenReturn(8L);

        Long id = service.create(request);

        assertThat(id).isEqualTo(8L);
        verify(jdbcTemplate).update(
            contains("insert into content_config"),
            eq("FAQ"),
            eq("如何预约"),
            eq(null),
            eq("选择阿姨后提交预约。"),
            eq(3),
            eq(1)
        );
    }

    @Test
    void returnsEnabledAppContentInDisplayOrder() {
        List<Map<String, Object>> rows = List.of(Map.of("id", 2L, "title", "用户协议"));
        when(jdbcTemplate.queryForList(contains("enabled = 1"), eq("AGREEMENT"))).thenReturn(rows);

        List<Map<String, Object>> result = service.listEnabled("AGREEMENT");

        assertThat(result).isEqualTo(rows);
    }
}
