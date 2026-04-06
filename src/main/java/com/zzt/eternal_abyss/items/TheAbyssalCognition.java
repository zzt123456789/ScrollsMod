package com.zzt.eternal_abyss.items;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;
import com.tmtravlr.potioncore.PotionCoreAttributes;
import com.zzt.eternal_abyss.event.DepthRingHandler;
import com.zzt.eternal_abyss.font.ModFontRenderers;
import com.zzt.eternal_abyss.init.ModAttributes;
import com.zzt.eternal_abyss.init.ModDamageSource;
import com.zzt.eternal_abyss.init.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

import static com.zzt.eternal_abyss.items.TheTwistedFate.revealFormattedText;

public class TheAbyssalCognition extends ItemBase implements IBauble {

    private static final UUID MOD_ATTACK_DAMAGE  = UUID.fromString("11111111-aaaa-bbbb-cccc-000000000001");
    private static final UUID MOD_ATTACK_SPEED   = UUID.fromString("11111111-aaaa-bbbb-cccc-000000000002");
    private static final UUID MOD_CRIT_CHANCE    = UUID.fromString("11111111-aaaa-bbbb-cccc-000000000003");
    private static final UUID MOD_CRIT_DAMAGE    = UUID.fromString("11111111-aaaa-bbbb-cccc-000000000004");
    private static final UUID MOD_MAGIC_DAMAGE = UUID.fromString("11111111-aaaa-bbbb-cccc-000000000005");
    private static final UUID MOD_ARROW_DAMAGE = UUID.fromString("11111111-aaaa-bbbb-cccc-000000000006");


    // tick 计数器

    public TheAbyssalCognition(String name) {
        super(name);
        this.setMaxStackSize(1);
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
    public void onWornTick(ItemStack stack, EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) entity;

        IAttributeInstance dmg = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        if (dmg.getModifier(MOD_ATTACK_DAMAGE) == null) {
            dmg.applyModifier(new AttributeModifier(
                    MOD_ATTACK_DAMAGE,
                    "abyss_cognition_dmg",
                    1,
                    2
            ));
        }

        IAttributeInstance asp = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED);
        if (asp.getModifier(MOD_ATTACK_SPEED) == null) {
            asp.applyModifier(new AttributeModifier(
                    MOD_ATTACK_SPEED,
                    "abyss_cognition_asp",
                    1,
                    2
            ));
        }

        IAttributeInstance magic = player.getEntityAttribute(PotionCoreAttributes.MAGIC_DAMAGE);
        if (magic != null && magic.getModifier(MOD_MAGIC_DAMAGE) == null) {
            magic.applyModifier(new AttributeModifier(
                    MOD_MAGIC_DAMAGE,
                    "abyss_cognition_magic",
                    1,
                    2
            ));
        }
        // 增加箭矢伤害
        IAttributeInstance arrowDmg = player.getEntityAttribute(PotionCoreAttributes.PROJECTILE_DAMAGE);
        if (arrowDmg != null && arrowDmg.getModifier(MOD_ARROW_DAMAGE) == null) {
            arrowDmg.applyModifier(new AttributeModifier(
                    MOD_ARROW_DAMAGE,
                    "abyss_cognition_arrow_dmg",
                    1,
                    2
            ));
        }

        if (DepthRing.isDamageReversedFor(player)) {
            IAttributeInstance cc = player.getEntityAttribute(ModAttributes.CRIT_CHANCE);
            if (cc.getModifier(MOD_CRIT_CHANCE) == null) {
                cc.applyModifier(new AttributeModifier(
                        MOD_CRIT_CHANCE,
                        "abyss_cognition_cc",
                        1,
                        2
                ));
            }

            IAttributeInstance cd = player.getEntityAttribute(ModAttributes.CRIT_DAMAGE);
            if (cd.getModifier(MOD_CRIT_DAMAGE) == null) {
                cd.applyModifier(new AttributeModifier(
                        MOD_CRIT_DAMAGE,
                        "abyss_cognition_cd",
                        0.5,
                        2
                ));
            }


        }

        NBTTagCompound tag = stack.getOrCreateSubCompound("AbyssCogData");

        int tick = tag.getInteger("Tick");
        tick++;

        if (tick >= 20) { // 20 tick = 1 秒
            tick = 0;

            int totalXP = getPlayerXP(player);

            if (totalXP >= 100) {
                drainPlayerXP(player, 100);
            } else {
                player.attackEntityFrom(ModDamageSource.VOID_DAMAGE, 10.0F);
            }
        }

