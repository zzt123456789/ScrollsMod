package com.zzt.eternal_abyss.event;

import baubles.api.BaublesApi;
import com.zzt.eternal_abyss.items.LuckModifier;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class LuckModifierHandler {


    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.world.isRemote) return;

        EntityPlayer player = event.player;
        IAttributeInstance luckAttr = player.getEntityAttribute(SharedMonsterAttributes.LUCK);
        if (luckAttr == null) return;

        boolean hasLuckItem = false;
        double totalLuck = 0;

        IInventory baubles = BaublesApi.getBaubles(player);
        for (int i = 0; i < baubles.getSizeInventory(); i++) {
            ItemStack stack = baubles.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof LuckModifier) {
                totalLuck += LuckModifier.getLuckValue(stack);
                hasLuckItem = true;
            }
        }

        // 移除旧 modifier（避免重复应用）
        if (luckAttr.getModifier(LuckModifier.LUCK_MODIFIER_ID) != null) {
            luckAttr.removeModifier(LuckModifier.LUCK_MODIFIER_ID);
        }

        // 如果有幸运饰品且幸运值非零，则重新应用
        if (hasLuckItem && totalLuck != 0.0) {
            luckAttr.applyModifier(new AttributeModifier(
                    LuckModifier.LUCK_MODIFIER_ID,
                    "LuckModifierBoost",
                    totalLuck,
                    0 // 加法模式
            ));
        }
    }


}
