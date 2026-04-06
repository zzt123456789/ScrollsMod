package com.zzt.eternal_abyss.items;

import com.zzt.eternal_abyss.entity.EntitySupernovaBall;
import com.zzt.eternal_abyss.init.ModCreativeTabs;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class Supernova extends ItemSword {
    public Supernova(ToolMaterial material, String name){
        super(material);
        this.setTranslationKey(name);
        this.setRegistryName(name);
        this.setCreativeTab(ModCreativeTabs.CelestialArtifactsTab);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.EPIC;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000; // 标准弓的蓄力时间
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW; // 显示拉弓动画（可换 NONE）
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(
            World world,
            EntityPlayer player,
            EnumHand hand
    ) {
        player.setActiveHand(hand); // ⭐ 开始蓄力
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }


    @Override
    public void onPlayerStoppedUsing(
            ItemStack stack,
            World world,
            EntityLivingBase entityLiving,
            int timeLeft
    ) {
        if (!(entityLiving instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) entityLiving;

        // 蓄力时间（tick）
        int useTime = this.getMaxItemUseDuration(stack) - timeLeft;

        // 转成 charge（你可以后面再调曲线）
        float charge = Math.min(useTime / 20.0F, 1.5F);

        if (!world.isRemote) {

            EntitySupernovaBall ball =
                    new EntitySupernovaBall(world, player, charge);

            ball.shoot(
                    player,
                    player.rotationPitch,
                    player.rotationYaw,
                    0.5F + charge * 0.1F
            );

            world.spawnEntity(ball);
        }

        float attackSpeed = (float) player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getAttributeValue();
        if (attackSpeed <= 0) attackSpeed = 1.0F;
        int cool_downTicks = (int) (40 / attackSpeed);
        player.getCooldownTracker().setCooldown(this, cool_downTicks);
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add("");
        tooltip.add(I18n.format("tooltip.supernova.description1"));
        tooltip.add(I18n.format("tooltip.supernova.description2"));
        tooltip.add(I18n.format("tooltip.supernova.description3"));
        tooltip.add(I18n.format("tooltip.supernova.description4"));
        tooltip.add(I18n.format("tooltip.supernova.description5"));
        tooltip.add("");
        tooltip.add(I18n.format("tooltip.supernova.description6"));
        tooltip.add("");

    }
}
