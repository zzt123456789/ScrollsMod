package com.zzt.eternal_abyss.font;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class VoidArtifactFontRenderer extends FontRenderer {

    // =========================================================
    // 文本常量
    // =========================================================

    // ===== 物品名 =====
    private static final String NAME_VOID_ARTIFACT = "永恒深渊之庇佑";

    // ===== 顶部描述 =====
    private static final String DESC1 = "深渊将永远庇护至高之人";

    // ===== 提示 =====
    private static final String HOLD_KEY = "按住";
    private static final String SHIFT_KEY = "Shift";
    private static final String ALT_KEY = "Alt";

    // ===== Shift 内容关键词 =====
    private static final String KEY_IMMUNE = "免疫";
    private static final String KEY_IFRAME = "无敌帧";
    private static final String KEY_FLY = "高速飞行";
    private static final String KEY_REMOVE = "移除";
    private static final String KEY_OVERWORLD = "主世界";
    private static final String KEY_NETHER = "地狱";
    private static final String KEY_END = "末地";
    private static final String KEY_DIM = "其他维度";
    private static final String KEY_INVASION = "万法不侵";
    private static final String KEY_TARGET = "攻击目标";

    // ===== Alt 行原文（用于逐字显示时的半截匹配） =====
    private static final String ALT_LINE_1 = "你踏尽深渊，彼岸花终在尽头为你盛开";
    private static final String ALT_LINE_2 = "回望来路，那一片猩红，是对你苦旅的最高赞叹";

    // =========================================================
    // 枚举
    // =========================================================
    private enum VoidStyle {
        NONE,
        NAME,
        HEADER,
        HINT,
        SHIFT_BODY,
        ALT_BODY
    }

    private enum AltLineStyle {
        NONE,
        LINE_1,
        LINE_2
    }

    public VoidArtifactFontRenderer(GameSettings gameSettings,
                                    ResourceLocation location,
                                    TextureManager textureManager,
                                    boolean unicode) {
        super(gameSettings, location, textureManager, unicode);
    }

    @Override
    public int drawString(String text, float x, float y, int color, boolean dropShadow) {
        return drawVoidString(text, x, y, color, dropShadow);
    }

    @Override
    public int drawString(String text, int x, int y, int color) {
        return drawVoidString(text, x, y, color, false);
    }

    @Override
    public int drawStringWithShadow(String text, float x, float y, int color) {
        return drawVoidString(text, x, y, color, true);
    }

    @Override
    public int getStringWidth(String text) {
        if (text == null) return 0;
        return super.getStringWidth(text);
    }

    private int drawVoidString(String text, float x, float y, int color, boolean dropShadow) {
        if (text == null || text.isEmpty()) return 0;

        long time = System.currentTimeMillis();
        VoidStyle style = resolveStyle(text);

        switch (style) {
            case NAME:
                return drawName(text, x, y, color, dropShadow, time);

            case HEADER:
                return drawHeader(text, x, y, color, dropShadow, time);

            case HINT:
                return drawHint(text, x, y, color, dropShadow, time);

            case ALT_BODY: {
                AltLineStyle altLineStyle = resolveAltLineStyle(text);
                return drawAltBody(text, x, y, color, dropShadow, altLineStyle, time);
            }

            case SHIFT_BODY:
                return drawShiftBody(text, x, y, color, dropShadow, time);

            default:
                return super.drawString(text, x, y, color, dropShadow);
        }
    }

    // =========================================================
    // 风格判定
    // =========================================================
    private VoidStyle resolveStyle(String text) {
        String plain = normalizeText(text);
        if (plain.isEmpty()) return VoidStyle.NONE;

        if (matchesPartialOrFull(plain, normalizeText(NAME_VOID_ARTIFACT))) {
            return VoidStyle.NAME;
        }

        if (matchesPartialOrFull(plain, normalizeText(DESC1))) {
            return VoidStyle.HEADER;
        }

        if (plain.contains(normalizeText(HOLD_KEY))
                && (plain.contains(normalizeText(SHIFT_KEY)) || plain.contains(normalizeText(ALT_KEY)))) {
            return VoidStyle.HINT;
        }

        if (resolveAltLineStyle(text) != AltLineStyle.NONE) {
            return VoidStyle.ALT_BODY;
        }

        if (containsAny(plain,
                normalizeText(KEY_IMMUNE),
                normalizeText(KEY_IFRAME),
                normalizeText(KEY_FLY),
                normalizeText(KEY_REMOVE),
                normalizeText(KEY_OVERWORLD),
                normalizeText(KEY_NETHER),
                normalizeText(KEY_END),
                normalizeText(KEY_DIM),
                normalizeText(KEY_INVASION),
                normalizeText(KEY_TARGET),
                normalizeText("火焰"),
                normalizeText("岩浆"),
                normalizeText("爆炸"),
                normalizeText("窒息"),
                normalizeText("溺水"),
                normalizeText("坠落"),
                normalizeText("虚空"),
                normalizeText("仙人掌"),
                normalizeText("负面效果"),
                normalizeText("蜘蛛网"),
                normalizeText("水下呼吸"),
                normalizeText("再生"),
                normalizeText("幸运"),
                normalizeText("饱和"),
                normalizeText("抗性"),
                normalizeText("夜视"),
                normalizeText("急迫"),
                normalizeText("发光"),
                normalizeText("免伤"))) {
            return VoidStyle.SHIFT_BODY;
        }

        return VoidStyle.NONE;
    }

    private AltLineStyle resolveAltLineStyle(String text) {
        String plain = normalizeText(text);
        if (plain.isEmpty()) return AltLineStyle.NONE;

        if (matchesPartialOrFull(plain, normalizeText(ALT_LINE_1))) return AltLineStyle.LINE_1;
        if (matchesPartialOrFull(plain, normalizeText(ALT_LINE_2))) return AltLineStyle.LINE_2;

        return AltLineStyle.NONE;
    }

    // =========================================================
    // 名称渲染
    // =========================================================
    private int drawName(String text, float x, float y, int baseColor, boolean dropShadow, long time) {
        float startX = x;
        String plain = stripFormatting(text);
        int totalWidth = super.getStringWidth(plain);

        int currentColorFromCode = baseColor;
        boolean bold = false;
        int visibleIndex = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '\u00A7' && i + 1 < text.length()) {
                char code = Character.toLowerCase(text.charAt(i + 1));
                int colorIndex = "0123456789abcdef".indexOf(code);

                if (colorIndex >= 0) {
                    currentColorFromCode = getColorCode(code);
                    bold = false;
                    i++;
                    continue;
                }

                switch (code) {
                    case 'l':
                        bold = true;
                        i++;
                        continue;
                    case 'r':
                        currentColorFromCode = baseColor;
                        bold = false;
                        i++;
                        continue;
                    default:
                        i++;
                        continue;
                }
            }

            float progress = totalWidth <= 0 ? 0F : (x - startX) / (float) totalWidth;
            float shift = (time % 3200L) / 3200.0F;
            float t = (progress + shift) % 1.0F;

            int renderColor = gradient3(
                    t,
                    currentColorFromCode,
                    0xFF1A1522,
                    0xFFF1E3AE,
                    0xFFA18BFF,
                    0.42F, 0.62F, 0.40F
            );

            renderColor = applyPulse(renderColor, time + visibleIndex * 11L, 0.10F, 760.0);
            renderColor = applySweepHighlight(renderColor, progress, shift, 0.10F, 0.22F);

            float offsetY = (float) Math.sin(time / 760.0D + visibleIndex * 0.14D) * 0.22F;
            float offsetX = (float) Math.cos(time / 1250.0D + visibleIndex * 0.11D) * 0.06F;

            int glow = withAlpha(lerpColor(renderColor, 0xFFFFFFFF, 0.15F), 28);
            int ghost = withAlpha(0xFF8E73F2, 18);

            super.drawString(String.valueOf(c), x + offsetX, y + offsetY - 0.2F, glow, false);
            super.drawString(String.valueOf(c), x - 0.25F + offsetX, y + offsetY + 0.15F, ghost, false);

            if (dropShadow) {
                int shadow = darkenColor(renderColor, 0.26F);
                super.drawString(String.valueOf(c), x + offsetX + 0.8F, y + offsetY + 0.8F, shadow, false);
            }

            super.drawString(String.valueOf(c), x + offsetX, y + offsetY, renderColor, false);

            if (bold) {
                super.drawString(String.valueOf(c), x + offsetX + 1.0F, y + offsetY, renderColor, false);
            }

            x += getCharWidth(c) + (bold ? 1 : 0);
            visibleIndex++;
        }

        return (int) (x - startX);
    }

    // =========================================================
    // 顶部描述
    // =========================================================
    private int drawHeader(String text, float x, float y, int baseColor, boolean dropShadow, long time) {
        float startX = x;
        String plain = stripFormatting(text);
        int totalWidth = super.getStringWidth(plain);

        int currentColorFromCode = baseColor;
        boolean bold = false;
        int visibleIndex = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '\u00A7' && i + 1 < text.length()) {
                char code = Character.toLowerCase(text.charAt(i + 1));
                int colorIndex = "0123456789abcdef".indexOf(code);

                if (colorIndex >= 0) {
                    currentColorFromCode = getColorCode(code);
                    bold = false;
                    i++;
                    continue;
                }

                switch (code) {
                    case 'l':
                        bold = true;
                        i++;
                        continue;
                    case 'r':
                        currentColorFromCode = baseColor;
                        bold = false;
                        i++;
                        continue;
                    default:
                        i++;
                        continue;
                }
            }

            float progress = totalWidth <= 0 ? 0F : (x - startX) / (float) totalWidth;
            float shift = (time % 3400L) / 3400.0F;
            float t = (progress + shift) % 1.0F;

            int renderColor = gradient3(
                    t,
                    currentColorFromCode,
                    0xFF17141D,
                    0xFFEADBA2,
                    0xFF9B7CFF,
                    0.48F, 0.68F, 0.46F
            );

            renderColor = applyPulse(renderColor, time + visibleIndex * 17L, 0.11F, 640.0);
            renderColor = applySweepHighlight(renderColor, progress, shift, 0.09F, 0.22F);

            float offsetY = (float) Math.sin(time / 860.0D + visibleIndex * 0.12D) * 0.14F;

            int glow = withAlpha(lerpColor(renderColor, 0xFFFFFFFF, 0.12F), 24);
            super.drawString(String.valueOf(c), x, y + offsetY - 0.15F, glow, false);

            if (((time / 150L) + visibleIndex) % 21 == 0) {
                int flash = withAlpha(0xFFFFFFFF, 18);
                super.drawString(String.valueOf(c), x + 0.15F, y + offsetY - 0.25F, flash, false);
            }

            if (dropShadow) {
                int shadow = darkenColor(renderColor, 0.28F);
                super.drawString(String.valueOf(c), x + 0.8F, y + offsetY + 0.8F, shadow, false);
            }

            super.drawString(String.valueOf(c), x, y + offsetY, renderColor, false);

            if (bold) {
                super.drawString(String.valueOf(c), x + 1.0F, y + offsetY, renderColor, false);
            }

            x += getCharWidth(c) + (bold ? 1 : 0);
            visibleIndex++;
        }

        return (int) (x - startX);
    }

    // =========================================================
    // Shift 正文：朴素版
    // =========================================================
    private int drawShiftBody(String text, float x, float y, int baseColor, boolean dropShadow, long time) {
        float startX = x;
        String plain = stripFormatting(text);
        int totalWidth = super.getStringWidth(plain);

        int currentColorFromCode = baseColor;
        boolean bold = false;
        int visibleIndex = 0;
        String normalized = normalizeText(text);

        boolean hasNumber = normalized.contains("66%")
                || normalized.contains("33%")
                || normalized.contains("11%")
                || normalized.contains("15%");

        boolean hasDimension = containsAny(normalized,
                normalizeText(KEY_OVERWORLD),
                normalizeText(KEY_NETHER),
                normalizeText(KEY_END),
                normalizeText(KEY_DIM));

        boolean hasPowerWord = containsAny(normalized,
                normalizeText(KEY_INVASION),
                normalizeText("负面效果"),
                normalizeText("高速飞行"),
                normalizeText("免疫"));

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '\u00A7' && i + 1 < text.length()) {
                char code = Character.toLowerCase(text.charAt(i + 1));
                int colorIndex = "0123456789abcdef".indexOf(code);

                if (colorIndex >= 0) {
                    currentColorFromCode = getColorCode(code);
                    bold = false;
                    i++;
                    continue;
                }

                switch (code) {
                    case 'l':
                        bold = true;
                        i++;
                        continue;
                    case 'r':
                        currentColorFromCode = baseColor;
                        bold = false;
                        i++;
                        continue;
                    default:
                        i++;
                        continue;
                }
            }

            float progress = totalWidth <= 0 ? 0F : (x - startX) / (float) totalWidth;
            float shift = (time % 5200L) / 5200.0F;
            float t = (progress + shift) % 1.0F;

            int left = 0xFF2B3040;
            int mid = 0xFFAEB9D6;
            int right = 0xFF747F9B;

            if (hasDimension) {
                mid = 0xFFD3C28E;
                right = 0xFF8B84B3;
            }
            if (hasPowerWord) {
                mid = 0xFFE0D6A8;
            }

            int renderColor = gradient3(
                    t,
                    currentColorFromCode,
                    left, mid, right,
                    0.16F, 0.24F, 0.16F
            );

            renderColor = applyPulse(renderColor, time + visibleIndex * 5L, 0.025F, 1800.0);
            renderColor = applySweepHighlight(renderColor, progress, shift, 0.06F, 0.06F);

            float offsetY = (float) Math.sin(time / 2600.0D + visibleIndex * 0.05D) * 0.025F;
            float offsetX = 0F;

            if (Character.isDigit(c) || c == '%') {
                renderColor = lerpColor(renderColor, 0xFFFFFFFF, hasNumber ? 0.20F : 0.10F);
            }

            if (dropShadow) {
                int shadow = darkenColor(renderColor, 0.34F);
                super.drawString(String.valueOf(c), x + offsetX + 0.65F, y + offsetY + 0.65F, shadow, false);
            }

            super.drawString(String.valueOf(c), x + offsetX, y + offsetY, renderColor, false);

            if (bold) {
                super.drawString(String.valueOf(c), x + offsetX + 1.0F, y + offsetY, renderColor, false);
            }

            x += getCharWidth(c) + (bold ? 1 : 0);
            visibleIndex++;
        }

        return (int) (x - startX);
    }

    // =========================================================
    // Alt 正文：夸张版
    // =========================================================
    private int drawAltBody(String text, float x, float y, int baseColor, boolean dropShadow, AltLineStyle style, long time) {
        float startX = x;

        String plain = stripFormatting(text);
        int totalWidth = super.getStringWidth(plain);

        int currentColorFromCode = baseColor;
        boolean bold = false;
        int visibleIndex = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '\u00A7' && i + 1 < text.length()) {
                char code = Character.toLowerCase(text.charAt(i + 1));
                int colorIndex = "0123456789abcdef".indexOf(code);

                if (colorIndex >= 0) {
                    currentColorFromCode = this.getColorCode(code);
                    bold = false;
                    i++;
                    continue;
                }

                switch (code) {
                    case 'l':
                        bold = true;
                        i++;
                        continue;
                    case 'r':
                        currentColorFromCode = baseColor;
                        bold = false;
                        i++;
                        continue;
                    default:
                        i++;
                        continue;
                }
            }

            float progress = totalWidth <= 0 ? 0F : (x - startX) / (float) totalWidth;
            int renderColor = getAltColor(style, progress, time, currentColorFromCode, visibleIndex);

            float offsetX = getAltOffsetX(style, time, visibleIndex);
            float offsetY = getAltOffsetY(style, time, visibleIndex);

            drawAltExtraLayers(style, c, x, y, renderColor, time, visibleIndex, progress);

            if (dropShadow) {
                int shadow = darkenColor(renderColor, getAltShadowFactor(style));
                super.drawString(String.valueOf(c), x + offsetX + 0.75F, y + offsetY + 0.75F, shadow, false);
            }

            super.drawString(String.valueOf(c), x + offsetX, y + offsetY, renderColor, false);

            if (bold) {
                super.drawString(String.valueOf(c), x + offsetX + 1.0F, y + offsetY, renderColor, false);
            }

            x += this.getCharWidth(c) + (bold ? 1 : 0);
            visibleIndex++;
        }

        return (int) (x - startX);
    }

    private void drawAltExtraLayers(AltLineStyle style, char c, float x, float y, int color, long time, int visibleIndex, float progress) {
        float lift = getAltLift(style, time, visibleIndex);
        float side = getAltMicroSide(style, time, visibleIndex);

        switch (style) {
            case LINE_1: {
                int softWhite = withAlpha(0xFFFFF4F4, 24);   // 微暖白
                int dimPurple = withAlpha(0xFF7A1022, 18);   // 血红残影
                int dimGold = withAlpha(0xFF3A0A0F, 12);     // 深渊暗红底影

                super.drawString(String.valueOf(c), x + side * 0.25F, y - 0.40F + lift, softWhite, false);
                super.drawString(String.valueOf(c), x - 0.30F + side, y + 0.08F + lift * 0.35F, dimPurple, false);
                super.drawString(String.valueOf(c), x + 0.20F - side * 0.5F, y + 0.14F, dimGold, false);

                if (((time / 160L) + visibleIndex * 3) % 29 == 0) {
                    int flash = withAlpha(0xFFFFFFFF, 34);
                    super.drawString(String.valueOf(c), x + 0.10F, y - 0.55F + lift, flash, false);
                }

                if (c == '，' || c == '。' || c == '「' || c == '」') {
                    int rune = withAlpha(0xFFFF8080, 20);
                    super.drawString(String.valueOf(c), x, y - 0.18F + lift * 0.6F, rune, false);
                }
                break;
            }

            case LINE_2: {
                int softWhite = withAlpha(0xFFFFF1F1, 26);   // 柔白
                int dimPurple = withAlpha(0xFF68101C, 20);   // 暗血红拖影
                int dimGold = withAlpha(0xFF2A070B, 14);     // 深渊阴影

                super.drawString(String.valueOf(c), x, y - 0.46F + lift, softWhite, false);
                super.drawString(String.valueOf(c), x - 0.34F + side, y + 0.08F + lift * 0.30F, dimPurple, false);
                super.drawString(String.valueOf(c), x + 0.18F - side * 0.4F, y + 0.16F - lift * 0.20F, dimGold, false);

                if (((time / 130L) + visibleIndex * 2) % 21 == 0) {
                    int flash = withAlpha(0xFFFFFFFF, 38);
                    super.drawString(String.valueOf(c), x + 0.18F, y - 0.62F + lift, flash, false);
                }

                if (c == '，' || c == '。' || c == '「' || c == '」') {
                    int rune = withAlpha(0xFFFF6A6A, 22);
                    super.drawString(String.valueOf(c), x + 0.04F, y - 0.22F + lift * 0.55F, rune, false);
                }
                break;
            }

            default: {
                int softWhite = withAlpha(0xFFFFFFFF, 22);
                super.drawString(String.valueOf(c), x, y - 0.30F, softWhite, false);
                break;
            }
        }
    }

    private int getAltColor(AltLineStyle style, float progress, long time, int baseColor, int visibleIndex) {
        int whiteBase = lerpColor(baseColor, 0xFFFFFFFF, 0.94F);

        float shiftA;
        float shiftB;
        float distA;
        float distB;

        switch (style) {
            case LINE_1: {
                shiftA = (time % 5200L) / 5200.0F;
                shiftB = 1.0F - shiftA;

                int color = gradient3(
                        progress,
                        whiteBase,
                        0xFFFFEAEA,
                        0xFFFF4A4A,
                        0xFF3A0F0F,
                        0.10F, 0.05F, 0.12F
                );

                distA = Math.abs(progress - shiftA);
                if (distA < 0.26F) {
                    float factor = 1.0F - (distA / 0.26F);
                    color = lerpColor(color, 0xFF8A0F1F, factor * 0.55F); // 深血红扫光
                }

                distB = Math.abs(progress - shiftB);
                if (distB < 0.22F) {
                    float factor = 1.0F - (distB / 0.22F);
                    color = lerpColor(color, 0xFFFF3B3B, factor * 0.32F); // 猩红高光
                }

                return applyPulse(color, time + visibleIndex * 4L, 0.035F, 2100.0);
            }

            case LINE_2: {
                shiftA = (time % 4600L) / 4600.0F;
                shiftB = 1.0F - shiftA;

                int color = gradient3(
                        progress,
                        whiteBase,
                        0xFFF5E6F8,
                        0xFFB83B5E,
                        0xFF2A0A14,
                        0.12F, 0.05F, 0.14F
                );

                distA = Math.abs(progress - shiftA);
                if (distA < 0.28F) {
                    float factor = 1.0F - (distA / 0.28F);
                    color = lerpColor(color, 0xFF6A0816, factor * 0.62F); // 更压抑的暗血红
                }

                distB = Math.abs(progress - shiftB);
                if (distB < 0.20F) {
                    float factor = 1.0F - (distB / 0.20F);
                    color = lerpColor(color, 0xFFE64545, factor * 0.26F); // 收束时的亮红
                }

                return applyPulse(color, time + visibleIndex * 5L, 0.04F, 1900.0);
            }

            default: {
                shiftA = (time % 5000L) / 5000.0F;
                shiftB = 1.0F - shiftA;

                int color = lerpColor(baseColor, 0xFFFFFFFF, 0.92F);

                distA = Math.abs(progress - shiftA);
                if (distA < 0.24F) {
                    float factor = 1.0F - (distA / 0.24F);
                    color = lerpColor(color, 0xFF625C84, factor * 0.46F);
                }

                distB = Math.abs(progress - shiftB);
                if (distB < 0.20F) {
                    float factor = 1.0F - (distB / 0.20F);
                    color = lerpColor(color, 0xFF847962, factor * 0.22F);
                }

                return applyPulse(color, time + visibleIndex * 4L, 0.035F, 2000.0);
            }
        }
    }

    private float getAltOffsetX(AltLineStyle style, long time, int visibleIndex) {
        switch (style) {
            case LINE_1:
                return (float) Math.sin(time / 1450.0D + visibleIndex * 0.22D) * 0.16F;

            case LINE_2:
                return (float) Math.cos(time / 1200.0D + visibleIndex * 0.25D) * 0.20F;

            default:
                return (float) Math.sin(time / 1500.0D + visibleIndex * 0.20D) * 0.14F;
        }
    }

    private float getAltOffsetY(AltLineStyle style, long time, int visibleIndex) {
        switch (style) {
            case LINE_1: {
                float wave = (float) Math.sin(time / 1350.0D + visibleIndex * 0.22D) * 0.85F;
                float lift = -(float) Math.abs(Math.sin(time / 2200.0D + visibleIndex * 0.10D)) * 0.95F;
                return wave + lift;
            }

            case LINE_2: {
                float wave = (float) Math.sin(time / 1100.0D + visibleIndex * 0.25D) * 1.05F;
                float lift = -(float) Math.abs(Math.sin(time / 1800.0D + visibleIndex * 0.12D)) * 1.15F;
                return wave + lift;
            }

            default: {
                float wave = (float) Math.sin(time / 1300.0D + visibleIndex * 0.22D) * 0.80F;
                float lift = -(float) Math.abs(Math.sin(time / 2100.0D + visibleIndex * 0.10D)) * 0.90F;
                return wave + lift;
            }
        }
    }

    private float getAltShadowFactor(AltLineStyle style) {
        switch (style) {
            case LINE_1: return 0.38F;
            case LINE_2: return 0.34F;
            default: return 0.38F;
        }
    }

    private float getAltLift(AltLineStyle style, long time, int visibleIndex) {
        switch (style) {
            case LINE_1:
                return -(float) Math.abs(Math.sin(time / 2000.0D + visibleIndex * 0.12D)) * 0.42F;
            case LINE_2:
                return -(float) Math.abs(Math.sin(time / 1700.0D + visibleIndex * 0.15D)) * 0.58F;
            default:
                return -(float) Math.abs(Math.sin(time / 1900.0D + visibleIndex * 0.12D)) * 0.40F;
        }
    }

    private float getAltMicroSide(AltLineStyle style, long time, int visibleIndex) {
        switch (style) {
            case LINE_1:
                return (float) Math.sin(time / 900.0D + visibleIndex * 0.30D) * 0.12F;
            case LINE_2:
                return (float) Math.cos(time / 760.0D + visibleIndex * 0.34D) * 0.15F;
            default:
                return (float) Math.sin(time / 900.0D + visibleIndex * 0.30D) * 0.10F;
        }
    }

    // =========================================================
    // 提示行
    // =========================================================
    private int drawHint(String text, float x, float y, int baseColor, boolean dropShadow, long time) {
        float startX = x;
        String plain = stripFormatting(text);
        int totalWidth = super.getStringWidth(plain);

        int currentColorFromCode = baseColor;
        boolean bold = false;
        int visibleIndex = 0;
        String normalized = normalizeText(text);

        boolean isShift = normalized.contains(normalizeText(SHIFT_KEY));
        boolean isAlt = normalized.contains(normalizeText(ALT_KEY));

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '\u00A7' && i + 1 < text.length()) {
                char code = Character.toLowerCase(text.charAt(i + 1));
                int colorIndex = "0123456789abcdef".indexOf(code);

                if (colorIndex >= 0) {
                    currentColorFromCode = getColorCode(code);
                    bold = false;
                    i++;
                    continue;
                }

                switch (code) {
                    case 'l':
                        bold = true;
                        i++;
                        continue;
                    case 'r':
                        currentColorFromCode = baseColor;
                        bold = false;
                        i++;
                        continue;
                    default:
                        i++;
                        continue;
                }
            }

            float progress = totalWidth <= 0 ? 0F : (x - startX) / (float) totalWidth;
            float shift = (time % 4400L) / 4400.0F;
            float t = (progress + shift) % 1.0F;

            int mid = isShift ? 0xFFE7CA72 : (isAlt ? 0xFFBA9AFF : 0xFFAAB8E8);

            int renderColor = gradient3(
                    t,
                    currentColorFromCode,
                    0xFF252733,
                    mid,
                    0xFF6F7FAD,
                    0.16F, 0.22F, 0.18F
            );

            renderColor = applyPulse(renderColor, time + visibleIndex * 5L, 0.02F, 1000.0);

            if (dropShadow) {
                int shadow = darkenColor(renderColor, 0.35F);
                super.drawString(String.valueOf(c), x + 0.6F, y + 0.6F, shadow, false);
            }

            super.drawString(String.valueOf(c), x, y, renderColor, false);

            if (bold) {
                super.drawString(String.valueOf(c), x + 1.0F, y, renderColor, false);
            }

            x += getCharWidth(c) + (bold ? 1 : 0);
            visibleIndex++;
        }

        return (int) (x - startX);
    }

    // =========================================================
    // 工具方法
    // =========================================================
    private boolean matchesPartialOrFull(String current, String target) {
        if (current == null || target == null) return false;
        if (current.isEmpty() || target.isEmpty()) return false;

        return target.startsWith(current)
                || current.startsWith(target)
                || current.contains(target)
                || target.contains(current);
    }

    private String stripFormatting(String text) {
        if (text == null) return "";
        return text.replaceAll("(?i)\u00A7[0-9A-FK-OR]", "");
    }

    private String normalizeText(String text) {
        if (text == null) return "";
        String plain = stripFormatting(text);
        plain = plain.replace('\u3000', ' ');
        plain = plain.replace('\u00A0', ' ');
        plain = plain.trim();
        plain = plain.replaceAll("\\s+", "");
        return plain;
    }

    private boolean containsAny(String text, String... keys) {
        if (text == null) return false;
        for (String key : keys) {
            if (key != null && !key.isEmpty() && text.contains(key)) {
                return true;
            }
        }
        return false;
    }

    private int applyPulse(int color, long time, float strength, double speed) {
        float pulse = (float) ((Math.sin(time / speed) + 1.0D) * 0.5D);
        return lerpColor(color, 0xFFFFFFFF, pulse * strength);
    }

    private int applySweepHighlight(int color, float progress, float shift, float width, float strength) {
        float dist = Math.abs(progress - shift);
        if (dist < width) {
            float factor = 1.0F - (dist / width);
            return lerpColor(color, 0xFFFFFFFF, factor * strength);
        }
        return color;
    }

    private int gradient3(float t, int baseColor,
                          int cLeft, int cMid, int cRight,
                          float leftBlend, float midBlend, float rightBlend) {
        int left = blendColor(baseColor, cLeft, leftBlend);
        int mid = blendColor(baseColor, cMid, midBlend);
        int right = blendColor(baseColor, cRight, rightBlend);

        if (t < 0.5F) {
            return lerpColor(left, mid, t / 0.5F);
        } else {
            return lerpColor(mid, right, (t - 0.5F) / 0.5F);
        }
    }

    private int blendColor(int c1, int c2, float t) {
        return lerpColor(c1, c2, clamp01(t));
    }

    private int lerpColor(int c1, int c2, float t) {
        t = clamp01(t);

        int a1 = (c1 >> 24) & 255;
        int r1 = (c1 >> 16) & 255;
        int g1 = (c1 >> 8) & 255;
        int b1 = c1 & 255;

        int a2 = (c2 >> 24) & 255;
        int r2 = (c2 >> 16) & 255;
        int g2 = (c2 >> 8) & 255;
        int b2 = c2 & 255;

        int a = (int) (a1 + (a2 - a1) * t);
        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private int darkenColor(int color, float factor) {
        factor = clamp01(factor);

        int a = (color >> 24) & 255;
        int r = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int b = color & 255;

        r = (int) (r * factor);
        g = (int) (g * factor);
        b = (int) (b * factor);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private int withAlpha(int color, int alpha) {
        alpha = Math.max(0, Math.min(255, alpha));
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    private float clamp01(float v) {
        return v < 0F ? 0F : (v > 1F ? 1F : v);
    }
}