package com.zzt.eternal_abyss.util;

import com.zzt.eternal_abyss.init.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import baubles.api.BaublesApi;

public class ImmunityHelper {
    public static boolean hasUltimateItem(EntityPlayer player) {
        if (player == null) return false;
        for (int slot = 0; slot < BaublesApi.getBaublesHandler(player).getSlots(); slot++) {
            ItemStack stack = BaublesApi.getBaublesHandler(player).getStackInSlot(slot);
            if (stack != null && stack.getItem() == ModItems.VOID_ARTIFACT) {
                return true;
            }
        }
        return false;
    }
}
