package com.zzt.eternal_abyss.items;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class YuwanRing extends ItemBase implements IBauble {
    public YuwanRing(String name) {
        super("yuwan_ring");
        this.setMaxStackSize(1);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.COMMON;
    }

    @Override
    public BaubleType getBaubleType(ItemStack itemStack) {
        return BaubleType.RING;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> list, ITooltipFlag flagIn) {
        list.add("");
        list.add("§b暮色遗物 · 冰霜造物");
        list.add("");
        list.add("§7由冰雪女王的头颅锻造而成。");
        list.add("§7寒意并非来自霜雪，而是死亡前最后的体温流失。");
        list.add("");
        list.add("§b攻击效果：");
        list.add("§7• 攻击命中时剥离目标 §c25 点生命值");
        list.add("§7• 无视护甲、抗性");
        list.add("§7• 每秒最多触发 §e4 §7次");
        list.add("");

        if(GuiScreen.isShiftKeyDown()){
            list.add("");
            list.add("§8\"她并未挥剑。\"");
            list.add("§8\"只是让战场，回到应有的低温。\"");
            list.add("");
        }
    }

}
