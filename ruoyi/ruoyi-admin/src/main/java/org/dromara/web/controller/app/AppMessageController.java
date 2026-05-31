package org.dromara.web.controller.app;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.web.service.SystemMessageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/app/messages")
public class AppMessageController {

    private final SystemMessageService systemMessageService;

    @GetMapping
    public TableDataInfo<Map<String, Object>> list(
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "20") int pageSize
    ) {
        return systemMessageService.listForUser(LoginHelper.getUserId(), pageNum, pageSize);
    }

    @PostMapping("/{id}/read")
    public R<Void> read(@PathVariable Long id) {
        return systemMessageService.markRead(LoginHelper.getUserId(), id)
            ? R.ok()
            : R.fail("消息不存在");
    }
}
