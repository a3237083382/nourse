package org.dromara.web.controller.admin;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.web.service.ContentConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/content")
public class AdminContentController {

    private final ContentConfigService contentConfigService;

    @GetMapping
    public TableDataInfo<Map<String, Object>> list(
        @RequestParam(required = false) String contentType,
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "10") int pageSize
    ) {
        return contentConfigService.listAdmin(contentType, pageNum, pageSize);
    }

    @PostMapping
    public R<Long> create(@RequestBody ContentConfigService.ContentRequest request) {
        if (request == null || request.contentType() == null || request.contentType().isBlank()) {
            return R.fail("请选择内容类型");
        }
        return R.ok(contentConfigService.create(request));
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody ContentConfigService.ContentRequest request) {
        if (request == null || request.contentType() == null || request.contentType().isBlank()) {
            return R.fail("请选择内容类型");
        }
        contentConfigService.update(id, request);
        return R.ok();
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        contentConfigService.updateStatus(id, request != null && request.enabled());
        return R.ok();
    }

    public record StatusRequest(Boolean enabled) {
    }
}
