package com.zzt.eternal_abyss.util;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public class LuckModifierHelper {

    private static final UUID LUCK_MODIFIER_ID =
            UUID.fromString("2c77c3c0-3cfd-4d45-b7db-1e0ddf77a951");

    /**
     * 只做一件事：给玩家设置幸运值（server-safe）
     */
    public static boolean setLuck(EntityPlayerMP player, double luck) {

        // 1️⃣ 直接操作 Attribute（服务器绝对安全）
        IAttributeInstance luckAttr =
                player.getEntityAttribute(SharedMonsterAttributes.LUCK);

        if (luckAttr != null) {
            luckAttr.removeModifier(LUCK_MODIFIER_ID);

            luckAttr.applyModifier(new AttributeModifier(
                    LUCK_MODIFIER_ID,
                    "LuckModifierBoost",
                    luck,
                    0 // ADD
            ));
        }

        // 2️⃣ 同步 Baubles（只用接口，不碰具体 Item 类）
        IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);

        for (int i = 0; i < baubles.getSlots(); i++) {
            ItemStack stack = baubles.getStackInSlot(i);
            if (!stack.isEmpty() && stack.hasTagCompound()) {
                // 只改 NBT，不 instanceof LuckModifier
                stack.getOrCreateSubCompound("LuckData")
                        .setDouble("Luck", luck);
            }
        }

        return true;
    }
}
