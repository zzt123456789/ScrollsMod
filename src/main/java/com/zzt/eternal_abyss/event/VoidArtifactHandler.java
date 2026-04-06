package com.zzt.eternal_abyss.event;

import baubles.api.BaublesApi;
import com.zzt.eternal_abyss.init.ModItems;
import com.zzt.eternal_abyss.util.ImmunityHelper;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.enhancedvisuals.api.event.SplashEvent;
import team.creative.enhancedvisuals.api.event.VisualExplosionEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber
public class VoidArtifactHandler {

    private static final int MAX_INVUL_TIME = 50; // 自定义无敌帧

    private static final Map<UUID, Integer> invulnerableMap = new ConcurrentHashMap<>();
    private static final Map<UUID, Float> prevHealthMap = new ConcurrentHashMap<>();

    private static final String TAG_ULTIMATE_FLIGHT = "UltimateFlight";
    private static final String TAG_GRANTED_FLIGHT = "GrantedByVoidArtifact";

    private static final int TICK_INTERVAL = 40;

//    public static DamageSource[] damageSourcesFromForge(EntityLivingBase source) {
//
//        return new DamageSource[]{
//                // 原版环境伤害
//                DamageSource.ANVIL,
//                DamageSource.CACTUS,
//                DamageSource.causeMobDamage(source),
//                DamageSource.DRAGON_BREATH,
//                DamageSource.DROWN,
//                DamageSource.causeExplosionDamage(source),
//                DamageSource.FALL,
//                DamageSource.FALLING_BLOCK,
//                DamageSource.OUT_OF_WORLD,
//                DamageSource.FLY_INTO_WALL,
//                DamageSource.GENERIC,
//                DamageSource.HOT_FLOOR,
//                DamageSource.IN_FIRE,
//                DamageSource.IN_WALL,
//                DamageSource.MAGIC,
//                DamageSource.LAVA,
//                DamageSource.LIGHTNING_BOLT,
//                DamageSource.causeMobDamage(source),
//                DamageSource.ON_FIRE,
//                DamageSource.STARVE,
//                DamageSource.causeMobDamage(source),
//                DamageSource.CRAMMING,
//                DamageSource.causePlayerDamage((EntityPlayer) source),
//                DamageSource.WITHER
//        };
//    }
//


