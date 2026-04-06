package com.zzt.eternal_abyss.event;

import baubles.api.BaublesApi;
import com.zzt.eternal_abyss.init.ModItems;
import com.zzt.eternal_abyss.items.TheArcaneAnnihilation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


@Mod.EventBusSubscriber
public class TheArcaneAnnihilationHandler {

    /**
     * 第一步：怪物掉落事件（读取掉落物数量 + 湮灭掉落物）
     */
    @SubscribeEvent
    public static void onMobDrops(LivingDropsEvent event) {

        if (!(event.getSource().getTrueSource() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();

        ItemStack ring = hasArcaneRing(player);
        if (ring.isEmpty()) return;

        if (!TheArcaneAnnihilation.isArcaneConvertDrops(ring)) return;

        int totalItems = 0;

        for (EntityItem ei : event.getDrops()) {
            if (ei == null) continue;
            ItemStack is = ei.getItem();
            if (is.isEmpty()) continue;

            totalItems += is.getCount();
        }

        if (totalItems > 0) {
            event.getDrops().clear();
        }
        int totalExp = 0;

        if (totalItems <= 10) {
            totalExp = totalItems * 1;
        } else if (totalItems > 10 && totalItems <= 32) {
            totalExp = totalItems * 2;
        } else if (totalItems >32 && totalItems<=64) {
            totalExp = totalItems * 3;
        } else if (totalItems>64) {
            totalExp = totalItems * 4;
        }

        if (totalExp > 0) {
            Entity entity = event.getEntityLiving();

            while (totalExp > 0) {
                int split = EntityXPOrb.getXPSplit(totalExp);
                totalExp -= split;

                EntityXPOrb orb = new EntityXPOrb(
                        player.world,
                        entity.posX,
                        entity.posY,
                        entity.posZ,
                        split
                );
                player.world.spawnEntity(orb);
            }
        }
    }



    public static ItemStack hasArcaneRing(EntityPlayer player) {
        int slot = BaublesApi.isBaubleEquipped(player, ModItems.THE_ARCANE_ANNIHILATION);
        if (slot != -1) {
            return BaublesApi.getBaublesHandler(player).getStackInSlot(slot);
        }
        return ItemStack.EMPTY;
    }
}
