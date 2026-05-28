package org.dromara.web.controller.admin;

import org.dromara.common.core.domain.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台鉴权连通性检查接口。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminPingController {

    @GetMapping("/ping")
    public R<String> ping() {
        return R.ok("ok");
    }
}
