package com.zzt.eternal_abyss.items;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;
import com.zzt.eternal_abyss.event.DepthRingHandler;
import com.zzt.eternal_abyss.event.TheTwistedFateHandler;
import com.zzt.eternal_abyss.font.ModFontRenderers;
import com.zzt.eternal_abyss.init.ModItems;
import com.zzt.eternal_abyss.util.compat.CompatFirstAid;
import com.zzt.eternal_abyss.util.compat.ModCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

import static com.zzt.eternal_abyss.util.compat.CompatFirstAid.apply20pct;
import static com.zzt.eternal_abyss.util.compat.CompatFirstAid.removeCap;

public class TheTwistedFate extends ItemBase  implements IBauble {
    public TheTwistedFate(String name) {
        super(name);
        this.setMaxStackSize(1);
    }






    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.EPIC;
    }

    @Override
    public BaubleType getBaubleType(ItemStack itemStack) {
        return BaubleType.HEAD;
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
    public void onWornTick(ItemStack itemstack, EntityLivingBase player) {

        if (player instanceof EntityPlayerMP) {

            if (ModCompat.COMPAT_FIRSTAID) {
                CompatFirstAid.apply20pct((EntityPlayerMP) player);
            } else {
                if(player.getHealth() >= player.getMaxHealth() * 0.2F)
                {
                    player.setHealth(player.getMaxHealth() * 0.2F);
                }
            }
        }

        Potion bleed = Potion.getPotionFromResourceLocation("mod_lavacow:fragile");
        if (bleed != null && player.isPotionActive(bleed)) {
            player.removePotionEffect(bleed);
        }
    }


    @SubscribeEvent
    public void onPotionAdded(PotionEvent.PotionApplicableEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayerMP)) return;
        EntityPlayerMP player = (EntityPlayerMP) event.getEntityLiving();

        if (event.getPotionEffect().getPotion() == Potion.getPotionFromResourceLocation("mod_lavacow:fragile")) {
            if (!playerHasImmuneFragileItem(player).isEmpty()) {
                event.setCanceled(true);
            }
        }

    }

    public static ItemStack playerHasImmuneFragileItem(EntityPlayer player) {
        int slot = BaublesApi.isBaubleEquipped(player, ModItems.THE_TWISTED_FATE);
        if (slot != -1) {
            return BaublesApi.getBaublesHandler(player).getStackInSlot(slot);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void onUnequipped(ItemStack itemstack, EntityLivingBase player) {
        if (player instanceof EntityPlayerMP) {
            if (ModCompat.COMPAT_FIRSTAID) {

                removeCap((EntityPlayerMP) player);
            } else {
                float maxHealth = player.getMaxHealth();
                if (player.getHealth() < maxHealth) {
                    player.setHealth(maxHealth);
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(I18n.format("tooltip.eternal_abyss.pref"));
        tooltip.add("");

        EntityPlayer player = Minecraft.getMinecraft().player;

        boolean shift = GuiScreen.isShiftKeyDown();
        boolean alt = GuiScreen.isAltKeyDown();

        // ===== Shift 区域 =====
        if (shift) {
            if (!DepthRing.isLuckyReversedFor(player)) {
                tooltip.add(I18n.format("tooltip.eternal_abyss.twisted_fate.line1"));
                tooltip.add(I18n.format("tooltip.eternal_abyss.twisted_fate.line2"));
            } else {
                double luckValue = TheTwistedFateHandler.getBaseLuckWithoutSelf(player);
                tooltip.add(I18n.format("tooltip.eternal_abyss.twisted_fate.line3"));
                tooltip.add(I18n.format("tooltip.eternal_abyss.twisted_fate.line2"));
                tooltip.add(" ");
                tooltip.add(I18n.format("tooltip.eternal_abyss.twisted_fate.line4"));
                tooltip.add(I18n.format("tooltip.eternal_abyss.twisted_fate.line5", luckValue * 6));
            }
        }

        // ===== Alt 区域 =====
        if (alt) {
            if (shift) {
                tooltip.add("");
            }

            String brief = I18n.format("tooltip.eternal_abyss.twisted_fate.brief");
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

    @SideOnly(Side.CLIENT)
    public static String revealFormattedText(String text, int visibleChars) {
        if (text == null || text.isEmpty() || visibleChars <= 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        int shown = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            // 保留格式码，但不计入显示字符数
            if (c == '\u00A7' && i + 1 < text.length()) {
                sb.append(c);
                sb.append(text.charAt(i + 1));
                i++;
                continue;
            }

            if (shown >= visibleChars) {
                break;
            }

            sb.append(c);
            shown++;
        }

        return sb.toString();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public FontRenderer getFontRenderer(ItemStack stack) {
        return ModFontRenderers.SPECIAL_FONT;
    }


}
