package com.zzt.eternal_abyss.init;

import com.zzt.eternal_abyss.blocks.BlockAutoAnvil;
import com.zzt.eternal_abyss.blocks.BlockXPDrain;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;

/**
 * 方块实例集中管理，不负责注册，注册逻辑由 RegistryBlocks 完成
 */
// ModBlocks.java
public class ModBlocks {

    public static final Block AUTO_ANVIL = new BlockAutoAnvil();
    public static final ItemBlock AUTO_ANVIL_ITEM = new ItemBlock(AUTO_ANVIL);

    public static final Block XPDrain = new BlockXPDrain(Material.GLASS, "xp_drain");

    public static final ItemBlock XPDrain_ITEM = new ItemBlock(XPDrain);

    static {
        AUTO_ANVIL_ITEM.setRegistryName(AUTO_ANVIL.getRegistryName());
        // 只需要给 ItemBlock 设置 registryName，别动 Block了！
        XPDrain_ITEM.setRegistryName(XPDrain.getRegistryName());
    }
}
