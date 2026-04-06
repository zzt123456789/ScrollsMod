package com.zzt.eternal_abyss.client.tooltip;

import com.zzt.eternal_abyss.init.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;

@Mod.EventBusSubscriber(Side.CLIENT)
public class TestTooltipGalaxyBackground {

    // 资源路径：
    // assets/eternal_abyss/textures/gui/galaxy_bg.png
    private static final ResourceLocation GALAXY_BG =
            new ResourceLocation("eternal_abyss:textures/gui/galaxy_bg.png");

    @SubscribeEvent
    public static void onRenderTooltipPostBackground(RenderTooltipEvent.PostBackground event) {
        ItemStack stack = event.getStack();
        if (stack.isEmpty()) return;

        Item items = stack.getItem();

        if (items != ModItems.VOID_ARTIFACT
                && items != ModItems.THE_TWISTED_FATE
                && items != ModItems.THE_DIMINISHED_SHADE
                && items != ModItems.THE_ABYSSAL_COGNITION
                && items != ModItems.THE_ARCANE_ANNIHILATION
                && items != ModItems.THE_SORROWED_SHRIEK
                && items != ModItems.DEPTH_RING
                && items != ModItems.RECORD_BAG
                && items != ModItems.ABYSSAL_ESSENCE
                && items != ModItems.WAILING_CORE
                && items != ModItems.INSANE_ABYSS_DICE
        ) return;



        Minecraft mc = Minecraft.getMinecraft();
        float partialTicks = mc.getRenderPartialTicks();
        float time = (mc.player != null ? mc.player.ticksExisted : 0) + partialTicks;

        // 外扩一点，让效果更贴合背景区域
        int x = event.getX() - 3;
        int y = event.getY() - 4;
        int w = event.getWidth() + 6;
        int h = event.getHeight() + 8;

        // 先加一个很淡的暗色遮罩，方便看清星空
        drawOverlayRect(x, y, x + w, y + h, 0x22101018);

        // 两层背景，速度/方向/随机扰动都不同
        drawGalaxyLayer(x, y, w, h, time, 0, 0.22F, 128.0F);
        drawGalaxyLayer(x, y, w, h, time, 1, 0.14F, 176.0F);
    }

    /**
     * 绘制一层带随机扰动的星空背景
     *
     * @param layerIndex 层索引，不同层会有不同速度/方向/扰动
     * @param alpha      透明度
     * @param scale      纹理缩放
     */
    private static void drawGalaxyLayer(int x, int y, int w, int h, float time,
                                        int layerIndex, float alpha, float scale) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.getTextureManager().bindTexture(GALAXY_BG);

        // 基于 tooltip 位置和层索引生成稳定“随机种子”
        int seed = x * 31 + y * 17 + w * 13 + h * 7 + layerIndex * 101;

        // 为每层生成不同速度
        float baseSpeedU = 0.0012F + ((seed >> 1) & 3) * 0.00035F;
        float baseSpeedV = 0.0009F + ((seed >> 3) & 3) * 0.00028F;

        // 随机方向
        if (((seed >> 5) & 1) == 0) baseSpeedU = -baseSpeedU;
        if (((seed >> 6) & 1) == 0) baseSpeedV = -baseSpeedV;

        // 第二层再额外偏一点
        if (layerIndex == 1) {
            baseSpeedU *= 1.35F;
            baseSpeedV *= 1.18F;
        }

        // 基础偏移：整体慢慢移动
        float uOffset = time * baseSpeedU;
        float vOffset = time * baseSpeedV;

        // 加一点非线性扰动，避免“整张图像机械平移”
        float phaseA = (seed & 255) * 0.137F;
        float phaseB = ((seed >> 8) & 255) * 0.173F;

        uOffset += (float) Math.sin(time * (0.021F + layerIndex * 0.006F) + phaseA) * 0.045F;
        vOffset += (float) Math.cos(time * (0.018F + layerIndex * 0.005F) + phaseB) * 0.040F;

        // 再加一个更微弱的次级扰动
        uOffset += (float) Math.cos(time * 0.007F + phaseB * 1.7F) * 0.018F;
        vOffset += (float) Math.sin(time * 0.009F + phaseA * 1.3F) * 0.018F;

        // 控制在 0~1 范围附近，避免 UV 太大
        uOffset = uOffset % 1.0F;
        vOffset = vOffset % 1.0F;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1F, 1F, 1F, alpha);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(x,     y + h, 300).tex(uOffset,                 h / scale + vOffset).endVertex();
        buf.pos(x + w, y + h, 300).tex(w / scale + uOffset,     h / scale + vOffset).endVertex();
        buf.pos(x + w, y,     300).tex(w / scale + uOffset,     vOffset).endVertex();
        buf.pos(x,     y,     300).tex(uOffset,                 vOffset).endVertex();
        tess.draw();

        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    /**
     * 简单叠加一层半透明色块，帮助测试时看清背景
     */
    private static void drawOverlayRect(int left, int top, int right, int bottom, int color) {
        float a = (color >> 24 & 255) / 255.0F;
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(r, g, b, a);

        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        buf.pos(left,  bottom, 290).endVertex();
        buf.pos(right, bottom, 290).endVertex();
        buf.pos(right, top,    290).endVertex();
        buf.pos(left,  top,    290).endVertex();
        tess.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1F, 1F, 1F, 1F);
    }
}