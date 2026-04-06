package com.zzt.eternal_abyss.util;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import com.zzt.eternal_abyss.EternalAbyss;
import com.zzt.eternal_abyss.packages.PacketSyncBaubleNBT;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public class BaubleSyncUtil {

    /**
     * 同步指定玩家指定饰品槽的 NBT 数据到客户端
     * @param player 玩家对象（必须是服务端）
     * @param slot   饰品槽位索引
     * @param stack  饰品 ItemStack
     */
    // 原方法保留
    public static void syncNBTToClient(EntityPlayerMP player, int slot, ItemStack stack) {
        BaublesApi.getBaublesHandler(player).setChanged(slot, true);
        EternalAbyss.NETWORK.sendTo(
                new PacketSyncBaubleNBT(slot, stack.getTagCompound()),
                player
        );
    }

    public static void syncBaubleNBT(EntityPlayerMP player, ItemStack targetStack) {
        IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stackInSlot = handler.getStackInSlot(i);
            if (!stackInSlot.isEmpty() && ItemStack.areItemStacksEqual(stackInSlot, targetStack)) {
                syncNBTToClient(player, i, stackInSlot);
                break;
            }
        }
    }

}
