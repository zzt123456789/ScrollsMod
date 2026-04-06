package com.zzt.eternal_abyss.event;

import baubles.api.BaublesApi;
import com.zzt.eternal_abyss.init.ModAttributes;
import com.zzt.eternal_abyss.init.ModItems;
import com.zzt.eternal_abyss.init.ModSounds;
import com.zzt.eternal_abyss.items.DepthRing;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.UUID;

public class TheDiminishedShadeHandler {

    private static final UUID CRIT_DAMAGE_ID = UUID.fromString("b0725cef-21d0-422d-9b09-812c049d9a23");

    /**
     * 防止把“转化后的普通伤害”再次进入本事件，导致无限递归
     */
    private static final String TAG_SHADE_CONVERTING = "ca:shade_converting";

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        EntityPlayer player = event.player;
        if (player.world.isRemote) return;

        boolean hasShade = BaublesApi.isBaubleEquipped(player, ModItems.THE_DIMINISHED_SHADE) != -1;
        if (!hasShade) {
            removeModifier(player);
            return;
        }

        if (!DepthRing.isVoidResistReversedFor(player)) {
            removeModifier(player);
            return;
        }

        double max = player.getMaxHealth();
        double current = player.getHealth();
        double lost = max - current;

        // 每损失 6 点生命，暴伤 +0.09
        int multiple = (int) (lost / 6.0);
        double bonus = multiple * 0.09D;

        IAttributeInstance critDmg = player.getEntityAttribute(ModAttributes.CRIT_DAMAGE);
        if (critDmg == null) return;

        AttributeModifier old = critDmg.getModifier(CRIT_DAMAGE_ID);
        if (old != null) {
            critDmg.removeModifier(old);
        }

        if (bonus > 0) {
            critDmg.applyModifier(new AttributeModifier(
                    CRIT_DAMAGE_ID,
                    "Crit_Dmg_bonus",
                    bonus,
                    0
            ));
        }
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        if (player.world.isRemote) return;

        boolean hasShade = BaublesApi.isBaubleEquipped(player, ModItems.THE_DIMINISHED_SHADE) != -1;
        if (!hasShade) return;

        // 防止递归：如果当前伤害就是“转化后补打的普通伤害”，则直接跳过
        if (isConverting(player)) return;

        DamageSource source = event.getSource();
        if (!isAbsoluteDamage(source)) return;

        float amount = event.getAmount();
        if (amount <= 0.0F) return;

        // 取消原始真实伤害 / 不可格挡伤害
        event.setCanceled(true);

        // 播放音效
        player.world.playSound(
                null,
                player.posX, player.posY, player.posZ,
                ModSounds.PARRY,
                SoundCategory.PLAYERS,
                0.8F, 1.2F
        );

        // 重新以“普通物理伤害”结算
        try {
            setConverting(player, true);
            player.attackEntityFrom(createConvertedPhysicalSource(source), amount);
        } finally {
            setConverting(player, false);
        }
    }

    private void removeModifier(EntityPlayer player) {
        IAttributeInstance critDmg = player.getEntityAttribute(ModAttributes.CRIT_DAMAGE);
        if (critDmg == null) return;

        AttributeModifier old = critDmg.getModifier(CRIT_DAMAGE_ID);
        if (old != null) {
            critDmg.removeModifier(old);
        }
    }

    /**
     * 判断是否为真实伤害 / 不可格挡伤害
     */
    private boolean isAbsoluteDamage(DamageSource source) {
        return source != null && (source.isDamageAbsolute() || source.isUnblockable());
    }

    /**
     * 创建一个“普通物理伤害”的 DamageSource
     * 不调用 setDamageBypassesArmor / setDamageIsAbsolute / setMagicDamage
     * 这样就会正常走护甲、抗性等减伤流程
     */
    private DamageSource createConvertedPhysicalSource(DamageSource original) {
        DamageSource converted = new DamageSource("shade_converted");

        // 下面这些标签可按需保留
        if (original != null) {
            if (original.isProjectile()) {
                converted.setProjectile();
            }
            if (original.isExplosion()) {
                converted.setExplosion();
            }
            // 这里故意不保留 magic / absolute / unblockable
            // 因为你要的是“普通物理伤害”
        }

        return converted;
    }

    private boolean isConverting(EntityPlayer player) {
        NBTTagCompound data = player.getEntityData();
        return data.getBoolean(TAG_SHADE_CONVERTING);
    }

    private void setConverting(EntityPlayer player, boolean value) {
        NBTTagCompound data = player.getEntityData();
        data.setBoolean(TAG_SHADE_CONVERTING, value);
    }
}