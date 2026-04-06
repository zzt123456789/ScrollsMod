package com.zzt.eternal_abyss.event;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import com.zzt.eternal_abyss.config.ModConfig;
import com.zzt.eternal_abyss.init.ModAttributes;
import com.zzt.eternal_abyss.init.ModItems;
import com.zzt.eternal_abyss.init.ModSounds;
import com.zzt.eternal_abyss.items.DepthRing;
import com.zzt.eternal_abyss.util.BaubleSyncUtil;
import com.zzt.eternal_abyss.util.ImmunityHelper;
import com.zzt.eternal_abyss.util.compat.CompatFirstAid;
import com.zzt.eternal_abyss.util.compat.ModCompat;
import com.zzt.eternal_abyss.util.compat.ScalingHealthCompat;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.silentchaos512.scalinghealth.ScalingHealth;

import java.util.*;

@Mod.EventBusSubscriber
public class DepthRingHandler {

    private static final UUID ATTACK_DAMAGE_MODIFIER_ID = UUID.fromString("a4e6c2a4-0b1a-4b8a-8e6e-9c0f6d8c1d23");
    private static final UUID ATTACK_SPEED_MODIFIER_ID = UUID.fromString("c6a7d1c9-bf92-4a21-bb2d-ff418afcfdf1");
    private static final UUID MOVEMENT_SPEED_MODIFIER_ID = UUID.fromString("e4b6c441-38ec-4e77-b111-0631a31ff45e");
    private static final UUID LUCK_MODIFIER_ID = UUID.fromString("7e8b0db1-3a7b-4f4e-9b5e-07b53d8d9a11");

    private static final Map<UUID, Long> DodgeTick = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;

        EntityPlayer player = event.player;
        ItemStack ring = getEquippedDepthRing(player);
        if (ring.isEmpty()) {
            removeAttackSpeedModifier(player);
            removeMovementSpeedModifier(player);
            removeAttackDamageModifier(player);
            removeLuckyModifier(player);
            return;
        }

        applyAttackSpeedEffect(player, ring);
        // applyAttackDamageEffect(player, ring);
        applyLuckyEffect(player, ring);

