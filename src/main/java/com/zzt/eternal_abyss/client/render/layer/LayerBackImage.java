package com.zzt.eternal_abyss.client.render.layer;

import com.zzt.eternal_abyss.util.ImmunityHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class LayerBackImage implements LayerRenderer<AbstractClientPlayer> {
    private static final ResourceLocation TEX =
            new ResourceLocation("eternal_abyss", "textures/misc/void.png");

    @Override
    public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                              float partialTicks, float ageInTicks, float netHeadYaw,
                              float headPitch, float scale) {

        // 只有佩戴 VOID_ARTIFACT 时才绘制
        if (!ImmunityHelper.hasUltimateItem(player)) return;

        GlStateManager.pushMatrix();

        GlStateManager.translate(0.0F, 0.8F, 0.0F);
        GlStateManager.translate(0.0F, -player.height / 2F, 0.65F);

        float angle = (player.ticksExisted + partialTicks)*1.5F; // 顺时针
        GlStateManager.rotate(angle, 0F, 0F, -1F);

        Minecraft.getMinecraft().getTextureManager().bindTexture(TEX);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        double size = 0.9F;
        buf.pos(-size,  size, 0).tex(0, 1).endVertex();
        buf.pos( size,  size, 0).tex(1, 1).endVertex();
        buf.pos( size, -size, 0).tex(1, 0).endVertex();
        buf.pos(-size, -size, 0).tex(0, 0).endVertex();

        tess.draw();
        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldCombineTextures() { return false; }
}
