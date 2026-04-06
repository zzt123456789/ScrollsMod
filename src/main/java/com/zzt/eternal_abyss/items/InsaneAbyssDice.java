package com.zzt.eternal_abyss.items;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;
import com.zzt.eternal_abyss.event.DepthRingHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class InsaneAbyssDice extends ItemBase implements IBauble {
    public InsaneAbyssDice(String name) {
        super(name);
        this.setMaxStackSize(1);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.RARE;
    }

    @Override
    public BaubleType getBaubleType(ItemStack itemStack) {
        return BaubleType.BELT;
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

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(I18n.format("tooltip.eternal_abyss.pref"));

        tooltip.add("");

        tooltip.add(I18n.format("tooltip.insane_dice.brief"));
        tooltip.add("");


        if (GuiScreen.isShiftKeyDown()) {
            tooltip.add(I18n.format("tooltip.insane_dice.details4"));
            tooltip.add("");
            tooltip.add(I18n.format("tooltip.insane_dice.details1"));
            tooltip.add(I18n.format("tooltip.insane_dice.details2"));
            tooltip.add(I18n.format("tooltip.insane_dice.details3"));
            tooltip.add("");
            tooltip.add(I18n.format("tooltip.insane_dice.details5"));
            tooltip.add(I18n.format("tooltip.insane_dice.details6"));
            tooltip.add("");
            tooltip.add(I18n.format("tooltip.insane_dice.details8"));
            tooltip.add(I18n.format("tooltip.insane_dice.details9"));
        }else{
            tooltip.add(I18n.format("tooltip.insane_dice.holdShift"));
        }

    }
}
