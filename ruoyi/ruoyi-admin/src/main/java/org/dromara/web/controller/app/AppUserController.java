package org.dromara.web.controller.app;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
            "select id, openid, unionid, nickname, avatar_url avatarUrl, phone, status from app_user where id = ? and deleted = 0",
            userId
        );
        return R.ok(profile);
    }

    @PutMapping("/profile")
    public R<Map<String, Object>> updateProfile(@RequestBody ProfileUpdateRequest request) {
        Long userId = LoginHelper.getUserId();
        String nickname = request.nickname() == null ? "" : request.nickname().trim();
        String avatarUrl = request.avatarUrl() == null ? "" : request.avatarUrl().trim();
        if (nickname.isBlank()) {
            return R.fail("昵称不能为空");
        }
        jdbcTemplate.update(
            "update app_user set nickname = ?, avatar_url = ?, updated_at = now() where id = ? and deleted = 0",
            nickname, avatarUrl, userId
        );
        return profile();
    }

    public record ProfileUpdateRequest(String nickname, String avatarUrl) {
    }
}
