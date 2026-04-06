package com.zzt.eternal_abyss.items;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;
import com.zzt.eternal_abyss.event.DepthRingHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class WailingCore extends ItemBase implements IBauble {


    public WailingCore(String name) {
        super(name);
        this.setMaxStackSize(1);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.RARE;
    }

    @Override
    public BaubleType getBaubleType(ItemStack itemStack) {
        return BaubleType.CHARM;
    }

    @Override
    public void onEquipped(ItemStack stack, EntityLivingBase player) {
        player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, .75F, 1.9f);

    }

    @Override
    public void onUnequipped(ItemStack stack, EntityLivingBase player) {
        player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, .75F, 2f);

    }

    @Override
    public boolean canEquip(ItemStack stack, EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer)) return false;

        EntityPlayer player = (EntityPlayer) entity;

        // 必须佩戴深渊之戒
        ItemStack ring = DepthRingHandler.getEquippedDepthRing(player);
        if (ring.isEmpty()) return false;

        // 禁止重复佩戴相同饰品
        int equippedIndex = BaublesApi.isBaubleEquipped(player, this);
        return equippedIndex == -1;
    }


    public static int getStoredVoidDamage(ItemStack stack) {
        NBTTagCompound tag = stack.getOrCreateSubCompound("VoidEcho");
        return tag.getInteger("StoredDamage");
    }

    public static void setStoredVoidDamage(ItemStack stack, int value) {
        stack.getOrCreateSubCompound("VoidEcho").setInteger("StoredDamage", value);
    }

    public static int getVoidEchoHitsUsed(ItemStack stack) {
        NBTTagCompound tag = stack.getOrCreateSubCompound("VoidEcho");
        return tag.getInteger("UsedHits");
    }

    public static void setVoidEchoHitsUsed(ItemStack stack, int value) {
        stack.getOrCreateSubCompound("VoidEcho").setInteger("UsedHits", value);
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(I18n.format("tooltip.eternal_abyss.pref"));
        tooltip.add("");

        tooltip.add(I18n.format("tooltip.wailing_core.brief"));

        if (GuiScreen.isShiftKeyDown()) {
            EntityPlayer player = net.minecraft.client.Minecraft.getMinecraft().player;
            if (player != null) {
                ItemStack ring = DepthRingHandler.getEquippedDepthRing(player);
                boolean reversed = !ring.isEmpty() && DepthRing.isHauntedReversed(ring);

                if(!reversed){
                    tooltip.add(I18n.format("tooltip.wailing_core.details1"));
                    tooltip.add(I18n.format("tooltip.wailing_core.details2"));
                    tooltip.add(I18n.format("tooltip.wailing_core.details3"));
                }
                else{
                    tooltip.add("");
                    tooltip.add(I18n.format("tooltip.wailing_core.details5"));
                    tooltip.add(I18n.format("tooltip.wailing_core.details6"));
                    tooltip.add(I18n.format("tooltip.wailing_core.details7"));
                    tooltip.add(I18n.format("tooltip.wailing_core.details12"));

                    tooltip.add("");

                    ItemStack equipped = com.zzt.eternal_abyss.event.WailingCoreHandler.getEquippedWailingCore(player);
                    if (!equipped.isEmpty()) {
                        int stored = WailingCore.getStoredVoidDamage(stack);
                        tooltip.add(I18n.format("tooltip.wailing_core.details11", stored));
                    }
                }
            }
            tooltip.add("");
            tooltip.add(I18n.format("tooltip.wailing_core.details9"));
            tooltip.add(I18n.format("tooltip.wailing_core.details8"));
            tooltip.add("");
            tooltip.add(I18n.format("tooltip.wailing_core.details10"));
            tooltip.add(I18n.format("tooltip.wailing_core.details13"));
        } else {
            tooltip.add(I18n.format("tooltip.wailing_core.holdShift"));
            tooltip.add("");
            EntityPlayer player = net.minecraft.client.Minecraft.getMinecraft().player;
            if (player != null) {
                ItemStack ring = DepthRingHandler.getEquippedDepthRing(player);
                boolean reversed = !ring.isEmpty() && DepthRing.isHauntedReversed(ring);
                tooltip.add(reversed ? TextFormatting.GREEN + "已受到深渊的祝福获得增强"
                        : TextFormatting.RED + "还有未尽之事...");


            }
            tooltip.add("");
        }
    }

}
