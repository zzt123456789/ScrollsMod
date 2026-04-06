package com.zzt.eternal_abyss.init;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class ModCreativeTabs {

    public static final CreativeTabs CelestialArtifactsTab = new CreativeTabs("eternal_abyss") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ModItems.ABYSSAL_ESSENCE);
        }
    };

}
