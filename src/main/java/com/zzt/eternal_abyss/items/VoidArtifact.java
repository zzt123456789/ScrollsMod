package com.zzt.eternal_abyss.items;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;
import baubles.api.cap.IBaublesItemHandler;
import com.zzt.eternal_abyss.client.tooltip.AltRevealState;
import com.zzt.eternal_abyss.event.DepthRingHandler;
import com.zzt.eternal_abyss.font.ModFontRenderers;
import com.zzt.eternal_abyss.util.KeepBaubleHelper;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

import static com.zzt.eternal_abyss.font.ModFontRenderers.VOID_ARTIFACT_RENDERER;

@Mod.EventBusSubscriber
public class VoidArtifact extends ItemBase implements IBauble {
    private static final UUID KNOCKBACK_RESISTANCE_UUID =
            UUID.fromString("d1b4e3c0-7f3c-4a2e-b8e4-7d5d2c1f77ab");

    public VoidArtifact(String name) {
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

    @Override
    public void onEquipped(ItemStack stack, EntityLivingBase player) {
        player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, .75F, 1.9f);

        if (player instanceof EntityPlayer) {
            KeepBaubleHelper.addKeepItem((EntityPlayer) player, this);
        }

        if (player instanceof EntityPlayer) {
            AttributeModifier modifier = new AttributeModifier(
                    KNOCKBACK_RESISTANCE_UUID,
                    "VoidArtifact knockback resistance",
                    10.0D, // 100%免疫击退
                    0
            );

            if (!player.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE)
                    .hasModifier(modifier)) {
                player.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE)
                        .applyModifier(modifier);
            }
        }
    }

    @Override
    public void onUnequipped(ItemStack stack, EntityLivingBase player) {
        player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, .75F, 2f);

        if (player instanceof EntityPlayer) {
            KeepBaubleHelper.removeKeepItem((EntityPlayer) player, this);
        }

        if (player.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE) != null) {
            player.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE)
                    .removeModifier(KNOCKBACK_RESISTANCE_UUID);
        }
    }

    @Override
    public boolean canEquip(ItemStack stack, EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer)) return false;

        EntityPlayer player = (EntityPlayer) entity;

        // 检查玩家是否已佩戴深渊之戒
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
        boolean shiftDown = GuiScreen.isShiftKeyDown();
        boolean altDown = GuiScreen.isAltKeyDown();

        tooltip.add(I18n.format("tooltip.void_artifact.desc1"));
        tooltip.add("");

        if (shiftDown) {
            addShiftTooltip(tooltip);
            if (altDown) {
                tooltip.add("");
            }
        }

        if (altDown) {
            addAltTooltip(tooltip, stack);
        }

        if (!shiftDown && !altDown) {
            tooltip.add(I18n.format("tooltip.void_artifact.hold_shift"));
            tooltip.add(I18n.format("tooltip.void_artifact.hold_alt"));
        }
    }


    @SideOnly(Side.CLIENT)
    private void addShiftTooltip(List<String> tooltip) {
        tooltip.add(I18n.format("tooltip.void_artifact.line1"));
        tooltip.add(I18n.format("tooltip.void_artifact.line2"));
        tooltip.add(I18n.format("tooltip.void_artifact.line3"));
        tooltip.add(I18n.format("tooltip.void_artifact.line4"));
        tooltip.add(I18n.format("tooltip.void_artifact.line5"));
        tooltip.add(I18n.format("tooltip.void_artifact.line6"));
        tooltip.add(I18n.format("tooltip.void_artifact.line7"));
        tooltip.add(I18n.format("tooltip.void_artifact.line8"));
        tooltip.add(I18n.format("tooltip.void_artifact.line9"));
        tooltip.add(I18n.format("tooltip.void_artifact.line10"));
        tooltip.add(I18n.format("tooltip.void_artifact.line11"));
        tooltip.add(I18n.format("tooltip.void_artifact.line12"));
        tooltip.add(I18n.format("tooltip.void_artifact.line13"));
    }

    @SideOnly(Side.CLIENT)
    private void addAltTooltip(List<String> tooltip, ItemStack stack) {
        int remaining = AltRevealState.getVisibleChars(17);

        remaining = addSequentialRevealLine(tooltip, I18n.format("tooltip.void_artifact.alt.line1"), remaining);
        tooltip.add("");
        remaining = addSequentialRevealLine(tooltip, I18n.format("tooltip.void_artifact.alt.line2"), remaining);
    }

    @SideOnly(Side.CLIENT)
    private int addSequentialRevealLine(List<String> tooltip, String fullText, int remainingVisibleChars) {
        int fullVisibleLength = getVisibleTextLength(fullText);
        int charsForThisLine = Math.min(remainingVisibleChars, fullVisibleLength);

        String partial = revealFormattedText(fullText, charsForThisLine);
        tooltip.add(partial.isEmpty() ? " " : partial);

        return Math.max(0, remainingVisibleChars - fullVisibleLength);
    }


    @SideOnly(Side.CLIENT)
    private String revealFormattedText(String text, int visibleChars) {
        if (text == null || text.isEmpty()) return "";
        if (visibleChars <= 0) return "";

        StringBuilder out = new StringBuilder();
        int shown = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '\u00A7' && i + 1 < text.length()) {
                out.append(c);
                out.append(text.charAt(i + 1));
                i++;
                continue;
            }

            if (shown >= visibleChars) {
                break;
            }

            out.append(c);
            shown++;
        }

        return out.toString();
    }

    @SideOnly(Side.CLIENT)
    private int getVisibleTextLength(String text) {
        if (text == null || text.isEmpty()) return 0;

        int len = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '\u00A7' && i + 1 < text.length()) {
                i++;
                continue;
            }

            len++;
        }
        return len;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public FontRenderer getFontRenderer(ItemStack stack) {
        return ModFontRenderers.VOID_ARTIFACT_RENDERER;
    }

}
