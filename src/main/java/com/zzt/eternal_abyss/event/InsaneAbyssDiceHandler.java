package com.zzt.eternal_abyss.event;

import baubles.api.BaublesApi;
import com.zzt.eternal_abyss.init.ModDamageSource;
import com.zzt.eternal_abyss.init.ModItems;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber
public class InsaneAbyssDiceHandler {

    private static final UUID INSANE_DICE_MODIFIER_ID = UUID.fromString("d23ffa12-1ea2-52ec-89ab-feed2321face");
    @SubscribeEvent
    public static void onPlayerAttack(LivingHurtEvent event) {

        if ("abyss_sweep".equals(event.getSource().getDamageType())) {
            return;
        }

        if (!(event.getSource().getTrueSource() instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();

        if (event.getEntityLiving() instanceof EntityPlayer) {
            return;
        }

        if (!hasInsaneDiceEquipped(player)) {
            return;
        }


        int luck = (int) player.getLuck();
        float chance = 0.2f + ((luck > 0) ? ((1.0f + luck) / (28.0f + luck)) : 0.0f);
        if (player.world.rand.nextFloat() < chance) {
            float baseDamage = event.getAmount();
            float doubleDamage = baseDamage * 2.0f;
            event.setAmount(doubleDamage);

            player.world.playSound(null, player.posX, player.posY, player.posZ,
                    SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, player.getSoundCategory(), 1.0F, 1.2F);

            sweepAttack(player, event.getEntityLiving(), doubleDamage);

        }
    }

    private static void sweepAttack(EntityPlayer player, EntityLivingBase mainTarget, float damage) {
        if (mainTarget == null || !mainTarget.isEntityAlive()) return;

        double radius = 3.0;
        AxisAlignedBB area = mainTarget.getEntityBoundingBox().grow(radius, 1.0, radius);

        List<EntityLivingBase> targets = player.world.getEntitiesWithinAABB(EntityLivingBase.class, area,
                e -> e != player && e != mainTarget && e.isEntityAlive() && player.canEntityBeSeen(e));

        for (EntityLivingBase target : targets) {
            target.attackEntityFrom(ModDamageSource.abyssSweepWithPlayer(player), damage);
        }

        //不管是否命中，都播放音效和粒子
        player.world.playSound(null, mainTarget.posX, mainTarget.posY, mainTarget.posZ,
                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, player.getSoundCategory(), 1.0F, 1.0F);

        if (player instanceof EntityPlayerMP) {
            ((EntityPlayerMP) player).spawnSweepParticles();
        }
    }

    private static boolean hasInsaneDiceEquipped(EntityPlayer player) {
        for (int i = 0; i < BaublesApi.getBaublesHandler(player).getSlots(); i++) {
            ItemStack stack = BaublesApi.getBaublesHandler(player).getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == ModItems.INSANE_ABYSS_DICE) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        EntityPlayer player = event.player;
        if(hasInsaneDiceEquipped(event.player))return;


        IAttributeInstance attr = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        AttributeModifier existing = attr.getModifier(INSANE_DICE_MODIFIER_ID);
        if (existing != null){
            attr.removeModifier(existing);
        }

        if(hasInsaneDiceEquipped(player)){
            attr.applyModifier(
                    new AttributeModifier(
                            INSANE_DICE_MODIFIER_ID,
                            "Insane_Dice",
                            6.0,
                            0)
            );
        }
    }


}
