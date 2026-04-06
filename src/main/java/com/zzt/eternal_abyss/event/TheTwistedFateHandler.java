package com.zzt.eternal_abyss.event;

import baubles.api.BaublesApi;
import com.zzt.eternal_abyss.init.ModAttributes;
import com.zzt.eternal_abyss.init.ModItems;
import com.zzt.eternal_abyss.items.DepthRing;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.UUID;

public class TheTwistedFateHandler {

    private static final UUID LUCK_MODIFIER_ID = UUID.fromString("deedface-1234-4567-89ab-feed0000face");

    private static final UUID CRIT_MODIFIER_ID = UUID.fromString("deedface-5241-4567-89ab-feed0000face");

    @SubscribeEvent
    public void onPlayerTick(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayerMP)) return;
        EntityPlayerMP player = (EntityPlayerMP) event.getEntityLiving();

        IAttributeInstance attr = player.getEntityAttribute(SharedMonsterAttributes.LUCK);
        if (attr == null) return;

        boolean hasTwistedFate = BaublesApi.isBaubleEquipped(player, ModItems.THE_TWISTED_FATE) != -1;
        ItemStack ring = DepthRingHandler.getEquippedDepthRing(player);
        boolean ringReversed = ring != null && DepthRing.isLuckyReversed(ring);

        // 先移除旧的 modifier
        AttributeModifier existing = attr.getModifier(LUCK_MODIFIER_ID);
        if (existing != null) {
            attr.removeModifier(existing);
        }

        // 应用新的幸运加成
        if (hasTwistedFate && ringReversed) {
            attr.applyModifier(new AttributeModifier(
                    LUCK_MODIFIER_ID,
                    "Curse_Lucky",
                    player.getLuck(),
                    0
            ));
        }

        double baseLuck = getBaseLuckWithoutSelf(player);

        IAttributeInstance critAttr = player.getEntityAttribute(ModAttributes.CRIT_CHANCE);
        AttributeModifier existingCrit = critAttr.getModifier(CRIT_MODIFIER_ID);
        if (existingCrit != null) {
            critAttr.removeModifier(existingCrit);
        }
        if (hasTwistedFate && ringReversed){
            critAttr.applyModifier(new AttributeModifier(
                    CRIT_MODIFIER_ID,
                    "luck_transform",
                    baseLuck*0.06,
                    0
                    ));
        }
    }
    public static double getBaseLuckWithoutSelf(EntityPlayer player) {
        IAttributeInstance luckAttr = player.getEntityAttribute(SharedMonsterAttributes.LUCK);
        if (luckAttr == null) return 0;

        double value = luckAttr.getBaseValue();  // 玩家幸运基础是 0

        for (AttributeModifier mod : luckAttr.getModifiers()) {
            // 跳过你自己的幸运 modifier
            if (mod.getID().equals(LUCK_MODIFIER_ID)) continue;

            if (mod.getOperation() == 0) {
                value += mod.getAmount();
            } else if (mod.getOperation() == 1) {
                value *= (1 + mod.getAmount());
            } else if (mod.getOperation() == 2) {
                value *= (1 + mod.getAmount());
            }
        }
        return value;
    }

}
