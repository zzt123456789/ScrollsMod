package com.zzt.eternal_abyss.util;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.Color;

public class RainbowTextHelper {

    public static ITextComponent rainbowText(String text, float speed, float spread) {
        // 服务端：直接返回普通文本
        if (FMLCommonHandler.instance().getSide().isServer()) {
            return new TextComponentString(text);
        }
        // 客户端：执行彩虹逻辑
        return rainbowTextClient(text, speed, spread);
    }

    @SideOnly(Side.CLIENT)
    private static ITextComponent rainbowTextClient(String text, float speed, float spread) {
        TextComponentString result = new TextComponentString("");
        int length = text.length();

        int tick = net.minecraft.client.Minecraft.getMinecraft().player != null
                ? net.minecraft.client.Minecraft.getMinecraft().player.ticksExisted
                : (int) (System.currentTimeMillis() / 50L);

        for (int i = 0; i < length; i++) {
            float hue = (float) ((tick / speed + i * spread) % 360) / 360.0F;
            int rgb = Color.HSBtoRGB(hue, 1.0F, 1.0F);

            TextFormatting color = pickNearestFormatting(rgb);

            TextComponentString letter = new TextComponentString(String.valueOf(text.charAt(i)));
            letter.setStyle(new Style().setColor(color));

            result.appendSibling(letter);
        }
        return result;
    }

    // 近似映射 24位RGB 到 16个预设颜色
    private static TextFormatting pickNearestFormatting(int rgb) {
        TextFormatting[] colors = {
                TextFormatting.DARK_RED, TextFormatting.RED, TextFormatting.GOLD,
                TextFormatting.YELLOW, TextFormatting.GREEN, TextFormatting.AQUA,
                TextFormatting.BLUE, TextFormatting.DARK_PURPLE, TextFormatting.LIGHT_PURPLE,
                TextFormatting.WHITE
        };
        return colors[(rgb & 0xFFFFFF) % colors.length];
    }

    public static ITextComponent rainbowText(String text) {
        return rainbowText(text, 3.0F, 0.8F);
    }
}
