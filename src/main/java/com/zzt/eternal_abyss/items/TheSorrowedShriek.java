package com.zzt.eternal_abyss.items;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;
import com.tmtravlr.potioncore.PotionCoreAttributes;
import com.zzt.eternal_abyss.event.DepthRingHandler;
import com.zzt.eternal_abyss.event.TheSorrowedShriekHandler;
import com.zzt.eternal_abyss.font.ModFontRenderers;
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
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

import static com.zzt.eternal_abyss.items.TheTwistedFate.revealFormattedText;

public class TheSorrowedShriek extends ItemBase implements IBauble {

    public TheSorrowedShriek(String name) {
        super(name);
        this.setMaxStackSize(1);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.EPIC;
    }

    @Override
    public BaubleType getBaubleType(ItemStack itemStack) {
        return BaubleType.AMULET;
    }

    private static final UUID MOVE_SPEED_UUID =
            UUID.fromString("9c267e34-8d8c-4e3d-9c34-6b7a2a900001");
    private static final UUID MINING_SPEED_UUID =
            UUID.fromString("9c267e34-8d8c-4e3d-9c34-6b7a2a900002");
    private static final UUID ARROR_DAMAGE_UUID =
            UUID.fromString("9c267e34-8d8c-4e3d-9c34-6b7a2a900003");

    @Override
    public void onWornTick(ItemStack stack, EntityLivingBase living) {

        if (!(living instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) living;


        IAttributeInstance move =
                player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (move != null) {
            AttributeModifier old = move.getModifier(MOVE_SPEED_UUID);
            if (old != null) move.removeModifier(old);

            move.applyModifier(new AttributeModifier(
                    MOVE_SPEED_UUID,
                    "sorrowed_shriek_move_speed",
                    0.5D,
                    2
            ));
        }


        IAttributeInstance mining =
                player.getEntityAttribute(PotionCoreAttributes.DIG_SPEED);
        if (mining != null) {
            AttributeModifier old = mining.getModifier(MINING_SPEED_UUID);
            if (old != null) mining.removeModifier(old);

            mining.applyModifier(new AttributeModifier(
                    MINING_SPEED_UUID,
                    "sorrowed_shriek_mining_speed",
                    1.0D,
                    2
            ));
        }

        IAttributeInstance arrow =
                player.getEntityAttribute(PotionCoreAttributes.PROJECTILE_DAMAGE);
        if (arrow != null) {
            AttributeModifier old = arrow.getModifier(ARROR_DAMAGE_UUID);
            if (old != null) arrow.removeModifier(old);
            arrow.applyModifier(new AttributeModifier(
                    ARROR_DAMAGE_UUID,
                    "sorrowed_shriek_arrow_dmg",
                    1.0D,
                    2
            ));
        }

        TheSorrowedShriekHandler.tickFlight(player);


    }


    @Override
    public void onUnequipped(ItemStack stack, EntityLivingBase living) {

        if (!(living instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) living;

        IAttributeInstance move =
                player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (move != null) {
            AttributeModifier old = move.getModifier(MOVE_SPEED_UUID);
            if (old != null) move.removeModifier(old);
        }

        IAttributeInstance mining =
                player.getEntityAttribute(PotionCoreAttributes.DIG_SPEED);
        if (mining != null) {
            AttributeModifier old = mining.getModifier(MINING_SPEED_UUID);
            if (old != null) mining.removeModifier(old);
        }

        IAttributeInstance arrow =
                player.getEntityAttribute(PotionCoreAttributes.PROJECTILE_DAMAGE);
        if (arrow != null) {
            AttributeModifier old = arrow.getModifier(ARROR_DAMAGE_UUID);
            if (old != null) arrow.removeModifier(old);
        }

        TheSorrowedShriekHandler.removeFlight(player);
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

        boolean shift = GuiScreen.isShiftKeyDown();
        boolean alt = GuiScreen.isAltKeyDown();

        // ===== Shift 区域 =====
        if (shift) {
            tooltip.add(I18n.format("tooltip.eternal_abyss.sorrowed_shriek.line1"));
            tooltip.add(I18n.format("tooltip.eternal_abyss.sorrowed_shriek.line2"));
            tooltip.add("");
            tooltip.add(I18n.format("tooltip.eternal_abyss.sorrowed_shriek.line3"));
            tooltip.add(I18n.format("tooltip.eternal_abyss.sorrowed_shriek.line4"));
            tooltip.add(I18n.format("tooltip.eternal_abyss.sorrowed_shriek.line5"));
        }

        // ===== Alt 区域 =====
        if (alt) {
            if (shift) {
                tooltip.add("");
            }

            String brief = I18n.format("tooltip.eternal_abyss.sorrowed_shriek.brief");
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
