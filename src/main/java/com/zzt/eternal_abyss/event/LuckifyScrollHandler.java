package com.zzt.eternal_abyss.event;

import baubles.api.BaublesApi;
import com.zzt.eternal_abyss.items.DepthRing;
import com.zzt.eternal_abyss.items.LuckifyScroll;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.IItemHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class LuckifyScrollHandler {

    /** 记录本 tick 内是否成功闪避 */
    private static final Map<UUID, Long> LAST_DODGE_TICK = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();

        if (!hasLuckifyScroll(player)) return;

        float luck = player.getLuck();
        double dodgeChance = DepthRing.isDamageReversedFor(player) ? 0.02 * luck : 0.01 * luck;
        dodgeChance = Math.min(dodgeChance, 0.6);

        if (player.world.rand.nextDouble() < dodgeChance) {
            event.setCanceled(true); // 成功闪避！

            // 记录本 tick 闪避成功
            LAST_DODGE_TICK.put(player.getUniqueID(), player.world.getTotalWorldTime());

            // 播放音效
            player.world.playSound(null, player.posX, player.posY, player.posZ,
                    SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.5F, 1.0F);
        }
    }


    @SubscribeEvent
    public static void onPlayerKnockBack(LivingKnockBackEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();

        Long tick = LAST_DODGE_TICK.get(player.getUniqueID());
        if (tick != null && tick == player.world.getTotalWorldTime()) {
            // 本 tick 内刚刚闪避成功 → 取消击退
            event.setStrength(0.0F);
        }
    }

    /** 是否佩戴了 LuckifyScroll */
    private static boolean hasLuckifyScroll(EntityPlayer player) {
        IItemHandler baubles = BaublesApi.getBaublesHandler(player);
        if (baubles == null) return false;

        for (int i = 0; i < baubles.getSlots(); i++) {
            ItemStack stack = baubles.getStackInSlot(i);
            if (!stack.isEmpty()
                    && stack.getItem() instanceof LuckifyScroll) {
                return true;
            }
        }
        return false;
    }
}
