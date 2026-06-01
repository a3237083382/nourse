package org.dromara.web.controller.app;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/app/contracts")
public class AppContractController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public TableDataInfo<Map<String, Object>> list(
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "10") int pageSize
    ) {
        Long userId = LoginHelper.getUserId();
        String where = "c.user_id = ? and c.deleted = 0";
        Object[] countArgs;
        Object[] listArgs;
        if (status != null && !status.isBlank()) {
            where += " and c.status = ?";
            countArgs = new Object[] { userId, status.trim() };
            listArgs = new Object[] { userId, status.trim(), Math.max(pageSize, 1), Math.max(pageNum - 1, 0) * Math.max(pageSize, 1) };
        } else {
            countArgs = new Object[] { userId };
            listArgs = new Object[] { userId, Math.max(pageSize, 1), Math.max(pageNum - 1, 0) * Math.max(pageSize, 1) };
        }
        Long total = jdbcTemplate.queryForObject("select count(1) from contract c where " + where, Long.class, countArgs);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
            select c.id, c.contract_no contractNo, c.title, c.file_url fileUrl, c.status,
                   c.signed_at signedAt, c.terminated_at terminatedAt, c.created_at createdAt,
                   s.name staffName, sc.name categoryName
            from contract c
            left join service_staff s on s.id = c.staff_id
            left join service_category sc on sc.id = s.category_id
            where
            """ + where + "\n" + """
            order by c.id desc
            limit ? offset ?
            """, listArgs);
        return new TableDataInfo<>(rows, total == null ? 0 : total);
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        Long userId = LoginHelper.getUserId();
        Map<String, Object> contract = jdbcTemplate.queryForMap("""
            select c.id, c.contract_no contractNo, c.title, c.file_url fileUrl, c.status,
                   c.signed_at signedAt, c.terminated_at terminatedAt, c.created_at createdAt,
                   s.name staffName, sc.name categoryName, d.title demandTitle, o.order_no orderNo
            from contract c
            left join service_staff s on s.id = c.staff_id
            left join service_category sc on sc.id = s.category_id
            left join user_demand d on d.id = c.demand_id
            left join service_order o on o.id = c.service_order_id
            where c.id = ? and c.user_id = ? and c.deleted = 0
            """, id, userId);
        return R.ok(contract);
    }

    @GetMapping(value = "/{id}/preview-image", produces = MediaType.IMAGE_PNG_VALUE)
    public void previewImage(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Long userId = LoginHelper.getUserId();
        Map<String, Object> contract;
        try {
            contract = jdbcTemplate.queryForMap("""
                select c.contract_no contractNo, c.title, c.status, c.signed_at signedAt,
                       s.name staffName, sc.name categoryName, d.title demandTitle, o.order_no orderNo
                from contract c
                left join service_staff s on s.id = c.staff_id
                left join service_category sc on sc.id = s.category_id
                left join user_demand d on d.id = c.demand_id
                left join service_order o on o.id = c.service_order_id
                where c.id = ? and c.user_id = ? and c.deleted = 0
                """, id, userId);
        } catch (EmptyResultDataAccessException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        BufferedImage image = new BufferedImage(900, 1280, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(250, 248, 244));
            g.fillRect(0, 0, 900, 1280);
            g.setColor(Color.WHITE);
            g.fillRoundRect(70, 70, 760, 1140, 28, 28);
            g.setColor(new Color(222, 216, 207));
            g.setStroke(new BasicStroke(3));
            g.drawRoundRect(70, 70, 760, 1140, 28, 28);

            Font titleFont = new Font("Microsoft YaHei", Font.BOLD, 42);
            Font labelFont = new Font("Microsoft YaHei", Font.BOLD, 25);
            Font valueFont = new Font("Microsoft YaHei", Font.PLAIN, 25);
            Font sealFont = new Font("Microsoft YaHei", Font.BOLD, 32);

            g.setColor(new Color(32, 36, 44));
            g.setFont(titleFont);
            drawCentered(g, text(contract, "title", "家政服务合同"), 450, 160);

            g.setFont(valueFont);
            g.setColor(new Color(103, 113, 122));
            drawCentered(g, "电子合同预览件", 450, 215);

            int y = 300;
            y = drawField(g, labelFont, valueFont, "合同编号", text(contract, "contractNo", "-"), y);
            y = drawField(g, labelFont, valueFont, "服务人员", text(contract, "staffName", "-"), y);
            y = drawField(g, labelFont, valueFont, "服务类型", text(contract, "categoryName", "-"), y);
            y = drawField(g, labelFont, valueFont, "关联需求", text(contract, "demandTitle", "-"), y);
            y = drawField(g, labelFont, valueFont, "关联订单", text(contract, "orderNo", "-"), y);
            y = drawField(g, labelFont, valueFont, "合同状态", "SIGNED".equals(text(contract, "status", "")) ? "已签署" : "已终止", y);
            drawField(g, labelFont, valueFont, "签署时间", text(contract, "signedAt", "-"), y);

            g.setColor(new Color(239, 243, 246));
            g.fillRoundRect(130, 810, 640, 180, 20, 20);
            g.setColor(new Color(103, 113, 122));
            g.setFont(valueFont);
            drawCentered(g, "本图片用于本地测试预览。正式环境请上传", 450, 875);
            drawCentered(g, "PDF、图片或拍照扫描的纸质合同文件。", 450, 920);

            g.setColor(new Color(178, 31, 58));
            g.setStroke(new BasicStroke(5));
            g.drawOval(610, 965, 145, 145);
            g.setFont(sealFont);
            drawCentered(g, "已签署", 682, 1048);
        } finally {
            g.dispose();
        }

        response.setContentType(MediaType.IMAGE_PNG_VALUE);
        response.setHeader("Cache-Control", "no-store");
        ImageIO.write(image, "png", response.getOutputStream());
    }

    private static String text(Map<String, Object> map, String key, String fallback) {
        Object value = map.get(key);
        return value == null || value.toString().isBlank() ? fallback : value.toString();
    }

    private static int drawField(Graphics2D g, Font labelFont, Font valueFont, String label, String value, int y) {
        g.setFont(labelFont);
        g.setColor(new Color(32, 36, 44));
        g.drawString(label + "：", 145, y);
        g.setFont(valueFont);
        g.setColor(new Color(65, 72, 82));
        drawWrapped(g, value, 290, y, 470, 40);
        return y + Math.max(72, ((value.length() / 18) + 1) * 42);
    }

    private static void drawCentered(Graphics2D g, String text, int centerX, int y) {
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(text, centerX - metrics.stringWidth(text) / 2, y);
    }

    private static void drawWrapped(Graphics2D g, String text, int x, int y, int maxWidth, int lineHeight) {
        FontMetrics metrics = g.getFontMetrics();
        StringBuilder line = new StringBuilder();
        int lineY = y;
        for (int i = 0; i < text.length(); i++) {
            String next = line + text.substring(i, i + 1);
            if (metrics.stringWidth(next) > maxWidth && !line.isEmpty()) {
                g.drawString(line.toString(), x, lineY);
                line = new StringBuilder(text.substring(i, i + 1));
                lineY += lineHeight;
            } else {
                line.append(text.charAt(i));
            }
        }
        if (!line.isEmpty()) {
            g.drawString(line.toString(), x, lineY);
        }
    }
}
