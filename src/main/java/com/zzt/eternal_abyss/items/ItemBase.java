package com.zzt.eternal_abyss.items;

import com.zzt.eternal_abyss.init.ModCreativeTabs;
import net.minecraft.item.Item;

public class ItemBase extends Item {
    public ItemBase(String name) {
        super();
        this.setRegistryName(name);
        this.setTranslationKey(name);
        this.setCreativeTab(ModCreativeTabs.CelestialArtifactsTab);
    }
}