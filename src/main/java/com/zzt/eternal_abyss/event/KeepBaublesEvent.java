package com.zzt.eternal_abyss.event;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import com.zzt.eternal_abyss.util.interfaces.IKeptBauble;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;

@Mod.EventBusSubscriber
public class KeepBaublesEvent {

    private static final Map<UUID, List<SlotStack>> KEPT = new HashMap<>();

    private static class SlotStack {
        final int slot;
        final ItemStack stack;

        SlotStack(int slot, ItemStack stack) {
            this.slot = slot;
            this.stack = stack;
        }
    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerDropsHigh(PlayerDropsEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player == null || player.world.isRemote) return;

        KEPT.remove(player.getUniqueID());

        IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);
        List<SlotStack> kept = new ArrayList<>();

        for (int i = 0; i < baubles.getSlots(); i++) {
            ItemStack stack = baubles.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof IKeptBauble) {
                kept.add(new SlotStack(i, stack)); // 保持与你说的一致，不 copy
                baubles.setStackInSlot(i, ItemStack.EMPTY);
            }
        }

        if (!kept.isEmpty()) {
            KEPT.put(player.getUniqueID(), kept);
        }
    }




    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerDropsLow(PlayerDropsEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player == null || player.world.isRemote) return;

        List<SlotStack> kept = KEPT.remove(player.getUniqueID());
        if (kept == null) return;

        IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);
        for (SlotStack ss : kept) {
            baubles.setStackInSlot(ss.slot, ss.stack);
        }
    }
}
