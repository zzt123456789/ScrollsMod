package com.zzt.eternal_abyss.font;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class DepthRingFontRenderer extends FontRenderer {

    private static final String NAME_DEPTH_RING = "深渊之戒";

    private static final String FLAVOR = "一个承载深渊意志的指环";
    private static final String DESC1 = "一旦带上，深渊的命运将永远与你纠缠";
    private static final String FOOTER = "承受深渊的压制，最终将获得深渊的庇护";

    private static final String SEALED_TEXT = "深渊已经封印";
    private static final String SEALING_TEXT = "深渊将在";

    private static final String TITLE_DAMAGE = "苦痛压制";
    private static final String TITLE_EXP = "知识侵蚀";
    private static final String TITLE_HAUNTED = "怨影缠身";
    private static final String TITLE_VOID = "虚空腐蚀";
    private static final String TITLE_LUCK = "厄运不断";
    private static final String TITLE_FINAL = "最终的挑战";

    private enum DepthStyle {
        NONE,
        NAME,
        FLAVOR,
        DESC,
        FOOTER,
        SEALED,
        SEAL_TIMER,
        CURSE_TITLE,
        CURSE_BODY,
        CURSE_PROGRESS,
        HINT
    }

    public DepthRingFontRenderer(GameSettings gameSettings,
                                 ResourceLocation location,
                                 TextureManager textureManager,
                                 boolean unicode) {
        super(gameSettings, location, textureManager, unicode);
    }

    @Override
    public int drawString(String text, float x, float y, int color, boolean dropShadow) {
        return drawDepthString(text, x, y, color, dropShadow);
    }

    @Override
    public int drawString(String text, int x, int y, int color) {
        return drawDepthString(text, x, y, color, false);
    }

    @Override
    public int drawStringWithShadow(String text, float x, float y, int color) {
        return drawDepthString(text, x, y, color, true);
    }

    private int drawDepthString(String text, float x, float y, int color, boolean dropShadow) {
        if (text == null || text.isEmpty()) return 0;

        long time = System.currentTimeMillis();
        DepthStyle style = resolveStyle(text);

        switch (style) {
            case NAME:
                return drawName(text, x, y, color, dropShadow, time);
            case FLAVOR:
            case DESC:
            case FOOTER:
                return drawSoftLine(text, x, y, color, dropShadow, time,
                        0xFF1E2638, 0xFF90AFFF, 0xFF3A4E7A,
                        0.30F, 0.42F, 0.32F,
                        0.05F, 0.10F);
            case CURSE_TITLE:
                return drawSoftLine(text, x, y, color, dropShadow, time,
                        0xFF1D2840, 0xFF9ABEFF, 0xFF4F78B8,
                        0.42F, 0.55F, 0.45F,
                        0.07F, 0.18F);
            case CURSE_BODY:
                return drawSoftLine(text, x, y, color, dropShadow, time,
                        0xFF202636, 0xFF8EA7E8, 0xFF556B9B,
                        0.20F, 0.26F, 0.22F,
                        0.02F, 0.05F);
            case CURSE_PROGRESS:
                return drawSoftLine(text, x, y, color, dropShadow, time,
                        0xFF202533, 0xFF9CB4F0, 0xFF637BA8,
                        0.24F, 0.30F, 0.24F,
                        0.02F, 0.06F);
            case SEALED:
                return drawSoftLine(text, x, y, color, dropShadow, time,
                        0xFF2A1013, 0xFFE06172, 0xFF6A2931,
                        0.42F, 0.55F, 0.45F,
                        0.06F, 0.12F);
            case SEAL_TIMER:
                return drawSoftLine(text, x, y, color, dropShadow, time,
                        0xFF2A2224, 0xFFE78E98, 0xFF7A89C8,
                        0.30F, 0.42F, 0.32F,
                        0.02F, 0.08F);
            case HINT:
                return drawSoftLine(text, x, y, color, dropShadow, time,
                        0xFF252933, 0xFFE5C96E, 0xFF7284B5,
                        0.18F, 0.22F, 0.18F,
                        0.01F, 0.04F);
            default:
                return super.drawString(text, x, y, color, dropShadow);
        }
    }

    private DepthStyle resolveStyle(String text) {
        String plain = normalizeText(text);
        if (plain.isEmpty()) return DepthStyle.NONE;

        if (nameMatches(plain, normalizeText(NAME_DEPTH_RING))) return DepthStyle.NAME;
        if (plain.contains(normalizeText(FLAVOR))) return DepthStyle.FLAVOR;
        if (plain.contains(normalizeText(DESC1))) return DepthStyle.DESC;
        if (plain.contains(normalizeText(FOOTER))) return DepthStyle.FOOTER;
        if (plain.contains(normalizeText(SEALED_TEXT))) return DepthStyle.SEALED;
        if (plain.contains(normalizeText(SEALING_TEXT))) return DepthStyle.SEAL_TIMER;

        if (plain.contains(normalizeText("按住")) &&
                (plain.contains(normalizeText("Shift")) || plain.contains(normalizeText("Alt")))) {
            return DepthStyle.HINT;
        }

        if (containsAny(plain,
                normalizeText(TITLE_DAMAGE),
                normalizeText(TITLE_EXP),
                normalizeText(TITLE_HAUNTED),
                normalizeText(TITLE_VOID),
                normalizeText(TITLE_LUCK),
                normalizeText(TITLE_FINAL))) {
            return DepthStyle.CURSE_TITLE;
        }

        if (containsAny(plain,
                normalizeText("击杀"),
                normalizeText("已击杀"),
                normalizeText("食用"),
                normalizeText("已食用"))) {
            return DepthStyle.CURSE_PROGRESS;
        }

        if (containsAny(plain,
                normalizeText("攻击伤害"),
                normalizeText("攻击速度"),
                normalizeText("经验获取"),
                normalizeText("移动速度"),
                normalizeText("怪物感知距离"),
                normalizeText("受到的伤害最少为最大生命值"),
                normalizeText("基础减伤"),
                normalizeText("幸运"),
                normalizeText("掉落物数量"),
                normalizeText("箭矢速度"),
                normalizeText("格挡概率"),
                normalizeText("伤害减免"))) {
            return DepthStyle.CURSE_BODY;
        }

        return DepthStyle.NONE;
    }

    // 名称：只做轻微流光 + 很弱浮动
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
                    0xFF161C2A,
                    0xFFA7BEFF,
                    0xFF5B78B8,
                    0.42F, 0.60F, 0.45F
            );

            renderColor = applyPulse(renderColor, time + visibleIndex * 15L, 0.08F, 520.0);
            renderColor = applySweepHighlight(renderColor, progress, shift, 0.10F, 0.20F);

            float offsetY = (float) Math.sin(time / 500.0D + visibleIndex * 0.16D) * 0.20F;

            if (dropShadow) {
                int shadow = darkenColor(renderColor, 0.25F);
                super.drawString(String.valueOf(c), x + 0.8F, y + offsetY + 0.8F, shadow, false);
            }

            // 一层很淡的辉光
            super.drawString(String.valueOf(c), x, y + offsetY - 0.15F,
                    withAlpha(lerpColor(renderColor, 0xFFFFFFFF, 0.10F), 30), false);

            super.drawString(String.valueOf(c), x, y + offsetY, renderColor, false);

            if (bold) {
                super.drawString(String.valueOf(c), x + 1.0F, y + offsetY, renderColor, false);
            }

            x += getCharWidth(c) + (bold ? 1 : 0);
            visibleIndex++;
        }

        return (int) (x - startX);
    }

    // 通用轻量线条渲染
    private int drawSoftLine(String text, float x, float y, int baseColor, boolean dropShadow, long time,
                             int leftColor, int midColor, int rightColor,
                             float leftBlend, float midBlend, float rightBlend,
                             float pulseStrength, float sweepStrength) {
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
            float shift = (time % 3600L) / 3600.0F;
            float t = (progress + shift) % 1.0F;

            int renderColor = gradient3(
                    t,
                    currentColorFromCode,
                    leftColor, midColor, rightColor,
                    leftBlend, midBlend, rightBlend
            );

            renderColor = applyPulse(renderColor, time + visibleIndex * 9L, pulseStrength, 700.0);
            renderColor = applySweepHighlight(renderColor, progress, shift, 0.08F, sweepStrength);

            if (dropShadow) {
                int shadow = darkenColor(renderColor, 0.30F);
                super.drawString(String.valueOf(c), x + 0.7F, y + 0.7F, shadow, false);
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

    private boolean nameMatches(String current, String target) {
        if (current == null || target == null) return false;
        return current.equals(target) || current.contains(target) || target.contains(current);
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