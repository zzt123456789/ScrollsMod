package com.zzt.eternal_abyss.items;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;
import com.zzt.eternal_abyss.event.DepthRingHandler;
import com.zzt.eternal_abyss.font.ModFontRenderers;
import com.zzt.eternal_abyss.init.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

import static com.zzt.eternal_abyss.event.WailingCoreHandler.getEquippedWailingCore;
import static com.zzt.eternal_abyss.event.WailingCoreHandler.isWailingReversed;
import static com.zzt.eternal_abyss.items.TheTwistedFate.revealFormattedText;

public class TheDiminishedShade extends ItemBase implements IBauble {

    private static final UUID HP_BOOST_ID = UUID.fromString("b0725cef-21d0-4c4d-9b09-821c049da123");

    public TheDiminishedShade(String name) {
        super(name);
        this.setMaxStackSize(1);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.EPIC;
    }

    @Override
    public BaubleType getBaubleType(ItemStack stack) {
        return BaubleType.BODY;
    }

    @Override
    public boolean canEquip(ItemStack stack, EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer)) return false;
        EntityPlayer player = (EntityPlayer) entity;

        // 必须佩戴深渊之戒
        ItemStack ring = DepthRingHandler.getEquippedDepthRing(player);
        if (ring.isEmpty()) return false;

        // 防止重复装备
        return BaublesApi.isBaubleEquipped(player, this) == -1;
    }

    @Override
    public void onWornTick(ItemStack itemstack, EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) entity;


        AttributeModifier mod = new AttributeModifier(
                HP_BOOST_ID,
                "void_shade_hp_boost",
                1.0,
                1
        );

        if (!player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).hasModifier(mod)) {
            player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(mod);

            if (player.getHealth() < player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }
        }
        boolean hasVoidArtifact =
                baubles.api.BaublesApi.isBaubleEquipped(player, ModItems.VOID_ARTIFACT) != -1;

        boolean hasWailingCore =
                baubles.api.BaublesApi.isBaubleEquipped(player, ModItems.WAILING_CORE) != -1;

        boolean hasDimishedShade =
                baubles.api.BaublesApi.isBaubleEquipped(player, ModItems.THE_DIMINISHED_SHADE) != -1;

        if (hasVoidArtifact || hasWailingCore) {
            return;
        }

        if (!hasDimishedShade) {
            return;
        }


        List<EntityMob> mobs = player.world.getEntitiesWithinAABB(
                EntityMob.class,
                player.getEntityBoundingBox().grow(50)
        );

        for (EntityMob mob : mobs) {
            if (mob instanceof IRangedAttackMob) continue;
            if (mob instanceof EntityCreeper) continue;


            mob.setRevengeTarget(player);
            mob.setAttackTarget(player);

            if (mob.ticksExisted % 2 == 0) {
                mob.getNavigator().tryMoveToEntityLiving(player, 1.5D);
            }
        }
    }

    @Override
    public void onUnequipped(ItemStack stack, EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) entity;

        // 移除生命翻倍
        player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH)
                .removeModifier(HP_BOOST_ID);

    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(I18n.format("tooltip.eternal_abyss.pref"));
        tooltip.add("");

        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null) return;

        boolean shift = GuiScreen.isShiftKeyDown();
        boolean alt = GuiScreen.isAltKeyDown();

        // ===== Shift 区域 =====
        if (shift) {
            if (!DepthRing.isVoidResistReversedFor(player)) {
                tooltip.add(I18n.format("tooltip.eternal_abyss.diminished_shade.line1"));
                tooltip.add("");
                tooltip.add(I18n.format("tooltip.eternal_abyss.diminished_shade.line6"));
                tooltip.add(I18n.format("tooltip.eternal_abyss.diminished_shade.line2"));
                tooltip.add(I18n.format("tooltip.eternal_abyss.diminished_shade.line3"));
            } else {
                double max = player.getMaxHealth();
                double current = player.getHealth();
                double lost = max - current;

                int multiple = (int) (lost / 6.0);
                double bonus = multiple * 9;

                tooltip.add(I18n.format("tooltip.eternal_abyss.diminished_shade.line1"));
                tooltip.add("");
                tooltip.add(I18n.format("tooltip.eternal_abyss.diminished_shade.line6"));
                tooltip.add(I18n.format("tooltip.eternal_abyss.diminished_shade.line3"));
                tooltip.add(" ");
                tooltip.add(I18n.format("tooltip.eternal_abyss.diminished_shade.line4"));
                tooltip.add(I18n.format("tooltip.eternal_abyss.diminished_shade.line5", bonus));
            }
        }

        // ===== Alt 区域 =====
        if (alt) {
            if (shift) {
                tooltip.add("");
            }

            String brief = I18n.format("tooltip.eternal_abyss.diminished_shade.brief");
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
