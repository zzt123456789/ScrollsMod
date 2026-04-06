package com.zzt.eternal_abyss.event;

import baubles.api.BaublesApi;
import com.zzt.eternal_abyss.config.ModConfig;
import com.zzt.eternal_abyss.init.ModItems;
import com.zzt.eternal_abyss.recordbag.RecordBagEffectRegistry;
import com.zzt.eternal_abyss.recordbag.RecordBagRuntimeUtil;
import com.zzt.eternal_abyss.util.compat.ModCompat;
import com.zzt.eternal_abyss.util.compat.ScalingHealthCompat;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.zzt.eternal_abyss.recordbag.RecordBagRuntimeUtil.findRecordBag;

@Mod.EventBusSubscriber
public class RecordBagSpecialEffectHandler {

    private static final int TICK_INTERVAL = 10;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntityLiving().world.isRemote) return;

        if (!(event.getSource().getTrueSource() instanceof EntityPlayer)) return;

        EntityPlayer attacker = (EntityPlayer) event.getSource().getTrueSource();
        if (!hasDepthRing(attacker)) return;

        Set<String> effects = RecordBagRuntimeUtil.getActiveEffects(attacker);

        float amount = event.getAmount();
        amount = applyDoubleDamageChance(amount, attacker, effects);

        event.setAmount(amount);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntityLiving().world.isRemote) return;

        float amount = event.getAmount();

        // =========================
        // 1. 攻击者：固定增伤 + on_hit_potion
        // =========================
        if (event.getSource().getTrueSource() instanceof EntityPlayer) {
            EntityPlayer attacker = (EntityPlayer) event.getSource().getTrueSource();

            if (!attacker.world.isRemote && hasDepthRing(attacker)) {
                EntityLivingBase target = event.getEntityLiving();

                if (target != null && target != attacker) {
                    Set<String> effects = RecordBagRuntimeUtil.getActiveEffects(attacker);

                    // 固定增伤
                    amount = applyDamageBonus(amount, effects);

                    // 攻击附带药水
                    for (String effect : effects) {
                        if (RecordBagEffectRegistry.isSpecialType(effect, "on_hit_potion")) {
                            applyOnHitPotion(target, attacker, effect);
                        }
                    }
                }
            }
        }

        // =========================
        // 2. 受击者：固定减伤
        // =========================
        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer victim = (EntityPlayer) event.getEntityLiving();

            if (!victim.world.isRemote && hasDepthRing(victim)) {
                Set<String> effects = RecordBagRuntimeUtil.getActiveEffects(victim);
                amount = applyDamageReduction(amount, effects);
            }
        }

        event.setAmount(amount);
    }

    @SubscribeEvent
    public static void onLivingExperienceDrop(LivingExperienceDropEvent event) {

        EntityPlayer player = event.getAttackingPlayer();
        if (player == null) return;
        if (player.world.isRemote) return;
        if (!hasDepthRing(player)) return;

        EntityLivingBase target = event.getEntityLiving();
        if (target == null) return;

        if (ModCompat.isScalingHealthLoaded()
                && ScalingHealthCompat.isEntityBlight(target)) {
            return;
        }

        Set<String> effects = RecordBagRuntimeUtil.getActiveEffects(player);

        int xp = event.getDroppedExperience();
        xp = applyXpBonus(xp, effects);

        event.setDroppedExperience(Math.max(0, xp));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        EntityPlayer player = event.player;
        if (player.world.isRemote) return;
        if ((player.ticksExisted % TICK_INTERVAL) != 0) return;

        // 必须佩戴深渊之戒，否则唱片包效果无效
        if (!hasDepthRing(player)) {
            cleanupRecordBagAttributes(player, Collections.emptySet());
            return;
        }

        Set<String> effects = RecordBagRuntimeUtil.getActiveEffects(player);
        Set<UUID> shouldKeep = new HashSet<>();

        for (String effect : effects) {
            if (RecordBagEffectRegistry.isPotionEffect(effect)) {
                applyPotionEffect(player, effect);
            } else if (RecordBagEffectRegistry.isAttributeEffect(effect)) {
                applyAttributeEffect(player, effect, shouldKeep);
            } else if (RecordBagEffectRegistry.isSpecialType(effect, "nearby_debuff")) {
                applyNearbyDebuff(player, effect);
            }
        }

        // ===== 隐藏彩蛋：唱片包收集达到上限时，全属性 +25% =====
        if (hasFullRecordBagEasterEgg(player)) {
            applyFullBagAllAttributeBonus(player, shouldKeep);
        }

        cleanupRecordBagAttributes(player, shouldKeep);
    }

    // =========================================================
    // special
    // =========================================================

    private static float applyDamageBonus(float baseDamage, Set<String> effects) {
        float result = baseDamage;

        for (String effect : effects) {
            if (RecordBagEffectRegistry.isSpecialType(effect, "damage_bonus")) {
                double bonus = RecordBagEffectRegistry.getSpecialArgDouble(effect, 0, 0.0D);
                if (bonus > 0.0D) {
                    result *= (float) (1.0D + bonus);
                }
            }
        }

        return result;
    }

    private static float applyDamageReduction(float baseDamage, Set<String> effects) {
        float result = baseDamage;

        for (String effect : effects) {
            if (RecordBagEffectRegistry.isSpecialType(effect, "damage_reduction")) {
                double reduction = RecordBagEffectRegistry.getSpecialArgDouble(effect, 0, 0.0D);
                if (reduction > 0.0D) {
                    result *= (float) Math.max(0.0D, 1.0D - reduction);
                }
            }
        }

        return result;
    }

    private static float applyDoubleDamageChance(float baseDamage, EntityPlayer attacker, Set<String> effects) {
        float result = baseDamage;

        for (String effect : effects) {
            if (RecordBagEffectRegistry.isSpecialType(effect, "double_damage_chance")) {
                double chance = RecordBagEffectRegistry.getSpecialArgDouble(effect, 0, 0.0D);
                if (chance > 0.0D && attacker.world.rand.nextDouble() < chance) {
                    result *= 2.0F;
                }
            }
        }

        return result;
    }

    private static int applyXpBonus(int baseXp, Set<String> effects) {
        int result = baseXp;

        for (String effect : effects) {
            if (RecordBagEffectRegistry.isSpecialType(effect, "xp_bonus")) {
                double bonus = RecordBagEffectRegistry.getSpecialArgDouble(effect, 0, 0.0D);
                if (bonus > 0.0D) {
                    result = (int) Math.round(result * (1.0D + bonus));
                }
            }
        }

        return result;
    }

    private static void applyOnHitPotion(EntityLivingBase target, EntityPlayer attacker, String effect) {
        try {
            String potionId = RecordBagEffectRegistry.getSpecialArg(effect, 0);
            int duration = RecordBagEffectRegistry.getSpecialArgInt(effect, 1, 0);
            int amplifier = RecordBagEffectRegistry.getSpecialArgInt(effect, 2, 0);
            double chance = RecordBagEffectRegistry.getSpecialArgDouble(effect, 3, 0.0D);

            if (potionId.isEmpty() || duration <= 0 || chance <= 0.0D) return;
            if (attacker.world.rand.nextDouble() >= chance) return;

            Potion potion = ForgeRegistries.POTIONS.getValue(new ResourceLocation(potionId));
            if (potion == null) return;

            target.addPotionEffect(new PotionEffect(potion, duration, amplifier));
        } catch (Exception ignored) {
        }
    }

    private static void applyNearbyDebuff(EntityPlayer player, String effect) {
        try {
            String potionId = RecordBagEffectRegistry.getSpecialArg(effect, 0);
            int duration = RecordBagEffectRegistry.getSpecialArgInt(effect, 1, 0);
            int amplifier = RecordBagEffectRegistry.getSpecialArgInt(effect, 2, 0);
            double range = RecordBagEffectRegistry.getSpecialArgDouble(effect, 3, 0.0D);

            if (potionId.isEmpty() || duration <= 0 || range <= 0.0D) return;

            Potion potion = ForgeRegistries.POTIONS.getValue(new ResourceLocation(potionId));
            if (potion == null) return;

            AxisAlignedBB box = player.getEntityBoundingBox().grow(range);
            List<EntityLivingBase> list = player.world.getEntitiesWithinAABB(EntityLivingBase.class, box);

            for (EntityLivingBase target : list) {
                if (target == null || target == player || !target.isEntityAlive()) continue;
                if (!canApplyNearbyDebuff(player, target)) continue;

                target.addPotionEffect(new PotionEffect(potion, duration, amplifier));
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 这里先留一个默认过滤逻辑。
     * 你后面如果已经有 config 黑名单，可以在这里接进去。
     */
    private static boolean canApplyNearbyDebuff(EntityPlayer player, EntityLivingBase target) {
        if (target == player) return false;

        ResourceLocation key = EntityList.getKey(target);
        if (key != null) {
            String id = key.toString();

            // 这里以后可接 ModConfig 里的黑名单
            // 例如：
             if (ModConfig.swordQiBlacklist.contains(id)) return false;
        }

        return true;
    }

    // =========================================================
    // potion
    // 格式：potion:minecraft:speed@40@1@true@false
    // =========================================================

    private static void applyPotionEffect(EntityPlayer player, String effect) {
        try {
            String body = RecordBagEffectRegistry.getPotionBody(effect);
            String[] parts = body.split("@");

            if (parts.length < 3) return;

            String potionId = parts[0].trim();
            int duration = Integer.parseInt(parts[1].trim());
            int amplifier = Integer.parseInt(parts[2].trim());
            boolean ambient = parts.length >= 4 && Boolean.parseBoolean(parts[3].trim());
            boolean particles = parts.length >= 5 && Boolean.parseBoolean(parts[4].trim());

            Potion potion = ForgeRegistries.POTIONS.getValue(new ResourceLocation(potionId));
            if (potion == null) return;

            PotionEffect current = player.getActivePotionEffect(potion);

            if (current == null
                    || current.getAmplifier() < amplifier
                    || current.getDuration() <= 200) {

                player.addPotionEffect(new PotionEffect(potion, duration, amplifier, ambient, particles));
            }
        } catch (Exception ignored) {
        }
    }

    // =========================================================
    // attribute
    // 格式：attribute:generic.attackDamage@0@4.0
    // 格式：attribute:ca.critChance@0@0.25
    // =========================================================

    private static void applyAttributeEffect(EntityPlayer player, String effect, Set<UUID> shouldKeep) {
        try {
            String body = RecordBagEffectRegistry.getAttributeBody(effect);
            String[] parts = body.split("@");

            if (parts.length < 3) return;

            String attrName = parts[0].trim();
            int operation = Integer.parseInt(parts[1].trim());
            double amount = Double.parseDouble(parts[2].trim());

            IAttributeInstance inst = player.getAttributeMap().getAttributeInstanceByName(attrName);
            if (inst == null) return;

            UUID uuid = makeAttributeUUID(effect);
            shouldKeep.add(uuid);

            AttributeModifier existing = inst.getModifier(uuid);
            if (existing == null) {
                AttributeModifier mod = new AttributeModifier(uuid, "RecordBag:" + attrName, amount, operation);
                inst.applyModifier(mod);
            }
        } catch (Exception ignored) {
        }
    }

    private static void applyFullBagAllAttributeBonus(EntityPlayer player, Set<UUID> shouldKeep) {
        Collection<IAttributeInstance> allAttrs = player.getAttributeMap().getAllAttributes();

        for (IAttributeInstance inst : allAttrs) {
            if (inst == null || inst.getAttribute() == null) continue;

            String attrName = inst.getAttribute().getName();
            if (attrName == null || attrName.isEmpty()) continue;

            UUID uuid = makeFullBagAttributeUUID(attrName);
            shouldKeep.add(uuid);

            AttributeModifier existing = inst.getModifier(uuid);
            if (existing == null) {
                AttributeModifier mod = new AttributeModifier(
                        uuid,
                        "RecordBag:EasterEgg:" + attrName,
                        0.25D,
                        2
                );
                inst.applyModifier(mod);
            }
        }
    }

    private static UUID makeFullBagAttributeUUID(String attrName) {
        return UUID.nameUUIDFromBytes(("RecordBagFullBonus|" + attrName).getBytes(StandardCharsets.UTF_8));
    }

    private static UUID makeAttributeUUID(String effect) {
        return UUID.nameUUIDFromBytes(("RecordBagAttr|" + effect).getBytes(StandardCharsets.UTF_8));
    }

    private static void cleanupRecordBagAttributes(EntityPlayer player, Set<UUID> shouldKeep) {
        Collection<IAttributeInstance> allAttrs = player.getAttributeMap().getAllAttributes();

        for (IAttributeInstance inst : allAttrs) {
            List<AttributeModifier> toRemove = new ArrayList<>();

            for (AttributeModifier mod : inst.getModifiers()) {
                if (mod.getName() != null && mod.getName().startsWith("RecordBag:")) {
                    if (!shouldKeep.contains(mod.getID())) {
                        toRemove.add(mod);
                    }
                }
            }

            for (AttributeModifier mod : toRemove) {
                inst.removeModifier(mod);
            }
        }
    }

    public static boolean hasDepthRing(EntityPlayer player) {
        return BaublesApi.isBaubleEquipped(player, ModItems.DEPTH_RING) != -1;
    }

    private static boolean hasFullRecordBagEasterEgg(EntityPlayer player) {
        ItemStack bag = findRecordBag(player);
        if (bag.isEmpty()) return false;

        com.zzt.eternal_abyss.inventory.RecordBagHandler handler =
                com.zzt.eternal_abyss.util.RecordBagUtil.getHandler(bag);

        Set<String> collected = new HashSet<>();

        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemRecord) {
                if (stack.getItem().getRegistryName() != null) {
                    collected.add(stack.getItem().getRegistryName().toString());
                }
            }
        }

        return collected.size() >= getRequiredRecordCountForEasterEgg();
    }

    private static int getRequiredRecordCountForEasterEgg() {
        return Math.min(getTotalRegisteredRecordCount(), 54);
    }

    private static int getTotalRegisteredRecordCount() {
        Set<String> ids = new HashSet<>();

        for (Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
            if (item instanceof ItemRecord && item.getRegistryName() != null) {
                ids.add(item.getRegistryName().toString());
            }
        }

        return ids.size();
    }
}