package com.zzt.eternal_abyss.client;

import com.zzt.eternal_abyss.client.particle.ParticleMiracleBlight;
import com.zzt.eternal_abyss.init.ModPotions;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public class SwordQiParticleHandler {

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (!entity.world.isRemote) return;

        PotionEffect effect = entity.getActivePotionEffect(ModPotions.SWORD_QI);
        if (effect == null) return;

        // 控制频率
        if (entity.world.rand.nextInt(2) != 0) return;

        spawnSwordQiBurst(entity, effect.getAmplifier());
    }

    private static void spawnSwordQiBurst(EntityLivingBase entity, int amp) {
        int count = 4 + amp * 2;

        for (int i = 0; i < count; i++) {
            // ===== 从实体中心附近生成 =====
            double px = entity.posX + (entity.world.rand.nextDouble() - 0.5D) * 0.45D;
            double py = entity.posY + entity.height * 0.5D
                    + (entity.world.rand.nextDouble() - 0.5D) * (0.70D + amp * 0.10D);
            double pz = entity.posZ + (entity.world.rand.nextDouble() - 0.5D) * 0.45D;

            double mx = (entity.world.rand.nextDouble() - 0.5D) * (0.80D + amp * 0.60D);
            double my = (entity.world.rand.nextDouble() - 0.5D) * (1.80D + amp * 1.20D);
            double mz = (entity.world.rand.nextDouble() - 0.5D) * (0.80D + amp * 0.60D);

            ParticleMiracleBlight p = new ParticleMiracleBlight(
                    entity.world,
                    px, py, pz,
                    mx, my, mz
            );

            Minecraft.getMinecraft().effectRenderer.addEffect(p);
        }
    }
}