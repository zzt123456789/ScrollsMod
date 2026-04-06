package com.zzt.eternal_abyss.render;

import com.zzt.eternal_abyss.entity.EntitySupernovaBall;
import com.zzt.eternal_abyss.init.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@SideOnly(Side.CLIENT)
public class RenderSupernovaBall extends Render<EntitySupernovaBall> {

    private static final ItemStack STACK = new ItemStack(ModItems.SUPERNOVA);

    public RenderSupernovaBall(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 0.0F;
    }

    @Override
    public void doRender(EntitySupernovaBall entity, double x, double y, double z,
                         float entityYaw, float partialTicks) {

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        // A：飞行朝向（只做一次）
        float yaw = entity.prevRotationYaw +
                (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
        float pitch = entity.prevRotationPitch +
                (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;

        GlStateManager.rotate(-yaw, 0F, 1F, 0F);
        GlStateManager.rotate(pitch, 1F, 0F, 0F);

        // B：自转（独立）
        float spin = entity.spinYaw + 20F * partialTicks;
        GlStateManager.rotate(spin, 0F, 0F, 1F); // 围绕自身轴

        GlStateManager.scale(0.75F, 0.75F, 0.75F);

        Minecraft.getMinecraft().getRenderItem()
                .renderItem(STACK, ItemCameraTransforms.TransformType.GROUND);

        GlStateManager.popMatrix();
    }


    @Override
    protected ResourceLocation getEntityTexture(EntitySupernovaBall entity) {
        // 一定要返回 null
        return null;
    }
}
