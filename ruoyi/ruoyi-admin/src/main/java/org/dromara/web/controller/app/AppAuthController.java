package org.dromara.web.controller.app;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.domain.model.LoginUser;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SaIgnore
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/app/auth")
public class AppAuthController {

    private static final String APP_CLIENT_ID = "app";

    private final JdbcTemplate jdbcTemplate;

    @PostMapping("/mock-login")
    public R<AppLoginVo> mockLogin(@RequestBody(required = false) AppLoginRequest request) {
        String openid = request != null && request.getOpenid() != null && !request.getOpenid().isBlank()
            ? request.getOpenid().trim()
            : "mock-openid";
        String nickname = request != null && request.getNickname() != null && !request.getNickname().isBlank()
            ? request.getNickname().trim()
            : "小程序用户";

        Long userId = findOrCreateUser(openid, nickname, true);

        LoginUser loginUser = new LoginUser();
        loginUser.setTenantId("000000");
        loginUser.setUserId(userId);
        loginUser.setUserType("app");
        loginUser.setUsername(openid);
        loginUser.setNickname(nickname);
        loginUser.setClientKey(APP_CLIENT_ID);
        loginUser.setDeviceType("xcx");

        SaLoginParameter model = new SaLoginParameter();
        model.setDeviceType("xcx");
        model.setExtra(LoginHelper.CLIENT_KEY, APP_CLIENT_ID);
        LoginHelper.login(loginUser, model);

        AppLoginVo vo = new AppLoginVo();
        vo.setUserId(userId);
        vo.setOpenid(openid);
        vo.setNickname(nickname);
        vo.setAccessToken(StpUtil.getTokenValue());
        vo.setExpireIn(StpUtil.getTokenTimeout());
        vo.setClientId(APP_CLIENT_ID);
        return R.ok(vo);
    }

    @PostMapping("/phone-login")
    public R<AppLoginVo> phoneLogin(@RequestBody(required = false) AppPhoneLoginRequest request) {
        String phone = request != null && request.getPhone() != null && !request.getPhone().isBlank()
            ? request.getPhone().trim()
            : "13800000000";
        String openid = "phone-" + phone;
        String nickname = "手机号用户" + phone.substring(Math.max(0, phone.length() - 4));

        Long userId = findOrCreateUser(openid, nickname, false);
        jdbcTemplate.update(
            "update app_user set phone = ?, updated_at = now() where id = ?",
            phone, userId
        );
        String currentNickname = jdbcTemplate.queryForObject(
            "select nickname from app_user where id = ? and deleted = 0",
            String.class,
            userId
        );
        nickname = currentNickname != null && !currentNickname.isBlank() ? currentNickname : nickname;

        LoginUser loginUser = new LoginUser();
        loginUser.setTenantId("000000");
        loginUser.setUserId(userId);
        loginUser.setUserType("app");
        loginUser.setUsername(openid);
        loginUser.setNickname(nickname);
        loginUser.setClientKey(APP_CLIENT_ID);
        loginUser.setDeviceType("xcx");

        SaLoginParameter model = new SaLoginParameter();
        model.setDeviceType("xcx");
        model.setExtra(LoginHelper.CLIENT_KEY, APP_CLIENT_ID);
        LoginHelper.login(loginUser, model);

        AppLoginVo vo = new AppLoginVo();
        vo.setUserId(userId);
        vo.setOpenid(openid);
        vo.setNickname(nickname);
        vo.setPhone(phone);
        vo.setAccessToken(StpUtil.getTokenValue());
        vo.setExpireIn(StpUtil.getTokenTimeout());
        vo.setClientId(APP_CLIENT_ID);
        return R.ok(vo);
    }

    private Long findOrCreateUser(String openid, String nickname, boolean refreshNickname) {
        Long userId = jdbcTemplate.query(
            "select id from app_user where openid = ? and deleted = 0 limit 1",
            rs -> rs.next() ? rs.getLong("id") : null,
            openid
        );
        if (userId != null) {
            if (refreshNickname) {
                jdbcTemplate.update(
                    "update app_user set nickname = ?, updated_at = now() where id = ?",
                    nickname, userId
                );
            }
            return userId;
        }
        jdbcTemplate.update(
            "insert into app_user(openid, nickname, status, created_at, updated_at, deleted) values(?, ?, 'ENABLED', now(), now(), 0)",
            openid, nickname
        );
        return jdbcTemplate.queryForObject(
            "select id from app_user where openid = ? and deleted = 0 limit 1",
            Long.class,
            openid
        );
    }

    @Data
    public static class AppLoginRequest {
        private String openid;
        private String nickname;
    }

    @Data
    public static class AppPhoneLoginRequest {
        private String phone;
        private String code;
    }

    @Data
    public static class AppLoginVo {
        private Long userId;
        private String openid;
        private String nickname;
        private String phone;
        private String accessToken;
        private long expireIn;
        private String clientId;
    }
}
