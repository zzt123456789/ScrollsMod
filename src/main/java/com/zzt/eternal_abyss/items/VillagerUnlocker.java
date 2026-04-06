package com.zzt.eternal_abyss.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.*;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class VillagerUnlocker extends ItemBase {

    public VillagerUnlocker(String name) {
        super(name);
        this.setMaxStackSize(64);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.UNCOMMON;
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand) {
        if (!(target instanceof EntityVillager)) return false;

        EntityVillager villager = (EntityVillager) target;

        if (!villager.world.isRemote && isLibrarian(villager)) {
            MerchantRecipeList recipes = villager.getRecipes(playerIn);

            if (recipes == null || recipes.isEmpty()) {
                playerIn.sendStatusMessage(new TextComponentString(TextFormatting.GRAY + "该图书管理员尚无交易内容。"), true);
                return true;
            }

            boolean changed = false;

            if (villager.world.rand.nextFloat() < 1f) {
                for (MerchantRecipe recipe : recipes) {
                    ItemStack result = recipe.getItemToSell();
                    if (result.getItem() == Items.ENCHANTED_BOOK && result.hasTagCompound()) {
                        NBTTagCompound tag = result.getTagCompound();
                        NBTTagList enchList = tag.getTagList("StoredEnchantments", 10);

                        for (int i = 0; i < enchList.tagCount(); i++) {
                            NBTTagCompound ench = enchList.getCompoundTagAt(i);
                            int id = ench.getShort("id");
                            Enchantment enchType = Enchantment.getEnchantmentByID(id);
                            if (enchType != null) {
                                ench.setShort("lvl", (short) enchType.getMaxLevel());
                                changed = true;
                            }
                        }
                    }
                }
            }

            if (changed) {
                playerIn.sendStatusMessage(new TextComponentString(TextFormatting.GREEN + "已升级！"), true);
                villager.playSound(SoundEvents.ENTITY_VILLAGER_YES, 1.0F, 1.0F);
            } else {
                playerIn.sendStatusMessage(new TextComponentString(TextFormatting.GRAY + "未提升附魔等级。"), true);
                villager.playSound(SoundEvents.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
            }

            stack.shrink(1);
            return true;
        }

        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (net.minecraft.client.gui.GuiScreen.isShiftKeyDown()) {
            tooltip.add(TextFormatting.STRIKETHROUGH + "" + TextFormatting.DARK_PURPLE + "  调♂教图书管理员");
        } else {
            tooltip.add(TextFormatting.GOLD + "右键图书管理员：");
            tooltip.add(TextFormatting.YELLOW + "  ▸ 使村民售卖的附魔变为满级");
        }
    }

    private boolean isLibrarian(EntityVillager villager) {
        return "librarian".equals(villager.getProfessionForge().getRegistryName().getPath());
    }
}
