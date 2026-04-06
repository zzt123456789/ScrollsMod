package com.zzt.eternal_abyss.items;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;
import baubles.api.cap.IBaublesItemHandler;
import com.zzt.eternal_abyss.config.ModConfig;
import com.zzt.eternal_abyss.event.DepthRingHandler;
import com.zzt.eternal_abyss.font.ModFontRenderers;
import com.zzt.eternal_abyss.init.ModItems;
import com.zzt.eternal_abyss.util.interfaces.IKeptBauble;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class DepthRing extends ItemBase implements IBauble, IKeptBauble {

    public DepthRing() {
        super("depth_ring");
        this.setMaxStackSize(1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public FontRenderer getFontRenderer(ItemStack stack) {
        return ModFontRenderers.DEPTH_RING_RENDERER;
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.EPIC;
    }

    @Override
    public BaubleType getBaubleType(ItemStack itemStack) {
        return BaubleType.RING;
    }

    @Override
    public void onEquipped(ItemStack stack, EntityLivingBase player) {
        // player.playSound(SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, .75F, 1.9f);
        
        // 标记玩家已佩戴过深渊之戒（用于超时机制）
        if (player instanceof EntityPlayer && !player.world.isRemote) {
            markAsEquipped((EntityPlayer) player);
        }
    }

    @Override
    public void onUnequipped(ItemStack stack, EntityLivingBase player) {
        // player.playSound(SoundEvents.BLOCK_END_PORTAL_SPAWN, .75F, 2f);
    }

    @Override
    public boolean canEquip(ItemStack stack, EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer)) return false;
        EntityPlayer player = (EntityPlayer) entity;

        // 客户端：用 stack NBT 快速判断（避免先放进去再被服务端弹回）
        if (player.world.isRemote) {
            NBTTagCompound c = stack.getSubCompound(TAG_CLIENT);
            if (c != null && c.getBoolean(KEY_SEALED)) return false;
            // 如果还没同步到，就允许（极短窗口可能仍会回弹，但基本只发生在刚进世界那一下）
            return true;
        }

        // 服务端：最终判定
        int equippedIndex = BaublesApi.isBaubleEquipped(player, this);
        if (equippedIndex != -1) return false;

        if (isEquipForbidden(player)) {
            player.sendStatusMessage(
                    new TextComponentString(TextFormatting.RED + "深渊已经封印，你无法再次佩戴此戒指。"),
                    true
            );
            return false;
        }
        return true;
    }


    /**
     * 检查玩家是否被禁止佩戴深渊之戒
     * 逻辑：玩家加入世界后，如果在配置的时间内没有佩戴过戒指，则永久失去佩戴资格
     */
    public static boolean isEquipForbidden(EntityPlayer player) {
        int timeoutMinutes = ModConfig.depthRingUnequipTimeoutMinutes;
        if (timeoutMinutes <= 0) return false;

        NBTTagCompound data = player.getEntityData()
                .getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);

        // 已佩戴过 → 永远允许
        if (data.getBoolean("DepthRingHasEquipped")) return false;

        // ★ 已封印 → 永远禁止
        if (data.getBoolean("DepthRingSealed")) return true;

        long playedMillis = data.getLong("DepthRingPlayedMillis");
        long lastJoin = data.getLong("DepthRingLastJoinTime");
        if (lastJoin > 0) {
            playedMillis += Math.max(0, System.currentTimeMillis() - lastJoin);
        }

        long limitMillis = timeoutMinutes * 60_000L;
        if (playedMillis >= limitMillis) {
            // ★ 一次性封印
            data.setBoolean("DepthRingSealed", true);
            data.removeTag("DepthRingLastJoinTime"); // 彻底清掉
            player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, data);
            return true;
        }

        return false;
    }


    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.player instanceof EntityPlayerMP))
            return;

        EntityPlayerMP player = (EntityPlayerMP) event.player;

        NBTTagCompound persistent = player.getEntityData();
        if (!persistent.hasKey(EntityPlayer.PERSISTED_NBT_TAG))
            return;

        NBTTagCompound data = persistent.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);

        long lastJoin = data.getLong("DepthRingLastJoinTime");
        if (lastJoin <= 0)
            return;

        long now = System.currentTimeMillis();
        long sessionMillis = Math.max(0, now - lastJoin);

        long total = data.getLong("DepthRingPlayedMillis");
        data.setLong("DepthRingPlayedMillis", total + sessionMillis);

        // 清理本次登录时间（可选，但推荐）
        data.removeTag("DepthRingLastJoinTime");

        persistent.setTag(EntityPlayer.PERSISTED_NBT_TAG, data);
    }


    /**
     * 标记玩家已经佩戴过深渊之戒
     */
    public static void markAsEquipped(EntityPlayer player) {
        NBTTagCompound persistent = player.getEntityData();
        NBTTagCompound data;
        if (!persistent.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
            data = new NBTTagCompound();
            persistent.setTag(EntityPlayer.PERSISTED_NBT_TAG, data);
        } else {
            data = persistent.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        }
        data.setBoolean("DepthRingHasEquipped", true);
        persistent.setTag(EntityPlayer.PERSISTED_NBT_TAG, data);
    }

    @Override
    public boolean canUnequip(ItemStack stack, EntityLivingBase entity) {
        // 仅允许创造模式的玩家卸下
        if (entity instanceof EntityPlayer) {
            return ((EntityPlayer) entity).isCreative();
        }
        return false;
    }

    public static int getKillCount(ItemStack stack, String curseKey) {
        if (!stack.hasTagCompound())
            return 0;
        return stack.getTagCompound()
                .getCompoundTag("PurifyData")
                .getInteger("Kills_" + curseKey);
    }

    public static void addKill(ItemStack stack, String curseKey) {
        int current = getKillCount(stack, curseKey);
        stack.getOrCreateSubCompound("PurifyData")
                .setInteger("Kills_" + curseKey, current + 1);
    }

    public static boolean isDamageReversed(ItemStack stack) {
        return stack.getOrCreateSubCompound("PurifyData").getBoolean("DamageReversed");
    }

    public static void setDamageReversed(ItemStack stack) {
        stack.getOrCreateSubCompound("PurifyData").setBoolean("DamageReversed", true);
    }
    //

    public static boolean isExpBoostReversed(ItemStack stack) {
        return stack.getOrCreateSubCompound("PurifyData").getBoolean("ExpBoostReversed");
    }

    public static void setExpBoostReversed(ItemStack stack) {
        stack.getOrCreateSubCompound("PurifyData").setBoolean("ExpBoostReversed", true);
    }

    public static boolean isExpBoostReversedFor(EntityPlayer player) {
        int slot = BaublesApi.isBaubleEquipped(player, com.zzt.eternal_abyss.init.ModItems.DEPTH_RING);
        if (slot == -1)
            return false;

        ItemStack ring = BaublesApi.getBaublesHandler(player).getStackInSlot(slot);
        return !ring.isEmpty() && isExpBoostReversed(ring);
    }

    public static boolean isDamageReversedFor(EntityPlayer player) {
        int slot = BaublesApi.isBaubleEquipped(player, com.zzt.eternal_abyss.init.ModItems.DEPTH_RING);
        if (slot == -1)
            return false;

        ItemStack ring = BaublesApi.getBaublesHandler(player).getStackInSlot(slot);
        return !ring.isEmpty() && isDamageReversed(ring); // 已反转
    }

    public static boolean isHauntedReversed(ItemStack stack) {
        return stack.getOrCreateSubCompound("PurifyData").getBoolean("HauntedReversed");
    }

    public static void setHauntedReversed(ItemStack stack) {
        stack.getOrCreateSubCompound("PurifyData").setBoolean("HauntedReversed", true);
    }

    public static boolean isVoidResistReversed(ItemStack stack) {
        return stack.getOrCreateSubCompound("PurifyData").getBoolean("VoidResistReversed");
    }

    public static void setVoidResistReversed(ItemStack stack) {
        stack.getOrCreateSubCompound("PurifyData").setBoolean("VoidResistReversed", true);
    }

    public static boolean isVoidResistReversedFor(EntityPlayer player) {
        int slot = BaublesApi.isBaubleEquipped(player, com.zzt.eternal_abyss.init.ModItems.DEPTH_RING);
        if (slot == -1)
            return false;

        ItemStack ring = BaublesApi.getBaublesHandler(player).getStackInSlot(slot);
        return !ring.isEmpty() && isVoidResistReversed(ring); // 检查幸运诅咒反转状态
    }

    public static void setLucky(ItemStack stack) {
        stack.getOrCreateSubCompound("PurifyData").setBoolean("LuckyReversed", true);
    }

    public static boolean isLuckyReversed(ItemStack stack) {
        return stack.getOrCreateSubCompound("PurifyData").getBoolean("LuckyReversed");
    }

    public static boolean isLuckyReversedFor(EntityPlayer player) {
        int slot = BaublesApi.isBaubleEquipped(player, com.zzt.eternal_abyss.init.ModItems.DEPTH_RING);
        if (slot == -1)
            return false;

        ItemStack ring = BaublesApi.getBaublesHandler(player).getStackInSlot(slot);
        return !ring.isEmpty() && isLuckyReversed(ring); // 检查幸运诅咒反转状态
    }

    public static boolean isFinalReversed(ItemStack stack) {
        return stack.getOrCreateSubCompound("PurifyData").getBoolean("FinalReversed");
    }

    public static void setFinalReversed(ItemStack stack) {
        stack.getOrCreateSubCompound("PurifyData").setBoolean("FinalReversed", true);
    }

    public static boolean hasCurse(ItemStack stack, String curseKey) {
        return getKillCount(stack, curseKey) > 0 || isCurseReversed(stack, curseKey);
    }

    // 判断任意诅咒是否已反转（扩展性）
    public static boolean isCurseReversed(ItemStack stack, String curseKey) {
        switch (curseKey) {
            case "DAMAGE_SPEED":
                return isDamageReversed(stack);
            case "EXPERIENCE_DROP":
                return isExpBoostReversed(stack);
            case "HAUNTED_SHADOWS":
                return isHauntedReversed(stack);
            case "VOID_RESISTANCE":
                return isVoidResistReversed(stack);
            case "LUCKY":
                return isLuckyReversed(stack);
            case "FINAL":
                return isFinalReversed(stack);
            default:
                return false;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {

        tooltip.add(I18n.format("tooltip.depth_ring.flavor"));
        tooltip.add(I18n.format("tooltip.depth_ring.desc1"));
        tooltip.add("");

        /* ================== Shift：显示诅咒详情 ================== */
        if (GuiScreen.isShiftKeyDown()) {

            /* ================== 苦痛压制（攻击 / 攻速） ================== */
            {
                String key = "DAMAGE_SPEED";
                ModConfig.CurseConfig cfg = ModConfig.getCurseConfig(key);
                int kills = getKillCount(stack, key);
                int req = cfg != null ? cfg.requiredKills : 100;
                String target = getEntityDisplayName(cfg);

                int dmgDown = (int) Math.round((1.0 - ModConfig.damageCurseMultiplier) * 100);
                int atkDown = (int) Math.round(Math.abs(ModConfig.attackSpeedCurse) * 100);
                int dmgUp = (int) Math.round((ModConfig.damageReversedMultiplier - 1.0) * 100);
                int atkUp = (int) Math.round(ModConfig.attackSpeedReversed * 100);

                if (!isDamageReversed(stack)) {
                    tooltip.add(I18n.format("tooltip.depth_ring.damage.title_unreversed"));
                    tooltip.add(I18n.format("tooltip.depth_ring.damage.negative1", dmgDown));
                    tooltip.add(I18n.format("tooltip.depth_ring.damage.negative2", atkDown));
                    tooltip.add(I18n.format(
                            "tooltip.depth_ring.damage.progress_unreversed",
                            target, kills, req));
                } else {
                    tooltip.add(I18n.format("tooltip.depth_ring.damage.title_reversed"));
                    tooltip.add(I18n.format("tooltip.depth_ring.damage.positive1", dmgUp));
                    tooltip.add(I18n.format("tooltip.depth_ring.damage.positive2", atkUp));
                    tooltip.add(I18n.format(
                            "tooltip.depth_ring.damage.progress_reversed",
                            target, kills));
                }
                tooltip.add("");
            }

            /* ================== 知识侵蚀（经验） ================== */
            {
                String key = "EXPERIENCE_DROP";
                ModConfig.CurseConfig cfg = ModConfig.getCurseConfig(key);
                int kills = getKillCount(stack, key);
                int req = cfg != null ? cfg.requiredKills : 50;
                String target = getEntityDisplayName(cfg);

                int expDown = (int) Math.round((1.0 - ModConfig.expCurseMultiplier) * 100);
                int chanceX2 = (int) Math.round(ModConfig.expReversedChanceX2 * 100);
                int chanceX4 = (int) Math.round(ModConfig.expReversedChanceX4 * 100);

                if (!isExpBoostReversed(stack)) {
                    tooltip.add(I18n.format("tooltip.depth_ring.exp.title_unreversed"));
                    tooltip.add(I18n.format("tooltip.depth_ring.exp.negative1", expDown));
                    tooltip.add(I18n.format(
                            "tooltip.depth_ring.exp.progress_unreversed",
                            target, kills, req));
                } else {
                    tooltip.add(I18n.format("tooltip.depth_ring.exp.title_reversed"));
                    tooltip.add(I18n.format(
                            "tooltip.depth_ring.exp.positive1",
                            chanceX2, chanceX4));
                    tooltip.add(I18n.format(
                            "tooltip.depth_ring.exp.progress_reversed",
                            target, kills));
                }
                tooltip.add("");
            }

            /* ================== 怨影缠身 ================== */
            {
                String key = "HAUNTED_SHADOWS";
                ModConfig.CurseConfig cfg = ModConfig.getCurseConfig(key);
                int kills = getKillCount(stack, key);
                int req = cfg != null ? cfg.requiredKills : 30;
                String target = getEntityDisplayName(cfg);

                int moveDown = (int) Math.round(Math.abs(ModConfig.hauntedMoveSpeedCurse) * 100);
                int moveUp = (int) Math.round(ModConfig.hauntedMoveSpeedReversed * 100);

                if (!isHauntedReversed(stack)) {
                    tooltip.add(I18n.format("tooltip.depth_ring.haunted.title_unreversed"));
                    tooltip.add(I18n.format("tooltip.depth_ring.haunted.negative1", moveDown));
                    tooltip.add(I18n.format(
                            "tooltip.depth_ring.haunted.negative2",
                            (int) ModConfig.hauntedAggroRangeBonus));
                    tooltip.add(I18n.format(
                            "tooltip.depth_ring.haunted.progress_unreversed",
                            target, kills, req));
                } else {
                    tooltip.add(I18n.format("tooltip.depth_ring.haunted.title_reversed"));
                    tooltip.add(I18n.format("tooltip.depth_ring.haunted.positive1", moveUp));
                    tooltip.add(I18n.format("tooltip.depth_ring.haunted.positive2"));
                    tooltip.add(I18n.format(
                            "tooltip.depth_ring.haunted.progress_reversed",
                            target, kills));
                }
                tooltip.add("");
            }

            /* ================== 虚空腐蚀 ================== */
            {
                String key = "VOID_RESISTANCE";
                ModConfig.CurseConfig cfg = ModConfig.getCurseConfig(key);
                int kills = getKillCount(stack, key);
                int req = cfg != null ? cfg.requiredKills : 20;
                String target = getEntityDisplayName(cfg);

                EntityPlayer player = Minecraft.getMinecraft().player;
                float maxHP = player != null ? player.getMaxHealth() : 0F;
                boolean hasShade = player != null &&
                        BaublesApi.isBaubleEquipped(player, ModItems.THE_DIMINISHED_SHADE) != -1;

                int minPercent = (int) Math.round(ModConfig.voidBaseHealthPercent * 100);
                if (hasShade) {
                    minPercent = Math.max(10,
                            minPercent - Math.min((int) (maxHP / 5F), minPercent - 10));
                }

                int reversedCount = getReversedCurseCount(stack);
                int curReduce = (int) Math.round(
                        (ModConfig.voidReversedBaseReduction
                                + ModConfig.voidReversedReductionPerCurse * reversedCount) * 100);
                int curDodge = (int) Math.round(
                        (0.04 + ModConfig.voidReversedDodgePerCurse * reversedCount) * 100);

                if (!isVoidResistReversed(stack)) {
                    tooltip.add(I18n.format("tooltip.depth_ring.haunted.title_unreversed_elemtnt"));
                    tooltip.add(I18n.format(
                            "tooltip.depth_ring.haunted.negative1_elemtnt",
                            minPercent));
                    tooltip.add(I18n.format(
                            "tooltip.depth_ring.haunted.progress_unreversed_elemtnt",
                            target, kills, req));
                } else {
                    tooltip.add(I18n.format("tooltip.depth_ring.haunted.title_reversed_elemtnt"));
                    tooltip.add(I18n.format(
                            "tooltip.depth_ring.haunted.positive1_elemtnt",
                            (int) (ModConfig.voidReversedBaseReduction * 100),
                            4,
                            (int) (ModConfig.voidReversedReductionPerCurse * 100),
                            (int) (ModConfig.voidReversedDodgePerCurse * 100)));
                    tooltip.add(I18n.format(
                            "tooltip.depth_ring.haunted.positive1_elemtnt2",
                            curReduce, curDodge));
                    tooltip.add(I18n.format(
                            "tooltip.depth_ring.haunted.progress_reversed_elemtnt",
                            target, kills));
                }
                tooltip.add("");
            }

            /* ================== 厄运不断（幸运） ================== */
            {
                String key = "LUCKY";
                ModConfig.CurseConfig cfg = ModConfig.getCurseConfig(key);
                int count = getKillCount(stack, key);
                int req = cfg != null ? cfg.requiredKills : 100;
                String target = I18n.format("item.clover.name");

                if (!isLuckyReversed(stack)) {
                    tooltip.add(I18n.format("tooltip.depth_ring.haunted.title_unreversed_luck"));
                    tooltip.add(I18n.format("tooltip.depth_ring.haunted.negative1_luck"));
                    tooltip.add(I18n.format(
                            "tooltip.depth_ring.haunted.progress_unreversed_luck",
                            target, count, req));
                } else {
                    tooltip.add(I18n.format("tooltip.depth_ring.haunted.title_reversed_luck"));
                    tooltip.add(I18n.format(
                            "tooltip.depth_ring.haunted.positive1_luck",
                            5, 33));
                    tooltip.add(I18n.format(
                            "tooltip.depth_ring.haunted.progress_reversed_luck",
                            target, count));
                }
            }

        } else {
            tooltip.add(I18n.format("tooltip.depth_ring.hold_shift"));
        }

        // ================== 底部：深渊封印倒计时 ==================
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null) {
            return;
        }

        NBTTagCompound c = stack.getSubCompound(TAG_CLIENT);
        if (c != null&& DepthRingHandler.getEquippedDepthRing(player).isEmpty()) {
            boolean sealed = c.getBoolean(KEY_SEALED);
            long remainSec = c.getLong(KEY_REMAIN_SEC);

            if (sealed || remainSec <= 0) {
                tooltip.add("");
                tooltip.add(TextFormatting.DARK_RED + "深渊已经封印");
            } else {
                long minutes = remainSec / 60;
                long seconds = remainSec % 60;
                tooltip.add("");
                tooltip.add(TextFormatting.GRAY + "深渊将在 "
                        + TextFormatting.RED + minutes + " 分 " + seconds + " 秒"
                        + TextFormatting.GRAY + " 后彻底封印");
            }
        }

        /* ================== Final（Alt） ================== */
        {
            String key = "FINAL";
            ModConfig.CurseConfig cfg = ModConfig.getCurseConfig(key);
            String target = getEntityDisplayName(cfg);

            if (allBaseCursesReversed(stack)) {
                if (GuiScreen.isAltKeyDown()) {
                    tooltip.add("");
                    if (!isFinalReversed(stack)) {
                        tooltip.add(I18n.format("tooltip.depth_ring.final.title_unreversed_ultimate"));
                        tooltip.add(I18n.format("tooltip.depth_ring.final.kill1", target));
                    } else {
                        tooltip.add(I18n.format("tooltip.depth_ring.final.title_reversed_ultimate"));
                        tooltip.add(I18n.format("tooltip.depth_ring.final.kill2", target));
                    }
                } else {
                    tooltip.add("");
                    tooltip.add(I18n.format("tooltip.eternal_abyss.holdAlt"));
                }
            } else {
                tooltip.add("");
                tooltip.add(I18n.format("tooltip.depth_ring.footer"));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static String getEntityDisplayName(ModConfig.CurseConfig cfg) {
        if (cfg == null || cfg.targets == null || cfg.targets.isEmpty()) {
            return I18n.format("tooltip.eternal_abyss.unknown_target");
        }

        String targetId = cfg.targets.get(0);

        ResourceLocation rl;
        try {
            rl = new ResourceLocation(targetId);
        } catch (Exception e) {
            return targetId;
        }

        // 从 Forge 注册表拿实体
        net.minecraftforge.fml.common.registry.EntityEntry entry = net.minecraftforge.fml.common.registry.ForgeRegistries.ENTITIES
                .getValue(rl);

        if (entry != null) {
            try {
                // 用客户端世界创建一个“虚拟实体”
                net.minecraft.world.World world = Minecraft.getMinecraft().world;
                if (world != null) {
                    net.minecraft.entity.Entity entity = entry.newInstance(world);
                    if (entity != null) {
                        // ★ 核心：直接拿显示名（已经本地化）
                        return entity.getDisplayName().getFormattedText();
                    }
                }
            } catch (Throwable ignored) {
            }
        }

        // 兜底：如果其实是物品
        Item item = Item.REGISTRY.getObject(rl);
        if (item != null) {
            return item.getItemStackDisplayName(new ItemStack(item));
        }

        // 最终兜底
        return targetId;
    }

    public static int getReversedCurseCount(ItemStack ring) {
        if (ring.isEmpty() || !(ring.getItem() instanceof DepthRing)) {
            return 0;
        }

        int count = 0;
        NBTTagCompound tag = ring.getSubCompound("PurifyData");
        if (tag == null)
            return 0;

        // 一个个判断
        if (tag.getBoolean("DamageReversed"))
            count++;
        if (tag.getBoolean("ExpBoostReversed"))
            count++;
        if (tag.getBoolean("HauntedReversed"))
            count++;
        if (tag.getBoolean("VoidResistReversed"))
            count++;
        if (tag.getBoolean("LuckyReversed"))
            count++;
        // if (tag.getBoolean("FinalCurse")) count++;
        // 将来如果加新诅咒，在这里继续加

        return count;
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.player instanceof EntityPlayerMP))
            return;

        EntityPlayerMP player = (EntityPlayerMP) event.player;

        // ===== 根 NBT（旧逻辑依赖）=====
        NBTTagCompound root = player.getEntityData();

        // ===== 命运 / 时间相关数据（新逻辑）=====
        NBTTagCompound fate;
        if (!root.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
            fate = new NBTTagCompound();
            root.setTag(EntityPlayer.PERSISTED_NBT_TAG, fate);
        } else {
            fate = root.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        }

        // 记录本次登录时间（用于累计在线时长）
        fate.setLong("DepthRingLastJoinTime", System.currentTimeMillis());

        // ===== 以下是你原本的发戒指逻辑（语义不变）=====
        if (!ModConfig.giveRingOnLogin) {
            return;
        }

        if (root.getBoolean("hasReceivedDepthRing")) {
            return;
        }

        ItemStack item = new ItemStack(ModItems.DEPTH_RING, 1);
        player.inventory.addItemStackToInventory(item);

        // 标记为已领取（仍然写在 root，保持兼容）
        root.setBoolean("hasReceivedDepthRing", true);
    }


    public static boolean allBaseCursesReversed(ItemStack ring) {
        return DepthRing.isDamageReversed(ring)
                && DepthRing.isExpBoostReversed(ring)
                && DepthRing.isHauntedReversed(ring)
                && DepthRing.isVoidResistReversed(ring)
                && DepthRing.isLuckyReversed(ring);
    }

    public static void checkCurse(EntityPlayer player) {
        if (player.world.isRemote)
            return;

        NBTTagCompound persistent = player.getEntityData();
        NBTTagCompound data;
        if (!persistent.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
            data = new NBTTagCompound();
            persistent.setTag(EntityPlayer.PERSISTED_NBT_TAG, data);
        } else {
            data = persistent.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        }

        if (data.getBoolean("has_final_artifact_notice"))
            return;

        int slot = BaublesApi.isBaubleEquipped(player, ModItems.DEPTH_RING);
        if (slot == -1)
            return;
        ItemStack ring = BaublesApi.getBaublesHandler(player).getStackInSlot(slot);
        if (ring.isEmpty())
            return;

        if (DepthRing.getReversedCurseCount(ring) >= 5) {
            // 给予奖励
            player.sendMessage(
                    new TextComponentString(TextFormatting.AQUA + "深渊浮现出了新的提示"));
            player.world.playSound(null, player.posX, player.posY, player.posZ,
                    SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, // 播放任务完成音效
                    player.getSoundCategory(), 0.8F, 1.0F);
            data.setBoolean("has_final_artifact_notice", true);
            persistent.setTag(EntityPlayer.PERSISTED_NBT_TAG, data);
        }
    }




    public static long getRemainingMillis(EntityPlayer player) {
        int limitMinutes = ModConfig.depthRingUnequipTimeoutMinutes;
        if (limitMinutes <= 0) return -1;

        NBTTagCompound root = player.getEntityData();
        if (!root.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
            return limitMinutes * 60_000L;
        }

        NBTTagCompound data = root.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);

        // 已佩戴过 → 永久允许 → 不显示倒计时
        if (data.getBoolean("DepthRingHasEquipped")) {
            return -1;
        }

        long playedMillis = data.getLong("DepthRingPlayedMillis");



        long limitMillis = limitMinutes * 60_000L;
        return limitMillis - playedMillis;
    }





    private static final String TAG_CLIENT = "DepthRingClient";
    private static final String KEY_REMAIN_SEC = "remainSec";
    private static final String KEY_SEALED = "sealed";
    private static final String KEY_LAST_SYNC_SEC = "lastSyncSec"; // 放在player的PERSISTED里节流

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof EntityPlayerMP)) return;

        EntityPlayerMP player = (EntityPlayerMP) event.player;

        NBTTagCompound root = player.getEntityData();
        NBTTagCompound data = root.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);

        int limitMinutes = ModConfig.depthRingUnequipTimeoutMinutes;
        if (limitMinutes <= 0) return;

        // 已佩戴过不需要同步/提示
        if (data.getBoolean("DepthRingHasEquipped")) return;

        long limitMillis = limitMinutes * 60_000L;

        long playedMillis = data.getLong("DepthRingPlayedMillis");
        long lastJoin = data.getLong("DepthRingLastJoinTime");
        if (lastJoin > 0) playedMillis += Math.max(0, System.currentTimeMillis() - lastJoin);

        long remaining = limitMillis - playedMillis;

        boolean sealed = data.getBoolean("DepthRingSealed");
        if (!sealed && remaining <= 0) {
            sealed = true;
            data.setBoolean("DepthRingSealed", true);
            data.removeTag("DepthRingLastJoinTime");
            root.setTag(EntityPlayer.PERSISTED_NBT_TAG, data);
        }

        long remainSec = Math.max(0, remaining / 1000L);

        // ====== 节流：只在秒数变化时同步（避免每tick刷包）======
        long lastSyncSec = data.getLong(KEY_LAST_SYNC_SEC);
        if (lastSyncSec == remainSec && data.getBoolean("DepthRingSealed") == sealed) {
            // 秒数没变化就不动
        } else {
            data.setLong(KEY_LAST_SYNC_SEC, remainSec);
            root.setTag(EntityPlayer.PERSISTED_NBT_TAG, data);

            // 遍历玩家身上的 DepthRing，把数据写进 ItemStack NBT（会同步到客户端）
            for (ItemStack s : player.inventory.mainInventory) {
                if (!s.isEmpty() && s.getItem() == ModItems.DEPTH_RING) {
                    NBTTagCompound c = s.getOrCreateSubCompound(TAG_CLIENT);
                    c.setLong(KEY_REMAIN_SEC, remainSec);
                    c.setBoolean(KEY_SEALED, sealed);
                }
            }

            // 如果你的戒指也可能在副手/盔甲栏/其他位置，再补一遍遍历即可
        }

        // ====== 你原来的 90/60/30 提示逻辑（保留）======
        if (!sealed && remaining > 0) {
            double ratio = remaining / (double) limitMillis;

            if (ratio <= 0.9 && !data.getBoolean("DepthRingWarn90")) {
                player.sendMessage(new TextComponentString(TextFormatting.GRAY + "你感受到深渊的低语……时间正在流逝。"));
                data.setBoolean("DepthRingWarn90", true);
            }
            if (ratio <= 0.6 && !data.getBoolean("DepthRingWarn60")) {
                player.sendMessage(new TextComponentString(TextFormatting.GOLD + "深渊的封印正在收紧，你的选择正在减少。"));
                data.setBoolean("DepthRingWarn60", true);
            }
            if (ratio <= 0.3 && !data.getBoolean("DepthRingWarn30")) {
                player.sendMessage(new TextComponentString(TextFormatting.RED + "深渊即将封闭，这是你最后的机会。"));
                data.setBoolean("DepthRingWarn30", true);
            }

            root.setTag(EntityPlayer.PERSISTED_NBT_TAG, data);
        }
    }

}
