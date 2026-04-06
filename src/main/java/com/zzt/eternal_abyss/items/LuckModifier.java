package com.zzt.eternal_abyss.items;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class LuckModifier extends ItemBase implements IBauble {

    public static final UUID LUCK_MODIFIER_ID = UUID.fromString("2c77c3c0-3cfd-4d45-b7db-1e0ddf77a951");
    public static final String NBT_LUCK_AMOUNT = "LuckValue";

    public LuckModifier(String name) {
        super(name);
        this.setMaxStackSize(1);
    }



    public static double getLuckValue(ItemStack stack) {
        if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey(NBT_LUCK_AMOUNT)) {
            return 0.0;
        }
        return stack.getTagCompound().getDouble(NBT_LUCK_AMOUNT);
    }

    public static void setLuckValue(ItemStack stack, double value) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setDouble(NBT_LUCK_AMOUNT, value);
    }

    @Override
    public BaubleType getBaubleType(ItemStack itemStack) {
        return BaubleType.TRINKET;
    }

    @Override
    public void onEquipped(ItemStack stack, EntityLivingBase entity) {
        if (entity instanceof EntityPlayer) {
            entity.world.playSound(null, entity.posX, entity.posY, entity.posZ,
                    SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.5F, 1.0F);
        }
    }

    @Override
    public void onUnequipped(ItemStack stack, EntityLivingBase entity) {
        if (entity instanceof EntityPlayer) {
            entity.world.playSound(null, entity.posX, entity.posY, entity.posZ,
                    SoundEvents.BLOCK_NOTE_HARP, SoundCategory.PLAYERS, 0.3F, 0.5F);
        }
    }

    @Override
    public boolean canEquip(ItemStack stack, EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer)) return false;
        EntityPlayer player = (EntityPlayer) entity;
        return BaublesApi.isBaubleEquipped(player, this) == -1;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        double nbtLuck = getLuckValue(stack);
        tooltip.add(TextFormatting.GRAY + "预设幸运值: " + TextFormatting.YELLOW + String.format("%.2f", nbtLuck));

        if (worldIn != null && Minecraft.getMinecraft().player != null) {
            EntityPlayer player = Minecraft.getMinecraft().player;
            IAttributeInstance luckAttr = player.getEntityAttribute(SharedMonsterAttributes.LUCK);
            if (luckAttr != null) {
                AttributeModifier modifier = luckAttr.getModifier(LUCK_MODIFIER_ID);
                if (modifier != null) {
                    tooltip.add(TextFormatting.GOLD + "当前幸运值加成: " + TextFormatting.GREEN + String.format("%.2f", modifier.getAmount()));
                } else {
                    tooltip.add(TextFormatting.RED + "当前未生效");
                }
            }
        }

        tooltip.add(TextFormatting.DARK_GRAY + "使用 /setLuckModifier <玩家> <值>");
    }
}