        String hauntedKey = "HAUNTED_SHADOWS";
        ModConfig.CurseConfig hauntedCfg = ModConfig.getCurseConfig(hauntedKey);
        if (hauntedCfg != null && DepthRing.getKillCount(ring, hauntedKey) >= 0) {
            IAttributeInstance speedAttr = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
            AttributeModifier existingSpeed = speedAttr.getModifier(MOVEMENT_SPEED_MODIFIER_ID);
            if (existingSpeed != null)
                speedAttr.removeModifier(existingSpeed);

            double amount = DepthRing.isHauntedReversed(ring)
                    ? ModConfig.hauntedMoveSpeedReversed
                    : ModConfig.hauntedMoveSpeedCurse;

            speedAttr.applyModifier(new AttributeModifier(
                    MOVEMENT_SPEED_MODIFIER_ID,
                    "Haunted_Shadow_Speed",
                    amount,
                    2));

        }

    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getTrueSource() instanceof EntityPlayer))
            return;

        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
        ItemStack ring = getEquippedDepthRing(player);
        if (ring.isEmpty())
            return;

        // 忽略 PvP 的伤害增强/减弱机制
        if (event.getEntityLiving() instanceof EntityPlayer)
            return;

        boolean reversed = DepthRing.isDamageReversed(ring);
        if (!reversed) {
            event.setAmount(event.getAmount() * (float) ModConfig.damageCurseMultiplier);
        } else {
            event.setAmount(event.getAmount() * (float) ModConfig.damageReversedMultiplier);
        }

        event.setAmount(Math.max(event.getAmount(), 0.1F));

        String hauntedKey = "HAUNTED_SHADOWS";
        if (DepthRing.isHauntedReversed(ring)) {
            if (event.getSource().getImmediateSource() instanceof EntityArrow) {
                EntityArrow arrow = (EntityArrow) event.getSource().getImmediateSource();

                // 计算箭矢当前的三维速度
                double vx = arrow.motionX;
                double vy = arrow.motionY;
                double vz = arrow.motionZ;
                double arrowSpeed = Math.sqrt(vx * vx + vy * vy + vz * vz);

                float extra = (float) (arrowSpeed
                        * ModConfig.hauntedArrowSpeedFactor
                        * ModConfig.hauntedArrowPercent
                        * ModConfig.hauntedArrowBonusRate);

                event.setAmount(event.getAmount() + extra);

            }
        }
    }

    // 在类中添加一个线程局部变量来跟踪是否正在处理伤害事件
    private static final ThreadLocal<Boolean> processingVoidDamage = ThreadLocal.withInitial(() -> false);

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer))
            return;
        if (processingVoidDamage.get())
            return;

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        ItemStack ring = getEquippedDepthRing(player);
        if (ring.isEmpty())
            return;

        float originalDamage = event.getAmount();
        float maxHealth = player.getMaxHealth();
        Random rand = player.getRNG();

        if (!DepthRing.isCurseReversed(ring, "VOID_RESISTANCE") && !ImmunityHelper.hasUltimateItem(player)) {

            float factor = (float) ModConfig.voidBaseHealthPercent;
            float min = 0.10f; // 写死下限，不配置

            int shadeSlot = BaublesApi.isBaubleEquipped(player, ModItems.THE_DIMINISHED_SHADE);
            if (shadeSlot != -1) {

                // 每 5 最大生命值 = 1% 压缩进度
                float compress = (maxHealth / 5f) * 0.01f;
                compress = Math.min(compress, 0.30f); // 最大 30%

                // 只压缩“factor → min”之间的距离
                float gap = factor - min;
                if (gap > 0) {
                    factor = factor - gap * compress;
                }

                // 保底
                factor = Math.max(factor, min);
            }

            // ✅ 关键：把 factor 用起来
            float fixedDamage = maxHealth * factor;
            if (originalDamage < fixedDamage) {
                event.setAmount(fixedDamage);
            }

            return;
        }

        // ======== 已反转逻辑 ========
        int reversedCount = DepthRing.getReversedCurseCount(ring);
        float reduction = (float) ModConfig.voidReversedBaseReduction
                + (float) ModConfig.voidReversedReductionPerCurse * reversedCount;

        reduction = Math.min(reduction, 0.9f);

        float chance = 0.04f
                + (float) ModConfig.voidReversedDodgePerCurse * reversedCount;

        chance = Math.min(chance, 0.30f);

        if (rand.nextFloat() < chance) {
            event.setCanceled(true);
            DodgeTick.put(player.getUniqueID(), player.world.getTotalWorldTime());

            player.world.playSound(
                    null, player.posX, player.posY, player.posZ,
                    ModSounds.PARRY,
                    player.getSoundCategory(), 1.0F, 1.2F);
            return;
        }

        // 按减伤比例调整伤害
        event.setAmount(originalDamage * (1 - reduction));
    }

    @SubscribeEvent
    public static void onPlayerKnockBack(LivingKnockBackEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer))
            return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();

        Long tick = DodgeTick.get(player.getUniqueID());
        if (tick != null && tick == player.world.getTotalWorldTime()) {
            // 本 tick 内刚刚闪避成功 → 取消击退
            event.setStrength(0.0F);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getSource() == null || !(event.getSource().getTrueSource() instanceof EntityPlayer))
            return;

        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
        IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
        if (handler == null)
            return;

        int slot = BaublesApi.isBaubleEquipped(player, ModItems.DEPTH_RING);
        if (slot == -1)
            return;

        ItemStack ring = handler.getStackInSlot(slot);
        if (ring.isEmpty())
            return;

        ResourceLocation key = EntityList.getKey(event.getEntityLiving());
        if (key == null)
            return;
        String killedEntityId = key.toString();

        String damageKey = "DAMAGE_SPEED";
        ModConfig.CurseConfig damageCfg = ModConfig.getCurseConfig(damageKey);
        if (damageCfg != null && damageCfg.targets != null && damageCfg.targets.contains(killedEntityId)) {
            DepthRing.addKill(ring, damageKey);

            if (!DepthRing.isDamageReversed(ring)
                    && DepthRing.getKillCount(ring, damageKey) >= damageCfg.requiredKills) {
                DepthRing.setDamageReversed(ring);
                if (!player.getEntityData().getBoolean("has_final_artifact_notice")) {
                    DepthRing.checkCurse(player);
                }
                player.world.playSound(
                        null,
                        player.posX, player.posY, player.posZ,
                        SoundEvents.ENTITY_ENDERMEN_DEATH,
                        player.getSoundCategory(),
                        1.0F, 1.0F);
                player.sendStatusMessage(new TextComponentString(
                        TextFormatting.YELLOW + "苦痛压制的诅咒已被解除"), true);
            }
        }

        String expKey = "EXPERIENCE_DROP";
        ModConfig.CurseConfig expCfg = ModConfig.getCurseConfig(expKey);
        if (expCfg != null && expCfg.targets.contains(killedEntityId)) {
            DepthRing.addKill(ring, expKey);
            if (!DepthRing.isExpBoostReversed(ring) && DepthRing.getKillCount(ring, expKey) >= expCfg.requiredKills) {
                DepthRing.setExpBoostReversed(ring);
                if (!player.getEntityData().getBoolean("has_final_artifact_notice")) {
                    DepthRing.checkCurse(player);
                }
                player.world.playSound(
                        null,
                        player.posX, player.posY, player.posZ,
                        SoundEvents.ENTITY_WITHER_DEATH,
                        player.getSoundCategory(),
                        1.0F, 1.0F);
                player.sendStatusMessage(new TextComponentString(
                        TextFormatting.YELLOW + "知识侵蚀的诅咒已被解除"), true);
            }
        }

        String hauntedKey = "HAUNTED_SHADOWS";
        ModConfig.CurseConfig hauntedCfg = ModConfig.getCurseConfig(hauntedKey);
        if (hauntedCfg != null && hauntedCfg.targets.contains(killedEntityId)) {
            DepthRing.addKill(ring, hauntedKey);
            if (!player.getEntityData().getBoolean("has_final_artifact_notice")) {
                DepthRing.checkCurse(player);
            }
            if (!DepthRing.isHauntedReversed(ring)
                    && DepthRing.getKillCount(ring, hauntedKey) >= hauntedCfg.requiredKills) {
                DepthRing.setHauntedReversed(ring);
                player.world.playSound(
                        null,
                        player.posX, player.posY, player.posZ,
                        SoundEvents.ENTITY_ENDERDRAGON_DEATH,
                        player.getSoundCategory(),
                        1.0F, 1.0F);
                player.sendStatusMessage(new TextComponentString(
                        TextFormatting.YELLOW + "怨影缠身的诅咒已被解除"), true);
            }
        }

        String voidKey = "VOID_RESISTANCE";
        ModConfig.CurseConfig voidCfg = ModConfig.getCurseConfig(voidKey);
        if (voidCfg != null && voidCfg.targets.contains(killedEntityId)) {
            DepthRing.addKill(ring, voidKey);
            if (!DepthRing.isVoidResistReversed(ring)
                    && DepthRing.getKillCount(ring, voidKey) >= voidCfg.requiredKills) {
                DepthRing.setVoidResistReversed(ring);
                if (!player.getEntityData().getBoolean("has_final_artifact_notice")) {
                    DepthRing.checkCurse(player);
                }
                player.world.playSound(null, player.posX, player.posY, player.posZ,
                        SoundEvents.ENTITY_WITHER_DEATH,
                        player.getSoundCategory(), 1.0F, 1.0F);
                player.sendStatusMessage(new TextComponentString(
                        TextFormatting.YELLOW + "虚空腐蚀的诅咒已被解除"), true);
            }
        }

        String finalKey = "FINAL";
        ModConfig.CurseConfig finalCfg = ModConfig.getCurseConfig(finalKey);
        if (finalCfg != null && finalCfg.targets.contains(killedEntityId)) {
            DepthRing.addKill(ring, finalKey);

            if (!DepthRing.isFinalReversed(ring)
                    && DepthRing.getKillCount(ring, finalKey) >= finalCfg.requiredKills
                    // 这里是关键：必须所有基础诅咒都已反转
                    && DepthRing.allBaseCursesReversed(ring)) {

                DepthRing.setFinalReversed(ring);

                // 只给一次
                if (!player.getEntityData().getBoolean("has_final_artifact")) {
                    ItemStack finalItem = new ItemStack(ModItems.VOID_ARTIFACT);
                    if (!player.inventory.addItemStackToInventory(finalItem)) {
                        player.dropItem(finalItem, false);
                    }
                    player.getEntityData().setBoolean("has_final_artifact", true);

                    player.world.playSound(null, player.posX, player.posY, player.posZ,
                            SoundEvents.ENTITY_ENDERDRAGON_DEATH,
                            player.getSoundCategory(), 1.0F, 1.0F);
                    player.sendStatusMessage(
                            new TextComponentString(TextFormatting.GOLD + "终极深渊的诅咒已被解除"), true);
                }
            }
        }

        if (!player.world.isRemote) {
            BaubleSyncUtil.syncNBTToClient((EntityPlayerMP) player, slot, ring);
        }
    }

    @SubscribeEvent
    public static void onFoodEaten(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer))
            return;

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        ItemStack eaten = event.getItem();

        // 仅处理真正的食物
        if (!(eaten.getItem() instanceof ItemFood))
            return;

        // 直接写死目标食物 ID
        ResourceLocation eatenId = Item.REGISTRY.getNameForObject(eaten.getItem());
        if (eatenId == null || !eatenId.toString().equals("eternal_abyss:clover"))
            return;

        ItemStack ring = getEquippedDepthRing(player);
        if (ring.isEmpty())
            return;

        // ====== 幸运诅咒处理 ======
        String luckKey = "LUCKY";

        // 从配置读取需要的进食次数
        ModConfig.CurseConfig luckyCfg = ModConfig.getCurseConfig(luckKey);
        int required = (luckyCfg != null) ? luckyCfg.requiredKills : 5; // 默认 5

        DepthRing.addKill(ring, luckKey);
        int count = DepthRing.getKillCount(ring, luckKey);

        // 达到要求则反转
        if (!DepthRing.isLuckyReversed(ring) && count >= required) {
            DepthRing.setLucky(ring);
            if (!player.getEntityData().getBoolean("has_final_artifact_notice")) {
                DepthRing.checkCurse(player);
            }
            player.world.playSound(null, player.posX, player.posY, player.posZ,
                    SoundEvents.ENTITY_PLAYER_LEVELUP,
                    player.getSoundCategory(), 1.0F, 1.0F);
            player.sendStatusMessage(
                    new TextComponentString(TextFormatting.GOLD + "厄运不断已因进食而解除"), true);
        }

        // 同步到客户端
        if (!player.world.isRemote) {
            int slot = BaublesApi.isBaubleEquipped(player, ModItems.DEPTH_RING);
            if (slot != -1) {
                BaubleSyncUtil.syncNBTToClient((EntityPlayerMP) player, slot, ring);
            }
        }
    }

    public static ItemStack getEquippedDepthRing(EntityPlayer player) {
        int slot = BaublesApi.isBaubleEquipped(player, ModItems.DEPTH_RING);
        if (slot != -1) {
            return BaublesApi.getBaublesHandler(player).getStackInSlot(slot);
        }
        return ItemStack.EMPTY;
    }

    private static void applyAttackSpeedEffect(EntityPlayer player, ItemStack ring) {
        IAttributeInstance attr = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED);
        AttributeModifier existing = attr.getModifier(ATTACK_SPEED_MODIFIER_ID);
        if (existing != null)
            attr.removeModifier(existing);

        if (!DepthRing.isDamageReversed(ring)) {
            attr.applyModifier(new AttributeModifier(
                    ATTACK_SPEED_MODIFIER_ID,
                    "Curse_AttackSpeed",
                    ModConfig.attackSpeedCurse,
                    2));
        } else {
            attr.applyModifier(new AttributeModifier(
                    ATTACK_SPEED_MODIFIER_ID,
                    "Buff_AttackSpeed",
                    ModConfig.attackSpeedReversed,
                    2));
        }

    }

    // private static void applyAttackDamageEffect(EntityPlayer player, ItemStack
    // ring) {
    // IAttributeInstance attr =
    // player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
    // if (attr == null) return;
    //
    // // 先移除旧的
    // AttributeModifier existing = attr.getModifier(ATTACK_DAMAGE_MODIFIER_ID);
    // if (existing != null) attr.removeModifier(existing);
    //
    // if (!DepthRing.isDamageReversed(ring)) {
    // attr.applyModifier(new AttributeModifier(
    // ATTACK_DAMAGE_MODIFIER_ID,
    // "Curse_AttackDamage",
    // -0.20D,
    // 2
    // ));
    // } else {
    // attr.applyModifier(new AttributeModifier(
    // ATTACK_DAMAGE_MODIFIER_ID,
    // "Buff_AttackDamage",
    // 0.30D,
    // 2
    // ));
    // }
    // }

    private static void applyLuckyEffect(EntityPlayer player, ItemStack ring) {
        IAttributeInstance attr = player.getEntityAttribute(SharedMonsterAttributes.LUCK);

        boolean EquipTwistedFate = BaublesApi.isBaubleEquipped(player, ModItems.THE_TWISTED_FATE) != -1;

        if (attr == null)
            return;

        AttributeModifier existing = attr.getModifier(LUCK_MODIFIER_ID);
        if (existing != null)
            attr.removeModifier(existing);

        if (DepthRing.isLuckyReversed(ring)) {
            attr.applyModifier(new AttributeModifier(
                    LUCK_MODIFIER_ID,
                    "Buff_Lucky",
                    5.0D,
                    0));
        } else {
            if (!EquipTwistedFate) {
                float luckValue = player.getLuck();
                attr.applyModifier(new AttributeModifier(
                        LUCK_MODIFIER_ID,
                        "Curse_Lucky",
                        -luckValue,
                        0));
            } else {
                float current_luck = player.getLuck();
                attr.applyModifier(new AttributeModifier(
                        LUCK_MODIFIER_ID,
                        "Curse_Lucky",
                        10.0D - current_luck,
                        0));
            }
        }
    }



    private static void removeAttackDamageModifier(EntityPlayer player) {
        IAttributeInstance attr = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        if (attr != null) {
            AttributeModifier existing = attr.getModifier(ATTACK_DAMAGE_MODIFIER_ID);
            if (existing != null)
                attr.removeModifier(existing);
        }
    }

    private static void removeAttackSpeedModifier(EntityPlayer player) {
        IAttributeInstance attr = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED);
        AttributeModifier existing = attr.getModifier(ATTACK_SPEED_MODIFIER_ID);
        if (existing != null)
            attr.removeModifier(existing);
    }

    private static void removeMovementSpeedModifier(EntityPlayer player) {
        IAttributeInstance attr = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (attr != null) {
            AttributeModifier existing = attr.getModifier(MOVEMENT_SPEED_MODIFIER_ID);
            if (existing != null)
                attr.removeModifier(existing);
        }
    }

    private static void removeLuckyModifier(EntityPlayer player) {
        IAttributeInstance attr = player.getEntityAttribute(SharedMonsterAttributes.LUCK);
        if (attr != null) {
            AttributeModifier existing = attr.getModifier(LUCK_MODIFIER_ID);
            if (existing != null)
                attr.removeModifier(existing);
        }
    }



    public static void spawnExtraXP(World world, BlockPos pos, int amount) {
        while (amount > 0) {
            int split = EntityXPOrb.getXPSplit(amount);
            amount -= split;
            world.spawnEntity(new EntityXPOrb(
                    world,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    split));
        }
    }

    @SubscribeEvent
    public static void onExperienceDrop(LivingExperienceDropEvent event) {

        EntityPlayer player = event.getAttackingPlayer();
        if (player == null)
            return;

        ItemStack ring = getEquippedDepthRing(player);
        if (ring.isEmpty())
            return;

        int baseExp = event.getOriginalExperience();
        if (baseExp <= 0)
            return;

        World world = player.world;
        if (world.isRemote)
            return;

        Random rand = player.getRNG();

        EntityLivingBase target = event.getEntityLiving();

        if (!DepthRing.isExpBoostReversed(ring)
                && !(ModCompat.isScalingHealthLoaded()
                && ScalingHealthCompat.isEntityBlight(target))){
            int reduced = Math.max(1, (int) (baseExp * ModConfig.expCurseMultiplier));
            event.setDroppedExperience(reduced);
            return;
        }

        // ===== 已反转：概率倍增 =====
        float roll = rand.nextFloat();
        int bonusExp;

        if (roll <= ModConfig.expReversedChanceX4) {
            bonusExp = baseExp * ModConfig.expReversedMultiplierX4;
            player.sendStatusMessage(
                    new TextComponentString(TextFormatting.GREEN + "4!"), true);
        } else if (roll <= ModConfig.expReversedChanceX4 + ModConfig.expReversedChanceX2) {
            bonusExp = baseExp * ModConfig.expReversedMultiplierX2;
            player.sendStatusMessage(
                    new TextComponentString(TextFormatting.GREEN + "2!"), true);
        } else {
            bonusExp = baseExp;
        }
        if (bonusExp > 0) {
            spawnExtraXP(world, event.getEntityLiving().getPosition(), bonusExp);
        }

    }

    @SubscribeEvent
    public static void onMobTargetPlayer(LivingSetAttackTargetEvent event) {
        if (!(event.getTarget() instanceof EntityPlayer))
            return;
        if (!(event.getEntityLiving() instanceof net.minecraft.entity.monster.IMob))
            return;

        EntityPlayer player = (EntityPlayer) event.getTarget();
        ItemStack ring = getEquippedDepthRing(player);
        if (ring.isEmpty())
            return;

        if (DepthRing.hasCurse(ring, "HAUNTED_SHADOWS") && !DepthRing.isHauntedReversed(ring)) {
            IAttributeInstance attr = event.getEntityLiving().getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
            if (attr != null) {
                double base = attr.getBaseValue();
                if (base < 16.0D + ModConfig.hauntedAggroRangeBonus) {
                    attr.setBaseValue(16.0D + ModConfig.hauntedAggroRangeBonus);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        EntityPlayer killer = event.getSource().getTrueSource() instanceof EntityPlayer
                ? (EntityPlayer) event.getSource().getTrueSource()
                : null;
        if (killer == null)
            return;

        ItemStack ring = getEquippedDepthRing(killer);
        if (ring.isEmpty() || ring.getItem() != ModItems.DEPTH_RING)
            return;
        if (!DepthRing.isLuckyReversed(ring))
            return;

        float mul = getLuckyDropMultiplier(killer);
        if (mul <= 1.0F)
            return;

        List<EntityItem> extraItems = new ArrayList<>();

        for (EntityItem item : event.getDrops()) {

            if (item.getThrower() != null && !item.getThrower().isEmpty()) {
                continue;
            }

            int extra = Math.round((mul - 1F) * item.getItem().getCount());
            if (extra > 0) {
                ItemStack bonus = item.getItem().copy();
                bonus.setCount(extra);
                extraItems.add(new EntityItem(item.world, item.posX, item.posY, item.posZ, bonus));
            }
        }

        event.getDrops().addAll(extraItems);
    }

    public static float getLuckyDropMultiplier(EntityPlayer player) {
        float luck = player.getLuck();
        return 1.0F + (luck / 5) * 0.33F;
    }
}
