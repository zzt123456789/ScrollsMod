package com.zzt.eternal_abyss.event;

import com.zzt.eternal_abyss.init.ModPotions;
import com.zzt.eternal_abyss.items.Exoblade;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

@Mod.EventBusSubscriber  // 自动注册
public class ExobladeEventHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (player.world.isRemote) return;

        ItemStack heldItem = player.getHeldItemMainhand();
        if (!heldItem.isEmpty() && heldItem.getItem() instanceof Exoblade) {
            player.removeActivePotionEffect(ModPotions.SWORD_QI);
        }
    }
}
