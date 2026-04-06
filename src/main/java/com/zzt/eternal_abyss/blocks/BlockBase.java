package com.zzt.eternal_abyss.blocks;

import com.zzt.eternal_abyss.init.ModCreativeTabs;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockBase extends Block {
    public BlockBase(Material material, String name) {
        super(material);
        this.setRegistryName(name);
        this.setTranslationKey(name);
        this.setCreativeTab(ModCreativeTabs.CelestialArtifactsTab);
    }
}
