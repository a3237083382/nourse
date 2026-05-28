package org.dromara.web.controller.app;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 小程序用户接口。
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/app/user")
public class AppUserController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/profile")
    public R<Map<String, Object>> profile() {
        Long userId = LoginHelper.getUserId();
        Map<String, Object> profile = jdbcTemplate.queryForMap(
            "select id, openid, unionid, nickname, avatar_url, phone, status from app_user where id = ? and deleted = 0",
            userId
        );
        return R.ok(profile);
    }
}
