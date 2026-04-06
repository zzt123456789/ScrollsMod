package com.zzt.eternal_abyss.font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModFontRenderers {

    public static FontRenderer VOID_ARTIFACT_RENDERER;
    public static FontRenderer SPECIAL_FONT;

    public static FontRenderer DEPTH_RING_RENDERER;

    public static void init() {
        Minecraft mc = Minecraft.getMinecraft();

        SPECIAL_FONT = new CustomFontRenderer(
                mc.gameSettings,
                new ResourceLocation("textures/font/ascii.png"),
                mc.renderEngine,
                mc.isUnicode()
        );

        DEPTH_RING_RENDERER = new DepthRingFontRenderer(
                mc.gameSettings,
                new ResourceLocation("textures/font/ascii.png"),
                mc.getTextureManager(),
                mc.getLanguageManager().isCurrentLocaleUnicode()
        );

        VOID_ARTIFACT_RENDERER = new VoidArtifactFontRenderer(
                mc.gameSettings,
                new ResourceLocation("textures/font/ascii.png"),
                mc.getTextureManager(),
                mc.getLanguageManager().isCurrentLocaleUnicode()
        );
    }
}