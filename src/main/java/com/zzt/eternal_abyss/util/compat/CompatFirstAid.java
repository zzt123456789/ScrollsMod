package com.zzt.eternal_abyss.util.compat;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.CapabilityExtendedHealthSystem;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.network.MessageSyncDamageModel;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * 完全原生 FirstAid 写法
 * 每个部位锁定最大生命值的 20%
 * 与 HalfHeartMask 完全一致，只改了比例
 */

public class CompatFirstAid {

    /** 锁血比例：20% */
    private static final float RATE = 0.2F;

    /** 每 tick 调用：锁血 */

    public static void apply20pct(EntityPlayerMP player) {

        AbstractPlayerDamageModel damageModel =
                player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null);

        if (damageModel == null) return;

        for (final EnumPlayerPart partEnum : EnumPlayerPart.values()) {

            final AbstractDamageablePart part = damageModel.getFromEnum(partEnum);
            if (part == null) continue;

            if (part.currentHealth > part.getMaxHealth() * RATE) {
                part.currentHealth = part.getMaxHealth() * RATE;
                FirstAid.NETWORKING.sendTo(new MessageSyncDamageModel(damageModel, true), player);
            }
        }
    }

    public static void removeCap(EntityPlayerMP player) {

        AbstractPlayerDamageModel damageModel =
                player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null);

        if (damageModel == null) return;

        boolean changed = false;

        for (EnumPlayerPart partEnum : EnumPlayerPart.values()) {

            AbstractDamageablePart part = damageModel.getFromEnum(partEnum);
            if (part == null) continue;

            float max = part.getMaxHealth();
            float cur = part.currentHealth;

            if (cur <= max * RATE + 0.1F) {
                part.currentHealth = max;
                changed = true;
            }
        }

        if (changed) {
            FirstAid.NETWORKING.sendTo(new MessageSyncDamageModel(damageModel, true), player);
        }
    }
}
