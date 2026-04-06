package com.zzt.eternal_abyss.font;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CustomFontRenderer extends FontRenderer {

    // ===== brief 文本 =====
    private static final String TWISTED_FATE =
            "「带上荆棘之冠，逆转汝之命运」";

    private static final String DIMINISHED_SHADE =
            "「沉入虚影深渊，献上不灭之心」";

    private static final String ABYSSAL_COGNITION =
            "「窥觊深渊之力，必承心智之蚀」";

    private static final String ARCANE_ANNIHILATION =
            "「灵能汇聚深渊之力，化物质为纯粹能量」";

    private static final String SORROWED_SHRIEK =
            "「劫散浮奢归于虚渊，施予纤命踏向苍穹」";

    // ===== 物品名称 =====
    private static final String NAME_TWISTED_FATE = "逆命荆棘之冠";
    private static final String NAME_DIMINISHED_SHADE = "灭蚀唤影之心";
    private static final String NAME_ABYSSAL_COGNITION = "怒渊智蚀之伤";
    private static final String NAME_ARCANE_ANNIHILATION = "奥法湮灭之戒";
    private static final String NAME_SORROWED_SHRIEK = "逸渊悲鸣之护";

    private enum BriefStyle {
        NONE,
        TWISTED_FATE,
        DIMINISHED_SHADE,
        ABYSSAL_COGNITION,
        ARCANE_ANNIHILATION,
        SORROWED_SHRIEK
    }

    public CustomFontRenderer(GameSettings gameSettings,
                              ResourceLocation location,
                              TextureManager textureManager,
                              boolean unicode) {
        super(gameSettings, location, textureManager, unicode);
    }

    @Override
    public int drawString(String text, float x, float y, int color, boolean dropShadow) {
        return drawSpecialString(text, x, y, color, dropShadow);
    }

    @Override
    public int drawString(String text, int x, int y, int color) {
        return drawSpecialString(text, x, y, color, false);
    }

    @Override
    public int drawStringWithShadow(String text, float x, float y, int color) {
        return drawSpecialString(text, x, y, color, true);
    }

    @Override
    public int getStringWidth(String text) {
        if (text == null) return 0;
        return super.getStringWidth(text);
    }

    private int drawSpecialString(String text, float x, float y, int color, boolean dropShadow) {
        if (text == null) return 0;

        long time = System.currentTimeMillis();

        // ===== 名称渲染 =====
        if (isArtifactName(text)) {
            return drawArtifactName(text, x, y, color, dropShadow, time);
        }

        // ===== brief 渲染 =====
        BriefStyle briefStyle = resolveBriefStyle(text);
        if (briefStyle != BriefStyle.NONE) {
            return drawGradientWobbleString(text, x, y, color, dropShadow, briefStyle, time);
        }

        return super.drawString(text, x, y, color, dropShadow);
    }

    // =========================================================
    // 文本标准化
    // =========================================================
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

    private boolean isPrefixOrEqual(String current, String target) {
        return target.startsWith(current);
    }

    private boolean nameMatches(String current, String target) {
        if (current == null || target == null) return false;

        String a = normalizeText(current);
        String b = normalizeText(target);

        return a.equals(b) || a.contains(b) || b.contains(a);
    }

    // =========================================================
    // 名称判定（替换版）
    // =========================================================
    private boolean isArtifactName(String text) {
        String plain = normalizeText(text);
        if (plain.isEmpty()) return false;

        return nameMatches(plain, NAME_TWISTED_FATE)
                || nameMatches(plain, NAME_DIMINISHED_SHADE)
                || nameMatches(plain, NAME_ABYSSAL_COGNITION)
                || nameMatches(plain, NAME_ARCANE_ANNIHILATION)
                || nameMatches(plain, NAME_SORROWED_SHRIEK);
    }

    // =========================================================
    // brief 判定
    // =========================================================
    private BriefStyle resolveBriefStyle(String text) {
        String plain = normalizeText(text);
        if (plain.isEmpty()) return BriefStyle.NONE;

        if (isPrefixOrEqual(plain, normalizeText(TWISTED_FATE))) {
            return BriefStyle.TWISTED_FATE;
        }
        if (isPrefixOrEqual(plain, normalizeText(DIMINISHED_SHADE))) {
            return BriefStyle.DIMINISHED_SHADE;
        }
        if (isPrefixOrEqual(plain, normalizeText(ABYSSAL_COGNITION))) {
            return BriefStyle.ABYSSAL_COGNITION;
        }
        if (isPrefixOrEqual(plain, normalizeText(ARCANE_ANNIHILATION))) {
            return BriefStyle.ARCANE_ANNIHILATION;
        }
        if (isPrefixOrEqual(plain, normalizeText(SORROWED_SHRIEK))) {
            return BriefStyle.SORROWED_SHRIEK;
        }

        return BriefStyle.NONE;
    }

    // =========================================================
    // 名称渲染（替换版：不再依赖 NameStyle）
    // =========================================================
    private int drawArtifactName(String text, float x, float y, int baseColor, boolean dropShadow, long time) {
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

            int color = getArtifactNameColor(text, progress, time, currentColorFromCode, visibleIndex);
            float offsetX = getArtifactNameOffsetX(text, time, visibleIndex);
            float offsetY = getArtifactNameOffsetY(text, time, visibleIndex);

            drawArtifactNameExtraLayers(text, c, x, y, color, time, visibleIndex);

            if (dropShadow) {
                int shadowColor = darkenColor(color, getArtifactNameShadowFactor(text));
                super.drawString(String.valueOf(c), x + offsetX + 0.8F, y + offsetY + 0.8F, shadowColor, false);
            }

            super.drawString(String.valueOf(c), x + offsetX, y + offsetY, color, false);

            if (bold) {
                super.drawString(String.valueOf(c), x + offsetX + 1.0F, y + offsetY, color, false);
            }

            x += this.getCharWidth(c) + (bold ? 1 : 0);
            visibleIndex++;
        }

        return (int) (x - startX);
    }

    private void drawArtifactNameExtraLayers(String text, char c, float x, float y, int color, long time, int visibleIndex) {
        if (nameMatches(text, NAME_DIMINISHED_SHADE)) {
            float wave = (float) Math.sin(time / 220.0 + visibleIndex * 0.35);
            float offset = wave * 1.2F;

            int ghost1 = withAlpha(darkenColor(color, 0.45F), 70);
            int ghost2 = withAlpha(darkenColor(color, 0.35F), 45);

            super.drawString(String.valueOf(c), x - 0.6F + offset, y, ghost1, false);
            super.drawString(String.valueOf(c), x - 1.2F + offset * 1.3F, y + 0.2F, ghost2, false);
            return;
        }

        if (nameMatches(text, NAME_ABYSSAL_COGNITION)) {
            if (((time / 120L) + visibleIndex) % 8 == 0) {
                int glow = withAlpha(0xFFFFFFFF, 120);
                super.drawString(String.valueOf(c), x, y - 0.3F, glow, false);
            }

            if (((time / 200L) + visibleIndex) % 13 == 0) {
                int spark = withAlpha(0x88FFFFFF, 90);
                super.drawString(String.valueOf(c), x + 0.5F, y, spark, false);
            }
            return;
        }

        if (nameMatches(text, NAME_ARCANE_ANNIHILATION)) {
            float pulse = (float) ((Math.sin(time / 120.0) + 1.0) * 0.5);

            if (pulse > 0.65F) {
                int glow = withAlpha(lerpColor(color, 0xFFFFFFFF, pulse), (int) (pulse * 140));
                super.drawString(String.valueOf(c), x, y - 0.4F, glow, false);
            }
            return;
        }

        if (nameMatches(text, NAME_SORROWED_SHRIEK)) {
            float drop = (float) Math.abs(Math.sin(time / 260.0 + visibleIndex * 0.25)) * 0.8F;

            int shadow = withAlpha(darkenColor(color, 0.4F), 60);

            super.drawString(String.valueOf(c), x, y + drop, shadow, false);
            super.drawString(String.valueOf(c), x, y + drop + 0.4F, withAlpha(shadow, 40), false);
            return;
        }

        if (nameMatches(text, NAME_TWISTED_FATE)) {
            // 主 glitch：频率略高一点，但透明度更克制
            if (((time / 115L) + visibleIndex * 3) % 19 == 0) {
                int glitch = withAlpha(0xFFFF6A6A, 78);
                super.drawString(String.valueOf(c), x + 0.75F, y, glitch, false);
            }

            // 偶发暗红残影
            if (((time / 170L) + visibleIndex * 5) % 31 == 0) {
                int after = withAlpha(0xFF7A0012, 52);
                super.drawString(String.valueOf(c), x - 0.45F, y + 0.15F, after, false);
            }
        }
    }

    private int getArtifactNameColor(String text, float progress, long time, int baseColor, int visibleIndex) {
        if (nameMatches(text, NAME_TWISTED_FATE)) {
            float shift = (time % 2000L) / 2000.0F; // 略微再快一点
            float t = (progress + shift) % 1.0F;

            // 红 → 白 → 红（神性版本）
            int color = gradient3(t, baseColor,
                    0xFFFFE6EC,   // 左：淡白粉（替代黑）
                    0xFFFF5A72,   // 中：亮红
                    0xFFFFC1CC,   // 右：偏白红
                    0.75F, 0.95F, 0.75F);

            // 更亮的扫光（神性核心）
            color = applySweepHighlight(color, progress, shift, 0.07F, 0.75F);

            // 柔和脉冲（不要太强，否则会过曝）
            return applyPulse(color, time + visibleIndex * 12L, 0.06F, 160.0);
        }

        float shift = (time % 2800L) / 2800.0F;
        float t = (progress + shift) % 1.0F;

        if (nameMatches(text, NAME_DIMINISHED_SHADE)) {
            int color = gradient3(t, baseColor,
                    0xFF120A22,
                    0xFF8A6CFF,
                    0xFF2A1645,
                    0.72F, 0.80F, 0.74F);
            return applyPulse(color, time, 0.16F, 520.0);
        }

        if (nameMatches(text, NAME_ABYSSAL_COGNITION)) {
            int color = gradient3(t, baseColor,
                    0xFF083743,
                    0xFF5DEBFF,
                    0xFF0D1F32,
                    0.70F, 0.82F, 0.74F);
            color = applyPulse(color, time + visibleIndex * 35L, 0.20F, 240.0);
            return applySweepHighlight(color, progress, shift, 0.07F, 0.35F);
        }

        if (nameMatches(text, NAME_ARCANE_ANNIHILATION)) {
            int color = gradient3(t, baseColor,
                    0xFF6A4200,
                    0xFFFFD76A,
                    0xFF6FD8FF,
                    0.68F, 0.88F, 0.45F);
            color = applyPulse(color, time, 0.18F, 300.0);
            return applySweepHighlight(color, progress, shift, 0.10F, 0.55F);
        }

        if (nameMatches(text, NAME_SORROWED_SHRIEK)) {
            int color = gradient3(t, baseColor,
                    0xFF17351D,
                    0xFF8DFFC0,
                    0xFF2D7A45,
                    0.72F, 0.82F, 0.74F);
            return applySweepHighlight(color, progress, shift, 0.08F, 0.30F);
        }

        return baseColor;
    }

    private float getArtifactNameOffsetX(String text, long time, int visibleIndex) {
        if (nameMatches(text, NAME_TWISTED_FATE)) {
            // 比原版略频繁，但位移更收一点
            if (((time / 105L) + visibleIndex * 7) % 23 == 0) {
                return (visibleIndex % 2 == 0) ? 0.35F : -0.35F;
            }
            return 0F;
        }

        if (nameMatches(text, NAME_DIMINISHED_SHADE)) {
            return (float) Math.cos(time / 220.0D + visibleIndex * 0.35D) * 0.35F;
        }

        if (nameMatches(text, NAME_ABYSSAL_COGNITION)) {
            if (((time / 100L) + visibleIndex) % 10 == 0) {
                return 0.5F;
            }
            return (float) Math.sin(time / 180.0D + visibleIndex * 0.35D) * 0.12F;
        }

        if (nameMatches(text, NAME_ARCANE_ANNIHILATION)) {
            return (float) Math.sin(time / 120.0D + visibleIndex * 0.2D) * 0.15F;
        }

        if (nameMatches(text, NAME_SORROWED_SHRIEK)) {
            return (float) Math.sin(time / 420.0D + visibleIndex * 0.28D) * 0.12F;
        }

        return 0F;
    }

    private float getArtifactNameOffsetY(String text, long time, int visibleIndex) {
        if (nameMatches(text, NAME_TWISTED_FATE)) {
            return 0F;
        }

        if (nameMatches(text, NAME_DIMINISHED_SHADE)) {
            return (float) Math.sin(time / 260.0D + visibleIndex * 0.3D) * 0.5F;
        }

        if (nameMatches(text, NAME_ABYSSAL_COGNITION)) {
            if (((time / 120L) + visibleIndex) % 12 == 0) {
                return -0.6F;
            }
            return (float) Math.sin(time / 220.0D + visibleIndex * 0.26D) * 0.12F;
        }

        if (nameMatches(text, NAME_ARCANE_ANNIHILATION)) {
            return (float) Math.sin(time / 180.0D + visibleIndex * 0.15D) * 0.2F;
        }

        if (nameMatches(text, NAME_SORROWED_SHRIEK)) {
            return (float) Math.abs(Math.sin(time / 260.0D + visibleIndex * 0.25D)) * 0.8F;
        }

        return 0F;
    }

    private float getArtifactNameShadowFactor(String text) {
        if (nameMatches(text, NAME_TWISTED_FATE)) return 0.22F;
        if (nameMatches(text, NAME_DIMINISHED_SHADE)) return 0.20F;
        if (nameMatches(text, NAME_ABYSSAL_COGNITION)) return 0.26F;
        if (nameMatches(text, NAME_ARCANE_ANNIHILATION)) return 0.30F;
        if (nameMatches(text, NAME_SORROWED_SHRIEK)) return 0.24F;
        return 0.28F;
    }

    // =========================================================
    // brief 渲染
    // =========================================================
    private int drawGradientWobbleString(String text, float x, float y, int baseColor, boolean dropShadow, BriefStyle style, long time) {
        float startX = x;

        int currentColorFromCode = baseColor;
        boolean bold = false;

        String plain = stripFormatting(text);
        int totalWidth = super.getStringWidth(plain);

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
            float shift = (time % 3200L) / 3200.0F;
            float t = (progress + shift) % 1.0F;

            int gradientColor = getGradientColorByStyle(style, t, currentColorFromCode);
            float offsetY = getWaveOffset(style, time, visibleIndex);
            float offsetX = getHorizontalOffset(style, time, visibleIndex);

            if (dropShadow) {
                int shadowColor = darkenColor(gradientColor, getShadowFactor(style));
                super.drawString(String.valueOf(c), x + offsetX + 0.7F, y + offsetY + 0.7F, shadowColor, false);
            }

            super.drawString(String.valueOf(c), x + offsetX, y + offsetY, gradientColor, false);

            if (bold) {
                super.drawString(String.valueOf(c), x + offsetX + 1.0F, y + offsetY, gradientColor, false);
            }

            x += this.getCharWidth(c) + (bold ? 1 : 0);
            visibleIndex++;
        }

        return (int) (x - startX);
    }

    private float getWaveOffset(BriefStyle style, long time, int visibleIndex) {
        switch (style) {
            case TWISTED_FATE:
                return (float) Math.sin((time / 380.0D) + visibleIndex * 0.22D) * 1.8F;
            case DIMINISHED_SHADE:
                return (float) Math.sin((time / 460.0D) + visibleIndex * 0.26D) * 1.2F;
            case ABYSSAL_COGNITION:
                return (float) Math.sin((time / 320.0D) + visibleIndex * 0.20D) * 1.5F;
            case ARCANE_ANNIHILATION:
                return (float) Math.sin((time / 300.0D) + visibleIndex * 0.24D) * 1.1F;
            case SORROWED_SHRIEK:
                return (float) Math.sin((time / 520.0D) + visibleIndex * 0.18D) * 1.7F;
            default:
                return 0F;
        }
    }

    private float getHorizontalOffset(BriefStyle style, long time, int visibleIndex) {
        switch (style) {
            case DIMINISHED_SHADE:
                return (float) Math.cos((time / 500.0D) + visibleIndex * 0.35D) * 0.35F;
            case SORROWED_SHRIEK:
                return (float) Math.cos((time / 620.0D) + visibleIndex * 0.28D) * 0.25F;
            default:
                return 0F;
        }
    }

    private float getShadowFactor(BriefStyle style) {
        switch (style) {
            case ARCANE_ANNIHILATION:
                return 0.33F;
            case ABYSSAL_COGNITION:
                return 0.30F;
            case DIMINISHED_SHADE:
                return 0.24F;
            case SORROWED_SHRIEK:
                return 0.26F;
            case TWISTED_FATE:
            default:
                return 0.28F;
        }
    }

    private int getGradientColorByStyle(BriefStyle style, float t, int baseColor) {
        switch (style) {
            case TWISTED_FATE:
                return gradient3(
                        t, baseColor,
                        0xFF7A0A16,
                        0xFFFF6262,
                        0xFF52060D,
                        0.55F, 0.70F, 0.55F
                );

            case DIMINISHED_SHADE:
                return gradient3(
                        t, baseColor,
                        0xFF25143F,
                        0xFF7D67FF,
                        0xFF120A22,
                        0.60F, 0.72F, 0.62F
                );

            case ABYSSAL_COGNITION:
                return gradient3(
                        t, baseColor,
                        0xFF063C46,
                        0xFF56F0FF,
                        0xFF0B1F31,
                        0.60F, 0.75F, 0.60F
                );

            case ARCANE_ANNIHILATION:
                return gradient3(
                        t, baseColor,
                        0xFF7A4A00,
                        0xFFFFD86B,
                        0xFFB87312,
                        0.55F, 0.78F, 0.65F
                );

            case SORROWED_SHRIEK:
                return gradient3(
                        t, baseColor,
                        0xFF0E4A1F,
                        0xFF8CFFAA,
                        0xFF16311C,
                        0.58F, 0.73F, 0.60F
                );

            default:
                return baseColor;
        }
    }

    // =========================================================
    // 公共工具方法
    // =========================================================
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

    private int blendColor(int c1, int c2, float t) {
        return lerpColor(c1, c2, clamp01(t));
    }

    private int withAlpha(int color, int alpha) {
        alpha = Math.max(0, Math.min(255, alpha));
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    private float clamp01(float v) {
        return v < 0F ? 0F : (v > 1F ? 1F : v);
    }
}