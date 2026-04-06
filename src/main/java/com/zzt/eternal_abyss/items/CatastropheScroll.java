package com.zzt.eternal_abyss.items;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import net.minecraft.item.ItemStack;


public class CatastropheScroll extends ItemBase implements IBauble {

    public CatastropheScroll() {
        super("catastrophe_scroll");
        this.setMaxStackSize(1);

    }

    @Override
    public BaubleType getBaubleType(ItemStack itemstack) {
        return BaubleType.TRINKET;
    }
}