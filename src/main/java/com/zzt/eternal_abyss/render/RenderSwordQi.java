package com.zzt.eternal_abyss.render;

import com.zzt.eternal_abyss.entity.EntitySwordQi;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.List;

@SideOnly(Side.CLIENT)
public class RenderSwordQi extends Render<EntitySwordQi> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("eternal_abyss:textures/entity/sword_qi.png");

    public RenderSwordQi(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntitySwordQi entity, double x, double y, double z,
                         float entityYaw, float partialTicks) {

        // 先画剑气本体（跟着实体走、旋转、缩放）
        renderSwordBody(entity, x, y, z, partialTicks);

        // 再画彩色拖尾（世界空间，单独一层，不闪烁）
        renderSwordTrail(entity, partialTicks);
    }

    private void renderSwordBody(EntitySwordQi entity, double x, double y, double z, float partialTicks) {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GlStateManager.disableCull(); // 双面显示

        this.bindEntityTexture(entity);

        // === 根据 motion 计算飞行方向 ===
        Vec3d motion = new Vec3d(entity.motionX, entity.motionY, entity.motionZ);
        if (motion.length() == 0) {
            motion = new Vec3d(1, 0, 0);
        }

        motion = motion.normalize();

        float yaw = (float) (Math.atan2(motion.z, motion.x) * 180.0D / Math.PI);
        float pitch = (float) Math.toDegrees(Math.asin(motion.y));

        // 让 +X 指向运动方向
        GlStateManager.rotate(-yaw, 0, 1, 0);
        GlStateManager.rotate(pitch, 0, 0, 1);

        // 贴图本身是左下→右上斜 45°
        GlStateManager.rotate(-45F, 0, 0, 1);

        // 缩放（控制整体大小）
        float s = 0.03F;
        GlStateManager.scale(s, s, s);

        float w = 47F, h = 47F;
        float thickness = 2.0F; // 厚度

        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(0, 0, 0).tex(0, 1).endVertex();
        buf.pos(w, 0, 0).tex(1, 1).endVertex();
        buf.pos(w, h, 0).tex(1, 0).endVertex();
        buf.pos(0, h, 0).tex(0, 0).endVertex();
        tess.draw();

        // 背面（简单对称一层）
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(0, 0, -thickness).tex(0, 1).endVertex();
        buf.pos(w, 0, -thickness).tex(1, 1).endVertex();
        buf.pos(w, h, -thickness).tex(1, 0).endVertex();
        buf.pos(0, h, -thickness).tex(0, 0).endVertex();
        tess.draw();

        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }


    private void renderSwordTrail(EntitySwordQi entity, float partialTicks) {
        List<Vec3d> trail = entity.trail;
        if (trail == null || trail.size() < 2) return;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        GlStateManager.pushMatrix();

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        // 不写入深度缓冲：避免与实体及自身 Z 冲突导致闪烁
        GlStateManager.depthMask(false);
        GlStateManager.glLineWidth(3.0F);

        buf.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);

        for (int i = 0; i < trail.size(); i++) {
            Vec3d p = trail.get(i);

            // 世界坐标 → 相机本地坐标
            double dx = p.x - renderManager.viewerPosX;
            double dy = p.y - renderManager.viewerPosY;
            double dz = p.z - renderManager.viewerPosZ;

            float alpha = (float) i / (float) trail.size(); // 越旧越透明

            float hue = ((entity.ticksExisted * 4 + i * 8) % 360) / 360.0f;
            int rgb = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);
            float r = ((rgb >> 16) & 0xFF) / 255.0f;
            float g = ((rgb >> 8) & 0xFF) / 255.0f;
            float b = (rgb & 0xFF) / 255.0f;

            buf.pos(dx, dy, dz).color(r, g, b, alpha).endVertex();
        }

        tess.draw();

        GlStateManager.glLineWidth(2.0F);
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySwordQi entity) {
        return TEXTURE;
    }
}
