package org.dromara.web.controller.app;

import cn.dev33.satoken.annotation.SaIgnore;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.web.service.ContentConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@SaIgnore
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/app/content")
public class AppContentController {

    private final ContentConfigService contentConfigService;

    @GetMapping("/{type}")
    public R<List<Map<String, Object>>> list(@PathVariable String type) {
        return R.ok(contentConfigService.listEnabled(toContentType(type)));
    }

    private static String toContentType(String type) {
        return switch (type) {
            case "faq" -> "FAQ";
            case "agreement" -> "AGREEMENT";
            case "privacy" -> "PRIVACY";
            case "about" -> "ABOUT";
            default -> type == null ? "" : type.toUpperCase();
        };
    }
}
