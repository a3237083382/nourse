package org.dromara.web.controller.app;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.lang.Dict;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.domain.model.LoginUser;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@SaIgnore
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/app/auth")
public class AppAuthController {

    private static final String APP_CLIENT_ID = "app";
    private static final String WECHAT_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";
    private static final String WECHAT_PHONE_URL = "https://api.weixin.qq.com/wxa/business/getuserphonenumber";
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private final JdbcTemplate jdbcTemplate;

    @Value("${app.wechat.app-id:}")
    private String wechatAppId;

    @Value("${app.wechat.app-secret:}")
    private String wechatAppSecret;

    private volatile String cachedWechatAccessToken;
    private volatile long cachedWechatAccessTokenExpireAt;

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
        String phone;
        try {
            phone = resolvePhone(request);
        } catch (IllegalStateException e) {
            return R.fail(e.getMessage());
        }
        if (StringUtils.isBlank(phone)) {
            return R.fail("未获取到微信授权手机号，请重新授权");
        }
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

    private String resolvePhone(AppPhoneLoginRequest request) {
        if (request == null) {
            return null;
        }
        if (StringUtils.isNotBlank(request.getPhone())) {
            return request.getPhone().trim();
        }
        if (StringUtils.isBlank(request.getCode())) {
            return null;
        }
        if (StringUtils.isBlank(wechatAppId) || StringUtils.isBlank(wechatAppSecret)) {
            throw new IllegalStateException("未配置微信小程序 AppID/AppSecret，不能用测试手机号兜底");
        }
        return requestWechatPhone(request.getCode().trim());
    }

    private String requestWechatPhone(String code) {
        String accessToken = getWechatAccessToken();
        String body = JsonUtils.toJsonString(Map.of("code", code));
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(WECHAT_PHONE_URL + "?access_token=" + encode(accessToken)))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
            .build();
        Dict response = sendWechatRequest(request);
        Object errcodeValue = response.get("errcode");
        Number errcode = errcodeValue instanceof Number number ? number : null;
        if (errcode != null && errcode.intValue() != 0) {
            throw new IllegalStateException("微信手机号换取失败: " + response.getStr("errmsg"));
        }
        Object phoneInfoObject = response.get("phone_info");
        if (!(phoneInfoObject instanceof Map<?, ?> phoneInfo)) {
            return null;
        }
        Object phoneNumber = phoneInfo.get("phoneNumber");
        if (phoneNumber == null) {
            phoneNumber = phoneInfo.get("purePhoneNumber");
        }
        return phoneNumber == null ? null : phoneNumber.toString();
    }

    private String getWechatAccessToken() {
        long now = System.currentTimeMillis();
        if (StringUtils.isNotBlank(cachedWechatAccessToken) && now < cachedWechatAccessTokenExpireAt) {
            return cachedWechatAccessToken;
        }
        String url = WECHAT_ACCESS_TOKEN_URL
            + "?grant_type=client_credential"
            + "&appid=" + encode(wechatAppId)
            + "&secret=" + encode(wechatAppSecret);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();
        Dict response = sendWechatRequest(request);
        String accessToken = response.getStr("access_token");
        if (StringUtils.isBlank(accessToken)) {
            throw new IllegalStateException("微信 access_token 获取失败: " + response.getStr("errmsg"));
        }
        Object expiresInValue = response.get("expires_in");
        Number expiresIn = expiresInValue instanceof Number number ? number : null;
        long safeExpiresIn = expiresIn == null ? 3600L : Math.max(60L, expiresIn.longValue() - 60L);
        cachedWechatAccessToken = accessToken;
        cachedWechatAccessTokenExpireAt = now + safeExpiresIn * 1000L;
        return accessToken;
    }

    private Dict sendWechatRequest(HttpRequest request) {
        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return JsonUtils.parseMap(response.body());
        } catch (IOException e) {
            throw new IllegalStateException("微信接口调用失败", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("微信接口调用被中断", e);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
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
