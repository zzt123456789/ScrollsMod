package com.zzt.eternal_abyss.event;

import com.zzt.eternal_abyss.EternalAbyss;
import com.zzt.eternal_abyss.config.ModConfig;
import com.zzt.eternal_abyss.init.ModAttributes;
import com.zzt.eternal_abyss.init.ModSounds;
import com.zzt.eternal_abyss.packages.PacketCritMessage;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

public class PlayerAttributeHandler {
    /**
     * Softmin 暴击倍率计算
     *
     * @param layers     n：暴击层数
     * @param critDamage D：单次暴击倍率（例如 1.5 = 150%）
     */
    public static double calcCritMultiplierSoft(
            int layers,
            double critDamage
    ) {
        if (layers <= 0) return 1.0;

        double C = critDamage;

        double a = ModConfig.critSoftA;
        double N = ModConfig.critSoftN;
        double k = ModConfig.critSoftK;

        double n = layers;

        // 指数增长
        double expPart = Math.pow(C, n);

        // 多项式软上限
        double polyPart = a * Math.pow(n, N);

        // 防止异常
        if (polyPart <= 0) return expPart;

        // ★ 真正的 softmin（关键）
        return expPart / Math.pow(
                1.0 + Math.pow(expPart / polyPart, k),
                1.0 / k
        );
    }



    @SubscribeEvent
    public void onEntityJoin(EntityJoinWorldEvent event) {
        if (!(event.getEntity() instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) event.getEntity();
        AbstractAttributeMap map = (AbstractAttributeMap) player.getAttributeMap();

        if (player.getEntityAttribute(ModAttributes.CRIT_CHANCE) == null)
            map.registerAttribute(ModAttributes.CRIT_CHANCE);

        if (player.getEntityAttribute(ModAttributes.CRIT_DAMAGE) == null)
            map.registerAttribute(ModAttributes.CRIT_DAMAGE);
    }

    @SubscribeEvent
    public void onPlayerAttack(LivingHurtEvent event) {
        if (!(event.getSource().getTrueSource() instanceof EntityPlayer)) return;
        if (event.isCanceled()) return;
        if (event.getAmount() <= 0) return;


        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
        if (DepthRingHandler.getEquippedDepthRing(player).isEmpty()) {
            return;
        }
        double C = player.getEntityAttribute(ModAttributes.CRIT_CHANCE).getAttributeValue();
        double D = player.getEntityAttribute(ModAttributes.CRIT_DAMAGE).getAttributeValue();

        float baseDamage = event.getAmount();
        Random rand = player.getRNG();


        int layer = getCritLayers(C, rand);

        if (layer <= 0) return; // 未暴击
        double totalMultiplier = calcCritMultiplierSoft(layer, D);

        // 计算总伤害
        float totalDamage = (float) (baseDamage * totalMultiplier);

        event.setAmount(totalDamage);

        // 发送飘字
        if (player instanceof EntityPlayerMP) {
            EternalAbyss.NETWORK.sendTo(
                    new PacketCritMessage(layer),
                    (EntityPlayerMP) player
            );
        }

        // 音调随层数提升
        float pitch = 0.1f + layer * 0.05f;

        // 播放音效
        player.world.playSound(null, player.posX, player.posY, player.posZ,
                ModSounds.CRIT_HIT, SoundCategory.PLAYERS,
                0.8F,
                pitch
        );
    }


    public static int getCritLayers(double critChance, Random rand) {

        // 第一层：概率 min(C, 1)
        double firstChance = Math.min(critChance, 1.0);
        if (rand.nextDouble() >= firstChance) {
            return 0; // 完全未暴击
        }

        int layers = 1;

        // 溢出暴击层（C - 1, C - 2, C - 3, ...）
        while (true) {
            double nextChance = critChance - layers;
            if (nextChance <= 0) break;

            if (rand.nextDouble() < nextChance) {
                layers++;
            } else break;
        }

        return layers;
    }

}
