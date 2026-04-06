package com.zzt.eternal_abyss.event;

import baubles.api.BaublesApi;
import com.zzt.eternal_abyss.init.ModDamageSource;
import com.zzt.eternal_abyss.init.ModItems;
import com.zzt.eternal_abyss.init.ModSounds;
import com.zzt.eternal_abyss.items.DepthRing;
import com.zzt.eternal_abyss.items.WailingCore;
import com.zzt.eternal_abyss.util.BaubleSyncUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;


import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.UUID;

import static com.zzt.eternal_abyss.event.DepthRingHandler.getEquippedDepthRing;

@Mod.EventBusSubscriber
public class WailingCoreHandler {

    private static final String TAG_RAW_TICK   = "ca:wailing_raw_tick";
    private static final String TAG_RAW_AMOUNT = "ca:wailing_raw_amount";
    private static final UUID ARMOR_ADD = UUID.fromString("d28663c1-e415-97c8-91a8-52555d802a01");
    private static final UUID ARMOR_TOUGHNESS_ADD = UUID.fromString("d28663c1-e415-97c8-91a8-52555d802a02");
    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        ItemStack core = getEquippedWailingCore(player);
        if (core.isEmpty()) return;

        boolean reversed = isWailingReversed(player);
        DamageSource source = event.getSource();
        Entity attacker = source.getTrueSource();

        //穿戴哀鸣之核时免疫自身的 abyss_void 反伤
        if ("abyss_void".equals(source.getDamageType()) && source.getTrueSource() == player) {
            event.setCanceled(true);
            return; // 中止进一步处理
        }

        if (!reversed) {
            if (attacker instanceof EntityLivingBase && attacker.isEntityAlive() && player.world.rand.nextFloat() < 0.3f) {
                float reflect = event.getAmount() * 1.11f;
                ((EntityLivingBase) attacker).attackEntityFrom(ModDamageSource.abyssVoidWithOwner(player), reflect);
                spawnVoidParticles(player);
                player.world.playSound(null, player.posX, player.posY, player.posZ,
                        ModSounds.PARRY, player.getSoundCategory(), 1.0F, 1.2F);
            }

        } else {
            //先储存虚空伤害
            if (attacker instanceof EntityLivingBase && !(attacker instanceof EntityPlayer)) {
                float toStore = event.getAmount();
                NBTTagCompound data = player.getEntityData();
                long tick = player.world.getTotalWorldTime();
                if (data.getLong(TAG_RAW_TICK) == tick) {
                    toStore = data.getFloat(TAG_RAW_AMOUNT);
                }
                tryStoreVoidDamage(player, toStore);
            }

            // 然后再判断是否触发反伤 + 无敌
            if (attacker instanceof EntityLivingBase && attacker.isEntityAlive() && player.world.rand.nextFloat() < 0.6f) {
                float reflect = event.getAmount() * 3.33f;
                ((EntityLivingBase) attacker).attackEntityFrom(ModDamageSource.abyssVoidWithOwner(player), reflect);

                //设置无敌帧
                event.setCanceled(true);
                player.hurtResistantTime = 30;

                spawnVoidParticles(player);
                player.world.playSound(null, player.posX, player.posY, player.posZ,
                        ModSounds.PARRY, player.getSoundCategory(), 1.0F, 1.2F);
            }
        }
    }


    @SubscribeEvent
    public static void onPlayerAttack(LivingHurtEvent event) {
        if (event.getSource().isDamageAbsolute()) return;

        String damageType = event.getSource().getDamageType();
        if ("abyss_void".equals(damageType) || "abyss_sweep".equals(damageType)) return;

        if (!(event.getSource().getTrueSource() instanceof EntityPlayer)) return;
        if (event.getEntityLiving() instanceof EntityPlayer) return; // 保护 PvP

        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
        ItemStack core = getEquippedWailingCore(player);
        if (core.isEmpty()) return;

        if (!isWailingReversed(player)) return;

        // ✅ 附加虚空伤害（真实来源）
        float abyssDamage = Math.max(1.0f, event.getAmount() * 0.1f);
        event.getEntityLiving().attackEntityFrom(ModDamageSource.abyssVoidWithOwner(player), abyssDamage);

        // ✅ 判断 DepthRing 状态
        ItemStack ring = getEquippedDepthRing(player);
        if (ring.isEmpty() || !DepthRing.isHauntedReversed(ring)) return;

        int stored = WailingCore.getStoredVoidDamage(core);
        int used = WailingCore.getVoidEchoHitsUsed(core);

        if (stored > 0 && used < 10) {
            int remainingUses = 10 - used;
            float bonus = Math.max(1.0f, stored / (float) remainingUses);

            // 增加主伤害
            event.setAmount(event.getAmount() + bonus);

            // 扣除等量虚空储存
            int newStored = Math.max(0, stored - Math.round(bonus));
            WailingCore.setStoredVoidDamage(core, newStored);

            // 增加使用次数
            WailingCore.setVoidEchoHitsUsed(core, used + 1);

            // 播放音效
            player.world.playSound(null, player.posX, player.posY, player.posZ,
                    SoundEvents.ENTITY_WITHER_SHOOT, player.getSoundCategory(), 0.7F, 1.6F);

            // 判断是否消耗完毕
            if (used + 1 >= 10 || newStored <= 0) {
                WailingCore.setStoredVoidDamage(core, 0);
                WailingCore.setVoidEchoHitsUsed(core, 0);

                player.world.playSound(null, player.posX, player.posY, player.posZ,
                        SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, player.getSoundCategory(), 0.6F, 0.5F);
            }
        }

    }


    @SubscribeEvent
    public static void onPlayerAttacked(LivingAttackEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();

        // 只记录：佩戴哀鸣之核 + 已反转（你要的储存逻辑只在反转时用）
        ItemStack core = getEquippedWailingCore(player);
        if (core.isEmpty()) return;
        if (!isWailingReversed(player)) return;

        // 记录这一击的“更早期”的伤害值
        NBTTagCompound data = player.getEntityData();
        data.setLong(TAG_RAW_TICK, player.world.getTotalWorldTime());
        data.setFloat(TAG_RAW_AMOUNT, event.getAmount());
    }