        tag.setInteger("Tick", tick);


    }


    /**
     * 每一级所需 XP（原版公式）
     */
    public static int getXpForLevel(int level) {
        return level >= 30 ? 112 + (level - 30) * 9 :
                level >= 15 ? 37 + (level - 15) * 5 :
                        7 + level * 2;
    }
    public static int getPlayerXP(EntityPlayer player) {
        int xp = 0;
        for (int i = 0; i < player.experienceLevel; i++) {
            xp += getXpForLevel(i);
        }
        xp += Math.round(player.experience * getXpForLevel(player.experienceLevel));
        return xp;
    }
    /**
     * 设置玩家总经验（自动重新计算等级 + 经验条）
     */
    private void setPlayerXP(EntityPlayer player, int xp) {
        player.experienceTotal = xp;
        player.experienceLevel = 0;
        player.experience = 0F;

        int remaining = xp;
        while (true) {
            int xpForLevel = getXpForLevel(player.experienceLevel);
            if (remaining >= xpForLevel) {
                remaining -= xpForLevel;
                player.experienceLevel++;
            } else {
                player.experience = (float) remaining / (float) xpForLevel;
                break;
            }
        }
    }

    /**
     * 扣除 XP（raw XP）
     */
    private void drainPlayerXP(EntityPlayer player, int xpToDrain) {
        int currentXP = getPlayerXP(player);
        setPlayerXP(player, Math.max(0, currentXP - xpToDrain));
    }


    @Override
    public void onUnequipped(ItemStack stack, EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) entity;

        removeModifier(player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE), MOD_ATTACK_DAMAGE);
        removeModifier(player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED), MOD_ATTACK_SPEED);
        removeModifier(player.getEntityAttribute(ModAttributes.CRIT_CHANCE), MOD_CRIT_CHANCE);
        removeModifier(player.getEntityAttribute(ModAttributes.CRIT_DAMAGE), MOD_CRIT_DAMAGE);
        removeModifier(player.getEntityAttribute(PotionCoreAttributes.MAGIC_DAMAGE), MOD_MAGIC_DAMAGE);
        removeModifier(player.getEntityAttribute(PotionCoreAttributes.PROJECTILE_DAMAGE), MOD_ARROW_DAMAGE);
    }

    public static void removeModifier(IAttributeInstance attr, UUID id) {
        if (attr == null) return;
        AttributeModifier m = attr.getModifier(id);
        if (m != null) attr.removeModifier(m);
    }





    @Override
    public boolean canEquip(ItemStack stack, EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer)) return false;

        EntityPlayer player = (EntityPlayer) entity;

        ItemStack ring = DepthRingHandler.getEquippedDepthRing(player);
        if (ring.isEmpty()) {
            return false; // 没有佩戴深渊之戒，禁止装备
        }
        // 检查是否已佩戴相同饰品（防止重复）
        int equippedIndex = BaublesApi.isBaubleEquipped(player, this);
        return equippedIndex == -1; // 只允许佩戴一个
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {

        tooltip.add(I18n.format("tooltip.eternal_abyss.pref"));
        tooltip.add("");

        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null) {
            tooltip.add("");
            return;
        }

        boolean shift = GuiScreen.isShiftKeyDown();
        boolean alt = GuiScreen.isAltKeyDown();

        // ===== Shift 区域 =====
        if (shift) {

            // 未反转攻击诅咒
            if (!DepthRing.isDamageReversedFor(player)) {
                tooltip.add(I18n.format("tooltip.eternal_abyss.abyssal_cognition.line1"));
                tooltip.add(I18n.format("tooltip.eternal_abyss.abyssal_cognition.line2"));
                tooltip.add(I18n.format("tooltip.eternal_abyss.abyssal_cognition.line3"));
                tooltip.add(I18n.format("tooltip.eternal_abyss.abyssal_cognition.line5",
                        getPlayerXP(player)));

            } else {
                // 已反转攻击诅咒
                tooltip.add(I18n.format("tooltip.eternal_abyss.abyssal_cognition.line1"));
                tooltip.add(I18n.format("tooltip.eternal_abyss.abyssal_cognition.line2"));
                tooltip.add(I18n.format("tooltip.eternal_abyss.abyssal_cognition.line3"));
                tooltip.add("");
                tooltip.add(I18n.format("tooltip.eternal_abyss.abyssal_cognition.line4"));
                tooltip.add(I18n.format("tooltip.eternal_abyss.abyssal_cognition.line6"));
                tooltip.add(I18n.format("tooltip.eternal_abyss.abyssal_cognition.line5",
                        getPlayerXP(player)));
            }
        }

        // ===== Alt 区域 =====
        if (alt) {
            if (shift) {
                tooltip.add("");
            }

            String brief = I18n.format("tooltip.eternal_abyss.abyssal_cognition.brief");
            int visibleChars = com.zzt.eternal_abyss.client.tooltip.AltRevealState.getVisibleChars(15);
            String partial = revealFormattedText(brief, visibleChars);

            if (partial != null && !partial.isEmpty()) {
                tooltip.add(partial);
            } else {
                tooltip.add(" ");
            }
        }

        // ===== 都没按时，显示两个提示 =====
        if (!shift && !alt) {
            tooltip.add(I18n.format("tooltip.depth_ring.hold_shift"));
            tooltip.add(I18n.format("tooltip.eternal_abyss.holdAlt"));
        }

        tooltip.add("");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public FontRenderer getFontRenderer(ItemStack stack) {
        return ModFontRenderers.SPECIAL_FONT;
    }
}