    @SubscribeEvent
    public void onPlayerTick(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        if (player.world.isRemote) return;

        boolean hasArtifact = ImmunityHelper.hasUltimateItem(player);
        updatePlayerNBT(player, hasArtifact);

        UUID id = player.getUniqueID();
        float prevHealth = prevHealthMap.getOrDefault(id, player.getHealth());

        if (hasArtifact) {
            player.getEntityData().setBoolean(TAG_ULTIMATE_FLIGHT, true);

            if (player.getHealth() < prevHealth) {
                setInvulnerable(player, MAX_INVUL_TIME);
                player.hurtResistantTime = MAX_INVUL_TIME;
            }
            prevHealthMap.put(id, player.getHealth());

            // 保持呼吸、灭火
            player.setAir(300);
            if (player.isBurning()) player.extinguish();

            // 移除蜘蛛网 - 3x3范围
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos webPos = new BlockPos(player.posX + x, player.posY + y, player.posZ + z);
                        Block webBlock = player.world.getBlockState(webPos).getBlock();
                        if (isWebBlock(webBlock)) {
                            player.world.destroyBlock(webPos, false);
                        }
                    }
                }
            }

            // 移除负面药水
            List<Potion> effectsToRemove = new ArrayList<>();
            for (PotionEffect effect : player.getActivePotionEffects()) {
                if (effect.getPotion().isBadEffect()) effectsToRemove.add(effect.getPotion());
            }
            for (Potion potion : effectsToRemove) player.removePotionEffect(potion);



            updateInvulnerabilityTimers(player);

            // 白天/黑夜周期增益
            if (player.world.provider.getDimension() == 0 && player.ticksExisted % 100 == 0) {
                boolean isDay = player.world.isDaytime();
                if (isDay) {
                    addEffect(player, MobEffects.SATURATION, 440, 1);
                    addEffect(player, MobEffects.RESISTANCE, 440, 1);
                    addEffect(player, MobEffects.REGENERATION, 440, 1);
                    addEffect(player, MobEffects.NIGHT_VISION, 440, 1);
                    addEffect(player, MobEffects.LUCK, 440, 1);
                } else {
                    addEffect(player, MobEffects.NIGHT_VISION, 440, 2);
                    addEffect(player, MobEffects.HASTE, 440, 2);
                    addEffect(player, MobEffects.GLOWING, 440, 2);
                    addEffect(player, MobEffects.REGENERATION, 440, 2);
                    addEffect(player, MobEffects.LUCK, 440, 2);
                }
            }

        } else {
            player.getEntityData().removeTag(TAG_ULTIMATE_FLIGHT);
            invulnerableMap.remove(id);
            prevHealthMap.remove(id);
        }

        boolean ultimateFlight = player.getEntityData().getBoolean(TAG_ULTIMATE_FLIGHT);

        // ===== 服务端：只处理 allowFlying（但要同步一次） =====

        if (ultimateFlight) {
            if (!player.capabilities.allowFlying) {
                player.capabilities.allowFlying = true;
                player.sendPlayerAbilities();
                player.getEntityData().setBoolean(TAG_GRANTED_FLIGHT, true);
            }

        } else {
            if (player.getEntityData().getBoolean(TAG_GRANTED_FLIGHT)
                    && !player.isCreative()
                    && !player.isSpectator()) {

                player.capabilities.isFlying = false;
                player.capabilities.allowFlying = false;
                player.sendPlayerAbilities();
                player.getEntityData().removeTag(TAG_GRANTED_FLIGHT);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();


        if (!ImmunityHelper.hasUltimateItem(player)) return;

        UUID id = player.getUniqueID();

        if (invulnerableMap.getOrDefault(id, 0) > 0) {
            event.setCanceled(true);
            return;
        }

        if (isImmuneDamage(event.getSource())) {
            event.setCanceled(true);
            player.hurtResistantTime = MAX_INVUL_TIME;
            return;
        }
        int dim = player.world.provider.getDimension();
        float reduction;
        if (dim == 0) reduction = 0.66f;     // 主世界 66% 免伤
        else if (dim == -1) reduction = 0.33f; // 下界 33%
        else if (dim == 1) reduction = 0.11f;  // 末地 11%
        else reduction = 0.15f;// 其他维度 15%

        if (reduction > 0f) {
            event.setAmount(event.getAmount() * (1.0f - reduction));
        }

        if (!event.isCanceled() && event.getAmount() > 0f) {
            setInvulnerable(player, MAX_INVUL_TIME);
            player.hurtResistantTime = MAX_INVUL_TIME;
        }

    }


    private void updateInvulnerabilityTimers(EntityPlayer player) {
        UUID id = player.getUniqueID();
        if (invulnerableMap.containsKey(id)) {
            int time = invulnerableMap.get(id);
            if (time > 0) invulnerableMap.put(id, time - 1);
            else invulnerableMap.remove(id);
        }
    }

    public static void setInvulnerable(EntityPlayer player, int ticks) {
        UUID id = player.getUniqueID();
        invulnerableMap.put(id, ticks);
    }

    private void updatePlayerNBT(EntityPlayer player, boolean hasArtifact) {
        player.getEntityData().setBoolean("HasUltimateItem", hasArtifact);
    }
    private boolean isWebBlock(Block block) {
        if (block == Blocks.WEB) return true;
        ResourceLocation registryName = block.getRegistryName();
        if (registryName != null) {
            if ("srparasites".equals(registryName.getNamespace()) &&
                    "srpweb".equals(registryName.getPath())) return true;
        }
        return false;
    }

    private void addEffect(EntityPlayer player, Potion potion, int duration, int amplifier) {
        PotionEffect effect = new PotionEffect(potion, duration, amplifier, true, false);
        player.addPotionEffect(effect);
    }

    private boolean isImmuneDamage(DamageSource src) {
        if (src == null) return false;

        // 通用判断
        if (src.isFireDamage()) return true;
        if (src.isExplosion()) return true;

        // 明确列出的常量
        if (src == DamageSource.FALL ||// 掉落
                src == DamageSource.CACTUS ||// 仙人掌
                src == DamageSource.DROWN ||// 溺水
                src == DamageSource.LAVA ||// 熔岩
                src == DamageSource.ANVIL ||// 铁砧
                src == DamageSource.FALLING_BLOCK ||// 摔落
                src == DamageSource.IN_WALL ||//窒息
                src == DamageSource.OUT_OF_WORLD ||
                src == DamageSource.FLY_INTO_WALL) {// 掉出世界
            return true;
        }

        // 闪电 / 龙息 等，部分版本下用字符串判断更稳妥
        String type = src.getDamageType();
        if ("lightningBolt".equals(type) || "dragonBreath".equals(type) || "wither".equals(type)) {
            return true;
        }

        return false;
    }


    @SubscribeEvent
    public void onPotionApplicable(PotionEvent.PotionApplicableEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        if (ImmunityHelper.hasUltimateItem(player) && event.getPotionEffect().getPotion().isBadEffect()) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public void onSetAttackTarget(LivingSetAttackTargetEvent event) {
        if (!(event.getEntityLiving() instanceof EntityMob)) return;
        if (!(event.getTarget() instanceof EntityPlayer)) return;

        EntityMob mob = (EntityMob) event.getEntityLiving();
        EntityPlayer target = (EntityPlayer) event.getTarget();

        if (target.getEntityData().getBoolean("HasUltimateItem")) {
            for (EntityPlayer player : mob.world.playerEntities) {
                if (!player.getEntityData().getBoolean("HasUltimateItem")) {
                    mob.setAttackTarget(player);
                    return;
                }
            }
            mob.setAttackTarget(null);
        }
    }

    @SubscribeEvent
    public static void onBreakSpeed(net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed event) {
        EntityPlayer player = event.getEntityPlayer();
        if (!ImmunityHelper.hasUltimateItem(player)) return;

        float speed = event.getNewSpeed();
        boolean inWater = player.isInWater() && !player.isPotionActive(MobEffects.WATER_BREATHING);
        boolean airborne = !player.onGround;

        if (inWater)  speed /= 0.2F;
        if (airborne) speed /= 0.2F;

        event.setNewSpeed(speed);
    }



    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.player.getUniqueID();
        invulnerableMap.remove(id);
        prevHealthMap.remove(id);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onVisualExplosion(VisualExplosionEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player != null && ImmunityHelper.hasUltimateItem(player)) {
            // 只禁用当前客户端玩家的模糊/震动
            event.setDisableBlur(true);
        }
    }
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onVisualSplash(SplashEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player != null && ImmunityHelper.hasUltimateItem(player)) {
            // 阻止水花模糊效果
            event.setCanceled(true);
        }
    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();

        if (!ImmunityHelper.hasUltimateItem(player)) return;

        DamageSource src = event.getSource();
        if (isImmuneDamage(src)) {
            event.setCanceled(true); // 彻底免疫，不会再触发 LivingHurt
        }
    }
}