//    @SubscribeEvent
//    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
//        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;
//
//        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
//        ItemStack core = getEquippedWailingCore(player);
//        if (core.isEmpty()) return;
//        if (!isWailingReversed(player)) return;
//
//        Potion bleed = Potion.getPotionFromResourceLocation("lycanitesmobs:bleed");
//        if (bleed != null && player.isPotionActive(bleed)) {
//            player.removePotionEffect(bleed);
//        }
//    }
    public static boolean isWailingReversed(EntityPlayer player) {
        int slot = BaublesApi.isBaubleEquipped(player, ModItems.WAILING_CORE);
        if (slot == -1) return false;

        ItemStack core = BaublesApi.getBaublesHandler(player).getStackInSlot(slot);
        if (core.isEmpty()) return false;

        ItemStack ring = getEquippedDepthRing(player);
        return !ring.isEmpty() && DepthRing.isHauntedReversed(ring);
    }

    public static ItemStack getEquippedWailingCore(EntityPlayer player) {
        for (int i = 0; i < BaublesApi.getBaublesHandler(player).getSlots(); i++) {
            ItemStack stack = BaublesApi.getBaublesHandler(player).getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == ModItems.WAILING_CORE) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static void spawnVoidParticles(EntityPlayer player) {
        for (int i = 0; i < 20; i++) {
            double dx = (player.world.rand.nextDouble() - 0.5) * 0.5;
            double dy = player.world.rand.nextDouble() * 1.2;
            double dz = (player.world.rand.nextDouble() - 0.5) * 0.5;
            player.world.spawnParticle(EnumParticleTypes.PORTAL,
                    player.posX + dx,
                    player.posY + dy,
                    player.posZ + dz,
                    0, 0.01, 0);
        }
    }

    public static void tryStoreVoidDamage(EntityPlayer player, float damage) {
        ItemStack core = getEquippedWailingCore(player);
        if (core.isEmpty()) return;

        ItemStack ring = getEquippedDepthRing(player);
        if (!ring.isEmpty() && DepthRing.hasCurse(ring, "HAUNTED_SHADOWS") && DepthRing.isHauntedReversed(ring)) {

            int current = WailingCore.getStoredVoidDamage(core);
            int added = Math.round(damage)*20;
            int newTotal = Math.min(current + added, 2000000);
            WailingCore.setStoredVoidDamage(core, newTotal);

            // NBT 同步
            if (player instanceof EntityPlayerMP) {
                BaubleSyncUtil.syncBaubleNBT((EntityPlayerMP) player, core);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        EntityPlayer player = event.player;
        if(getEquippedWailingCore(event.player).isEmpty())return;




        IAttributeInstance armor = player.getEntityAttribute(SharedMonsterAttributes.ARMOR);
        IAttributeInstance armor_toughness = player.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS);

        AttributeModifier armor_modifier = armor.getModifier(ARMOR_ADD);
        AttributeModifier armor_toughness_modifier = armor_toughness.getModifier(ARMOR_TOUGHNESS_ADD);

        if (armor_modifier != null){
            armor.removeModifier(armor_modifier);
        }
        if (armor_toughness_modifier != null){
            armor_toughness.removeModifier(armor_toughness_modifier);
        }

        armor.applyModifier(new AttributeModifier(
                ARMOR_ADD,
                "WailingCoreArmor",
                1,
                1
        ));

        armor_toughness.applyModifier(new AttributeModifier(
                ARMOR_TOUGHNESS_ADD,
                "WailingCoreArmorToughness",
                0.5,
                1
        ));
    }
}
