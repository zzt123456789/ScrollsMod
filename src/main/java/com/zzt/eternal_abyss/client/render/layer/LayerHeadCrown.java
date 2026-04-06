package com.zzt.eternal_abyss.client.render.layer;

import baubles.api.BaublesApi;
import com.zzt.eternal_abyss.init.ModItems;
import com.zzt.eternal_abyss.items.DepthRing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class LayerHeadCrown implements LayerRenderer<AbstractClientPlayer> {

    private static final ResourceLocation TEX =
            new ResourceLocation("eternal_abyss", "textures/misc/twisted_fate_crown.png");

    @Override
    public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                              float partialTicks, float ageInTicks, float netHeadYaw,
                              float headPitch, float scale) {

        // 判断是否佩戴饰品
        int slot = BaublesApi.isBaubleEquipped(player, ModItems.THE_TWISTED_FATE);
        if (slot == -1) return;
        ItemStack crown = BaublesApi.getBaublesHandler(player).getStackInSlot(slot);
        if (crown.isEmpty()) return;

        boolean reversed = DepthRing.isLuckyReversedFor(player);
        GlStateManager.pushMatrix();


        Minecraft.getMinecraft().getRenderManager().getSkinMap()
                .get(player.getSkinType()).getMainModel().bipedHead.postRender(0.0625F);

        GlStateManager.enableDepth();

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        Minecraft.getMinecraft().getTextureManager().bindTexture(TEX);
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        float time = player.ticksExisted + partialTicks;


        // ===================== 小图光环 9x9 =====================
        {
            GlStateManager.pushMatrix();

            // 上移到头顶，单位为模型单位
            GlStateManager.translate(0.0F, -0.65F, 0.0F);

            // 面朝上（平行头顶平面）
            GlStateManager.rotate(90F, 1F, 0F, 0F);

            // 自转（绕自身中心）
            GlStateManager.rotate(-time * 2.0F, 0F, 0F, 1F);

            // 微光
            float glow = (float) (Math.sin(time * 0.4F) * 0.2F + 0.8F);
            GlStateManager.color(1.0F, glow, 1.0F, 1.0F);

            double size = 0.3D;
            double u0 = 31.0D / 64.0D;
            double v0 = 0.0D / 32.0D;
            double u1 = (31.0D + 9.0D) / 64.0D;
            double v1 = 9.0D / 32.0D;

            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buf.pos(-size, size, 0).tex(u0, v1).endVertex();
            buf.pos(size, size, 0).tex(u1, v1).endVertex();
            buf.pos(size, -size, 0).tex(u1, v0).endVertex();
            buf.pos(-size, -size, 0).tex(u0, v0).endVertex();
            tess.draw();

            GlStateManager.popMatrix();
        }

        // ===================== 主图光环 31x31 =====================
        if(reversed){
            GlStateManager.pushMatrix();

            // 略微下移，与小图同一高度或者略低
            GlStateManager.translate(0.0F, -1F, 0.0F);

            // 倾斜 25°
            GlStateManager.rotate(45F, 1F, 0F, 0F);

            // 自转（与小图不同速度）
            GlStateManager.rotate(time * 1.33F, 0F, 0F, 1F);

            // 脉动
            float pulse = (float) (Math.sin(time * 0.2F) * 0.1F + 0.9F);
            GlStateManager.color(pulse, 0.8F, 1.0F, 0.9F);

            double mainSize = 0.8D;
            double u0 = 0.0D / 64.0D;
            double v0 = 0.0D / 32.0D;
            double u1 = 31.0D / 64.0D;
            double v1 = 31.0D / 32.0D;

            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buf.pos(-mainSize, mainSize, 0).tex(u0, v1).endVertex();
            buf.pos(mainSize, mainSize, 0).tex(u1, v1).endVertex();
            buf.pos(mainSize, -mainSize, 0).tex(u1, v0).endVertex();
            buf.pos(-mainSize, -mainSize, 0).tex(u0, v0).endVertex();
            tess.draw();

            GlStateManager.popMatrix();
        }
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
